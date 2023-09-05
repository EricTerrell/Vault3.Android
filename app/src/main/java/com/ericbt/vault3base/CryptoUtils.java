/*
  Vault 3
  (C) Copyright 2023, Eric Bergman-Terrell
  
  This file is part of Vault 3.

  Vault 3 is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Vault 3 is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.vault3base;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import commonCode.Base64Coder;

/**
 * @author Eric Bergman-Terrell
 *
 */
public class CryptoUtils {
	private static final int minPasswordLength = 4;

	public static int getMinPasswordLength() {
		return minPasswordLength;
	}
	
	private static final String cipherAlgorithm = "AES";
	private static final String keyAlgorithm = "AES";
	private static final String messageDigestAlgorithm = "SHA-512";
	private static final int keyBits = 128;
	
	private static byte[] getPasswordMessageDigest(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return MessageDigest.getInstance(messageDigestAlgorithm).digest(password.getBytes("UTF-8"));
	}

	public static SecretKey createSecretKey(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		long startTime = System.currentTimeMillis();
		
        int keyLengthBytes = keyBits / 8;
        
		byte[] passwordMessageDigest = getPasswordMessageDigest(password);
		
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.createSecretKey: key length (bits): %d, key length (bytes): %d, algorithm: %s message digest length: %d", 
        										   keyBits, keyLengthBytes, keyAlgorithm, passwordMessageDigest.length));
        
		List<Byte> passwordBytes = new ArrayList<>();
		
		for (byte passwordByte : passwordMessageDigest) {
			passwordBytes.add(passwordByte);
		}
		
		while (passwordBytes.size() < keyLengthBytes) {
			passwordBytes.add((byte) 0);
		}
		
		byte[] passwordByteArray = new byte[keyLengthBytes];
		
		for (int i = 0; i < keyLengthBytes; i++) {
			passwordByteArray[i] = passwordBytes.get(i);
		} 
		
		SecretKey secretKey = new SecretKeySpec(passwordByteArray, keyAlgorithm);
        
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.createSecretKey: %d ms", elapsedMilliseconds));

		return secretKey;
	}

	public static Cipher createEncryptionCipher(SecretKey secretKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		long startTime = System.currentTimeMillis();

		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.createEncryptionCipher: %d ms", elapsedMilliseconds));

		return cipher;
	}
	
	public static Cipher createDecryptionCipher(SecretKey secretKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		long startTime = System.currentTimeMillis();

		Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.createDecryptionCipher: %d ms", elapsedMilliseconds));

		return cipher;
	}
	
	public static String encryptString(Cipher cipher, String plainText) throws UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		long startTime = System.currentTimeMillis();

		// Silently convert null strings to empty strings.
		if (plainText == null) {
			plainText = "";
		}
		
		byte[] plainTextBytes = plainText.getBytes("UTF-8");
		
		byte[] cipherTextBytes = cipher.doFinal(plainTextBytes);
		
		char[] cipherTextArray = Base64Coder.encode(cipherTextBytes);
		String cipherTextString = new String(cipherTextArray);
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.encryptString: %d ms", elapsedMilliseconds));

		return cipherTextString;
	}
	
	public static String decryptString(Cipher cipher, String cipherText) throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		long startTime = System.currentTimeMillis();

		byte[] cipherTextBytes = Base64Coder.decode(cipherText);
		
		byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);
		
		String plainTextString = new String(plainTextBytes, "UTF-8");
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.v(StringLiterals.LogTag, String.format("CryptoUtils.decryptString: %d ms", elapsedMilliseconds));

		return plainTextString;
	}
}
