package bg.government.virusafe.app.bluetooth.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.text.TextUtils
import androidx.preference.PreferenceManager
import bg.government.virusafe.app.bluetooth.PeripheralSynchronizationTimeoutInSec
import bg.government.virusafe.app.bluetooth.ProteGoCharacteristicUUIDString
import bg.government.virusafe.app.bluetooth.ProteGoServiceUUIDString
import bg.government.virusafe.app.bluetooth.UuidAdapter
import bg.government.virusafe.app.bluetooth.beacon.BeaconId
import bg.government.virusafe.app.bluetooth.beacon.BeaconIdAgent
import bg.government.virusafe.app.bluetooth.beacon.BeaconIdLocal
import bg.government.virusafe.app.bluetooth.beacon.BeaconIdRemote
import bg.government.virusafe.app.bluetooth.beacon.BeaconIdSource
import bg.government.virusafe.app.bluetooth.proximity.ProximityData
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.exceptions.BleAdapterDisabledException
import com.polidea.rxandroidble2.exceptions.BleAlreadyConnectedException
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import com.polidea.rxandroidble2.exceptions.BleGattCharacteristicException
import com.polidea.rxandroidble2.exceptions.BleGattException
import com.upnetix.applicationservice.geolocation.Proximity
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ProteGoConnector(beaconIdAgent: BeaconIdAgent, val context: Context) {

	private val uuidProteGoService = UUID.fromString(ProteGoServiceUUIDString)
	private val uuidProteGoCharacteristic = UUID.fromString(ProteGoCharacteristicUUIDString)
	private val beaconIdToUseSubject: BehaviorSubject<BeaconIdToUse> =
		BehaviorSubject.createDefault(BeaconIdToUse.NoBeaconId)
	private val beaconIdAgentListener = object : BeaconIdAgent.Listener {
		override fun useBeaconId(beaconIdLocal: BeaconIdLocal?) {
			val beaconIdToUse = when (beaconIdLocal) {
				null -> BeaconIdToUse.NoBeaconId
				else -> BeaconIdToUse.CurrentBeaconId(beaconIdLocal.beaconId)
			}
			this@ProteGoConnector.beaconIdToUseSubject.onNext(beaconIdToUse)
		}
	}

	init {
		beaconIdAgent.registerListener(beaconIdAgentListener)
	}

	private sealed class DiscoveryProcess {
		object Started : DiscoveryProcess()
		sealed class Finished : DiscoveryProcess() {
			object NotFound : Finished()
			data class Found(
				val connection: RxBleConnection,
				val proteGoCharacteristic: BluetoothGattCharacteristic
			) : Finished()
		}
	}

	private sealed class BeaconIdToUse {
		data class CurrentBeaconId(val beaconId: BeaconId) : BeaconIdToUse()
		object NoBeaconId : BeaconIdToUse()
	}

	private sealed class ConnectionResult {
		object TimedOut : ConnectionResult()
		object AlreadyConnecting : ConnectionResult()
		data class Connected(val connection: RxBleConnection) : ConnectionResult()
	}

	fun syncBeaconIds(proteGoPeripheral: ClassifiedPeripheral.ProteGo): Observable<SyncEvent> =
		proteGoPeripheral.bleDevice.establishConnection(false)
			.timeout(PeripheralSynchronizationTimeoutInSec, TimeUnit.SECONDS)
			.map<ConnectionResult> { ConnectionResult.Connected(it) }
			.onErrorReturn {
				when (it) {
					is BleAlreadyConnectedException -> ConnectionResult.AlreadyConnecting
					is TimeoutException -> ConnectionResult.TimedOut
					else -> throw it
				}
			}
			.switchMap { connectionResult ->
				when (connectionResult) {
					ConnectionResult.TimedOut -> Observable.just(SyncEvent.Connection.Error.Timeout)
					ConnectionResult.AlreadyConnecting -> Observable.just(SyncEvent.Connection.Error.Duplicate)
					is ConnectionResult.Connected -> executeExchangeProcess(
						proteGoPeripheral,
						connectionResult.connection
					)
				}
			}
			.startWithArray(SyncEvent.Connection.Connecting)
			.takeUntil { it is SyncEvent.Process.End }
			.onErrorReturn { classifyError(it) }

	private fun classifyError(throwable: Throwable): SyncEvent =
		when (throwable) {
			is BleDisconnectedException -> SyncEvent.Connection.Error.Gatt(throwable.state)
			is BleGattException -> SyncEvent.Connection.Error.Gatt(throwable.status)
			is BleAdapterDisabledException -> SyncEvent.Connection.Error.AdapterOff

			is CompositeException -> {
				if (throwable.exceptions.size == 1) classifyError(throwable.exceptions[0])
				else throw throwable
			}

			else -> throw throwable
		}

	private fun executeExchangeProcess(
		proteGoPeripheral: ClassifiedPeripheral.ProteGo,
		connection: RxBleConnection
	): Observable<SyncEvent> {
		return connection.discoverServices()
			.map<DiscoveryProcess> { services ->
				services.bluetoothGattServices.find { it.uuid == uuidProteGoService }
					?.getCharacteristic(uuidProteGoCharacteristic)
					?.let { DiscoveryProcess.Finished.Found(connection, it) }
					?: DiscoveryProcess.Finished.NotFound
			}
			.toObservable()
			.startWithArray(DiscoveryProcess.Started)
			.flatMap {
				when (it) {
					DiscoveryProcess.Started -> Observable.just(SyncEvent.Connection.DiscoveringServices)
					DiscoveryProcess.Finished.NotFound -> Observable.just(SyncEvent.Process.End.NoProteGoAttributes)
						.also {
							if (proteGoPeripheral !is ClassifiedPeripheral.ProteGo.PotentialAdvertisement) {
								Timber.tag("[connection]")
									.w("ProteGoCharacteristic not found for: ${proteGoPeripheral.className()}")
							}
						}
					is DiscoveryProcess.Finished.Found -> when (proteGoPeripheral) {
						is ClassifiedPeripheral.ProteGo.FullAdvertisement -> {
							val distance = ProximityData.calculateDistance(proteGoPeripheral.rssi ?: 0)

							val logBeacon = TextUtils.concat(
								"ProteGoConnector - Beacon: ", UuidAdapter.getUUIDFromBytes(proteGoPeripheral.beaconId.byteArray).toString(),
								" distance: ", String.format("%.1f", distance),
								" rssi: ", proteGoPeripheral.rssi.toString(),
								" Mac: ", proteGoPeripheral.bleDevice.macAddress, " ",
								"time: ", Calendar.getInstance().time.toString(),  "\n").toString()

							Timber.i(logBeacon)

							val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
							val savedBeacons = sharedPrefs.getString("beacons", "")
							val editor = sharedPrefs.edit()
							editor.putString("beacons", "$savedBeacons \n $logBeacon")
							editor.apply()

							if (distance >= 0) {
								val proximitiesList = ProximityData.getSavedProximities(context)

								proximitiesList.add(
									Proximity(
										String.format(
											Locale.US,
											"%.1f",
											distance
										), UuidAdapter.getUUIDFromBytes(proteGoPeripheral.beaconId.byteArray).toString()
									)
								)

								ProximityData.saveProximities(context, proximitiesList)
							}

							it.writeLocalBeaconIdOnly()
						}
						is ClassifiedPeripheral.ProteGo.MinimalAdvertisement,
						is ClassifiedPeripheral.ProteGo.PotentialAdvertisement -> it.syncBeaconIds()
					}
						.concatWith(Observable.just(SyncEvent.Process.End.Finished))
						.startWithArray(SyncEvent.Process.Start)
				}
			}
	}

	private fun DiscoveryProcess.Finished.Found.writeLocalBeaconIdOnly(): Observable<SyncEvent.Process> =
		this.writeLocalBeaconId().toObservable().cast(SyncEvent.Process::class.java)

	private fun DiscoveryProcess.Finished.Found.syncBeaconIds(): Observable<SyncEvent.Process> =
		this.readRemoteBeaconId()
			.toObservable()
			.publish { readRemoteBeaconIdObs ->
				val writeLocalBeaconIdIfReadSuccessfulObs = readRemoteBeaconIdObs
					.filter { it is SyncEvent.Process.ReadBeaconId.Valid }
					.flatMapSingle { writeLocalBeaconId() }
				Observable.merge(
					readRemoteBeaconIdObs,
					writeLocalBeaconIdIfReadSuccessfulObs
				)
			}

	private fun DiscoveryProcess.Finished.Found.writeLocalBeaconId(): Single<SyncEvent.Process.WrittenBeaconId> =
		this@ProteGoConnector.beaconIdToUseSubject
			.take(1)
			.singleOrError()
			.flatMap { currentBeaconIdToUse ->
				when (currentBeaconIdToUse) {
					BeaconIdToUse.NoBeaconId ->
						Single.just(SyncEvent.Process.WrittenBeaconId.Invalid)
					is BeaconIdToUse.CurrentBeaconId ->
						this.connection.writeCharacteristic(
								this.proteGoCharacteristic,
								currentBeaconIdToUse.beaconId.byteArray
							)
							.map<SyncEvent.Process.WrittenBeaconId> { SyncEvent.Process.WrittenBeaconId.Success }
							.onErrorReturn {
								if (it is BleGattCharacteristicException) SyncEvent.Process.WrittenBeaconId.Failure(it.status)
								else throw it
							}
				}
			}

	@Suppress("RemoveExplicitTypeArguments") // does not infer well
	private fun DiscoveryProcess.Finished.Found.readRemoteBeaconId(): Single<SyncEvent.Process.ReadBeaconId> =
		Single.zip<ByteArray, Int, SyncEvent.Process.ReadBeaconId>(
				this.connection.readCharacteristic(this.proteGoCharacteristic),
				this.connection.readRssi(),
				BiFunction { bytes, rssi ->
					if (bytes.size == BeaconId.byteCount) {
						val beaconId = BeaconId(bytes, 0)
						val remoteBeaconId = BeaconIdRemote(
							beaconId,
							rssi,
							BeaconIdSource.CONNECTION_OUTGOING,
							BluetoothAdapter.getDefaultAdapter().address ?: ""
						)
						SyncEvent.Process.ReadBeaconId.Valid(remoteBeaconId)
					} else {
						SyncEvent.Process.ReadBeaconId.Invalid
					}
				}
			)
			.onErrorReturn {
				if (it is BleGattCharacteristicException) SyncEvent.Process.ReadBeaconId.Failure(it.status)
				else throw it
			}
}