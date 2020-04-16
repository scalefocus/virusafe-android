package com.upnetix.applicationservice.encryption

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

/**
 * The service class which is responsible for encryption and decryption.
 *
 * @property keyAlias - alias for the generated RSA pair, that should be passed when creating the service
 * and should be unique per project
 */

internal class EncryptionService constructor(
	private val context: Context,
	private val keyAlias: String
) : IEncryptionService {

	private lateinit var keyStore: KeyStore

	private val sharedPreferences: SharedPreferences

	init {
		sharedPreferences =
			context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
		initializeKeyStore()
	}

	companion object {
		/**
		 * The Android KeyStore provider
		 */
		private const val ANDROID_KEYSTORE = "AndroidKeyStore"

		/**
		 * Years of validity of the generated certificate
		 */
		private const val KEYSTORE_END_DATE_YEAR = 30

		/**
		 *The Cipher RSA transformation
		 */
		private const val RSA_MODE = "RSA/ECB/PKCS1Padding"

		/**
		 * The Cipher RSA provider below API 23
		 */
		private const val ANDROID_OPEN_SSL = "AndroidOpenSSL"

		/**
		 * The Cipher RSA provider above API 23
		 */
		private const val ANDROID_OPEN_SSL_WORKAROUND = "AndroidKeyStoreBCWorkaround"

		/**
		 * The key for the AES key in SharedPrefs
		 */
		private const val ENCRYPTED_AES_KEY = "encrypted_key"

		/**
		 * The RSA algorithm name
		 */
		private const val RSA = "RSA"

		/**
		 * The AES algorithm name
		 */
		private const val AES = "AES"

		/**
		 * Provider for the AES encryption
		 */
		private const val BC = "BC"

		/**
		 * The AES transformation that works with padding
		 */
		private const val AES_TRANSFORMATION_WITH_PADDING = "AES/ECB/PKCS7Padding"

		private const val DEFAULT_STR = ""

		private const val SHARED_PREFS_FILE_NAME = "app_prefs_key"

		/**
		 * The UTF-8 charset name
		 */
		private const val UTF_CHARSET = "UTF-8"

		private const val AES_KEY_SIZE = 16
	}

	@Suppress("TooGenericExceptionCaught")
	private fun initializeKeyStore() {
		keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
		keyStore.load(null)
		//check if the pair already exists
		if (!keyStore.containsAlias(keyAlias)) {
			generateRSAKeyPairs()
		}

		//retrieve the symmetric key used for encryption
		val encryptedBase64AESKey = sharedPreferences.getString(ENCRYPTED_AES_KEY, DEFAULT_STR)
		if (encryptedBase64AESKey.isNullOrEmpty()) {
			//create new symmetric encryption key
			val encryptedKey = generateEncryptionAESKey()
			//save the key to be used later on
			sharedPreferences.edit()
				.putString(ENCRYPTED_AES_KEY, encryptedKey)
				.apply()
		}
	}

	/**
	 * Creates a pair of public and private RSA encryption keys
	 * to be used from the KeyStore
	 */
	private fun generateRSAKeyPairs() {
		//set validity to be 30 years
		val startDate = Calendar.getInstance()
		val endDate = Calendar.getInstance()
		endDate.add(Calendar.YEAR, KEYSTORE_END_DATE_YEAR)

		val subject = "CN=$keyAlias"
		val pair: AlgorithmParameterSpec

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			pair = KeyPairGeneratorSpec.Builder(context)
				.setAlias(keyAlias)
				.setSubject(X500Principal(subject))
				.setSerialNumber(BigInteger.TEN)
				.setStartDate(startDate.time)
				.setEndDate(endDate.time)
				.build()
		} else {
			pair = KeyGenParameterSpec.Builder(
					keyAlias,
					KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
				)
				.setCertificateSubject(X500Principal(subject))
				.setRandomizedEncryptionRequired(false)
				.setBlockModes(KeyProperties.BLOCK_MODE_ECB)
				.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
				.setCertificateSerialNumber(BigInteger.TEN)
				.setCertificateNotBefore(startDate.time)
				.setCertificateNotAfter(endDate.time)
				.build()
		}
		//generate self signed certificate
		val keyPairGenerator = KeyPairGenerator.getInstance(RSA, ANDROID_KEYSTORE)
		//init the key pair
		keyPairGenerator.initialize(pair)
		//generate it
		keyPairGenerator.generateKeyPair()
	}

	/**
	 * Creates a symmetric key and encodes it in Base64 form with default encode flag
	 * @return  the created encrypted AES key
	 */
	private fun generateEncryptionAESKey(): String {
		val key = ByteArray(AES_KEY_SIZE)
		val secureRandom = SecureRandom()
		secureRandom.nextBytes(key)
		val encryptedAESKey = rsaEncrypt(key)
		return String(Base64.encode(encryptedAESKey, Base64.DEFAULT))
	}

	/**
	 * Encrypts the AES key with the help of the KeyStore public key
	 * @return ByteArray the encrypted value of the AES key
	 */
	private fun rsaEncrypt(aesKeyToEncrypt: ByteArray): ByteArray {
		val publicKey: PublicKey? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			keyStore.getCertificate(keyAlias).publicKey
		} else {
			val keyStorePrivateKeyEntry =
				keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
			keyStorePrivateKeyEntry?.certificate?.publicKey
		}
		val inputCipher = getCorrectCipherRSA()
		inputCipher.init(Cipher.ENCRYPT_MODE, publicKey)
		val outputStream = ByteArrayOutputStream()
		val cipherOutputStream = CipherOutputStream(outputStream, inputCipher)
		try {
			cipherOutputStream.write(aesKeyToEncrypt)
		} catch (e: IOException) {
			e.printStackTrace()
		} finally {
			cipherOutputStream.close()
		}
		return outputStream.toByteArray()
	}

	/**
	 * Decrypts the AES key via the KeyStore private key
	 * @return  the decrypted value of the key
	 */
	private fun rsaDecrypt(aesKeyToDecrypt: ByteArray): ByteArray? {
		val outputCipher = getCorrectCipherRSA()
		val privateKey: Key? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			keyStore.getKey(keyAlias, null)
		} else {
			val keyStorePrivateKeyEntry =
				keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
			keyStorePrivateKeyEntry?.privateKey
		}
		outputCipher.init(Cipher.DECRYPT_MODE, privateKey)
		val inputStream = ByteArrayInputStream(aesKeyToDecrypt)
		val cipherInputStream = CipherInputStream(inputStream, outputCipher)
		var decryptedBytes: ByteArray? = null
		try {
			decryptedBytes = cipherInputStream.readBytes()
		} catch (e: IOException) {
			e.printStackTrace()
		} finally {
			cipherInputStream.close()
		}
		return decryptedBytes
	}

	/**
	 *Gets the correct provider for the RSA encryption based on API
	 */
	private fun getCorrectCipherRSA(): Cipher {
		return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			Cipher.getInstance(RSA_MODE, ANDROID_OPEN_SSL)
		} else {
			Cipher.getInstance(RSA_MODE, ANDROID_OPEN_SSL_WORKAROUND)
		}
	}

	/**
	 * Retrieves, decodes and decrypts the AES key
	 * @return a key for encryption and decryption
	 */
	private fun getSecretKey(): SecretKeySpec? {
		val encryptedAES256key = sharedPreferences.getString(ENCRYPTED_AES_KEY, DEFAULT_STR)
		val decrypthedAESkey = Base64.decode(encryptedAES256key, Base64.DEFAULT)
		val decryptedKey = rsaDecrypt(decrypthedAESkey)
		return SecretKeySpec(decryptedKey, AES)
	}

	override fun encryptValue(toEncryptValue: String): String {
		val c = Cipher.getInstance(AES_TRANSFORMATION_WITH_PADDING, BC)
		c.init(Cipher.ENCRYPT_MODE, getSecretKey())
		val encodedBytes = c.doFinal(toEncryptValue.toByteArray())
		val encryptedBase64Encoded = Base64.encode(encodedBytes, Base64.DEFAULT)
		return String(encryptedBase64Encoded, Charset.forName(UTF_CHARSET))
	}

	override fun decryptValue(toDecryptValue: String): String? {
		val c = Cipher.getInstance(AES_TRANSFORMATION_WITH_PADDING, BC)
		val decBase64 = try {
			c.init(Cipher.DECRYPT_MODE, getSecretKey())
			Base64.decode(toDecryptValue.toByteArray(), Base64.DEFAULT)
		} catch (exception: IllegalArgumentException) {
			return null
		}
		val decodedData = c.doFinal(decBase64)
		return String(decodedData, Charset.forName(UTF_CHARSET))
	}
}
