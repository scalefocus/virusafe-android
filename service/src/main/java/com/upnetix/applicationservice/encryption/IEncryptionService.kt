package com.upnetix.applicationservice.encryption

/**
 * Interface for the encryption
 */
interface IEncryptionService {

	/**
	 *Encrypts a given string securely with the help of KeyStore
	 * @return the encrypted string
	 */
	fun encryptValue(toEncryptValue: String): String

	/**
	 * Decrypts a given string with the help of KeyStore
	 * @return the decrypted string or null if the argument provided cannot be decoded from Base64
	 */
	fun decryptValue(toDecryptValue: String): String?
}