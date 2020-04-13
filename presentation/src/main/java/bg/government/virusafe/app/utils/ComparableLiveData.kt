package bg.government.virusafe.app.utils

class ComparableLiveData<T> : SingleLiveEvent<T>() {

	override fun postValue(value: T) {
		if (this.value == value) {
			return
		}
		super.postValue(value)
	}

	override fun setValue(t: T?) {
		if (value == t) {
			return
		}
		super.setValue(t)
	}
}
