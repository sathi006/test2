/**
 * 
 */
package com.mcg.batch.runtime.impl.batch.utils;

/**
 * 
 * @version 1.0
 * @since:1.0
 * @author Nanda Gopalan
 * 
 */
public interface Encryptor<E, D> {

	/**
	 * Encrypts the input String.
	 * 
	 * @param decrypytedString
	 * @return
	 */
	public E encrypt(String input);

	/**
	 * Decrypts the input String.
	 * 
	 * @param encryptedString
	 * @return
	 */
	public D decrypt(E encryptionObject);

}
