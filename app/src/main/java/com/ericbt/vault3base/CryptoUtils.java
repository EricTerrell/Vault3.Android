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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import commonCode.Base64Coder;
import commonCode.VaultDocumentVersion;

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

	private static class DocumentVersion1_3Constants {
		private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
		private static final int KEY_LENGTH = 256;

		private static final int SALT_LENGTH = 16;
		private static final int IV_LENGTH = 16;

		private static final int KEY_ITERATIONS = 1_000;

		private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";

		private static final String KEY_ALGORITHM_SHORT = "AES";
	}

	private static byte[] getPasswordMessageDigest(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return MessageDigest.getInstance(messageDigestAlgorithm).digest(password.getBytes("UTF-8"));
	}

	private static SecretKey createSecretKey(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		final long startTime = System.currentTimeMillis();
		
        final int keyLengthBytes = keyBits / 8;
        
		byte[] passwordMessageDigest = getPasswordMessageDigest(password);
		
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.createSecretKey: key length (bits): %d, key length (bytes): %d, algorithm: %s message digest length: %d", 
        										   keyBits, keyLengthBytes, keyAlgorithm, passwordMessageDigest.length));
        
		final List<Byte> passwordBytes = new ArrayList<>();
		
		for (byte passwordByte : passwordMessageDigest) {
			passwordBytes.add(passwordByte);
		}
		
		while (passwordBytes.size() < keyLengthBytes) {
			passwordBytes.add((byte) 0);
		}
		
		final byte[] passwordByteArray = new byte[keyLengthBytes];
		
		for (int i = 0; i < keyLengthBytes; i++) {
			passwordByteArray[i] = passwordBytes.get(i);
		} 
		
		final SecretKey secretKey = new SecretKeySpec(passwordByteArray, keyAlgorithm);
        
		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.createSecretKey: %d ms", elapsedMilliseconds));

		return secretKey;
	}

	public static byte[] randomBytes(int nBytes) {
		final byte[] result = new byte[nBytes];
		new SecureRandom().nextBytes(result);

		return result;
	}

	public static byte[] createSalt() {
		return randomBytes(DocumentVersion1_3Constants.SALT_LENGTH);
	}

	public static byte[] createIV() {
		return randomBytes(DocumentVersion1_3Constants.IV_LENGTH);
	}

	private static SecretKey createSecretKeyVaultDocumentVersion_1_3(String password, byte[] salt)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		final PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt,
				DocumentVersion1_3Constants.KEY_ITERATIONS,
				DocumentVersion1_3Constants.KEY_LENGTH);

		final SecretKey pbeKey = SecretKeyFactory
				.getInstance(DocumentVersion1_3Constants.KEY_ALGORITHM)
				.generateSecret(pbeKeySpec);

		return new SecretKeySpec(pbeKey.getEncoded(),
				DocumentVersion1_3Constants.KEY_ALGORITHM_SHORT);
	}

	private static Cipher createEncryptionCipher(SecretKey secretKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		long startTime = System.currentTimeMillis();

		final Cipher cipher = Cipher.getInstance(cipherAlgorithm);

		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.createEncryptionCipher: %d ms", elapsedMilliseconds));

		return cipher;
	}

	public static Cipher createEncryptionCipher(String password,
												VaultDocumentVersion vaultDocumentVersion,
												byte[] salt, byte[] iv)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		Cipher cipher;

		if (vaultDocumentVersion.compareTo(VaultDocumentVersion.VERSION_1_3) == 0) {
			final SecretKey secretKey = createSecretKeyVaultDocumentVersion_1_3(password, salt);

			cipher = Cipher.getInstance(DocumentVersion1_3Constants.CIPHER_ALGORITHM);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
		} else {
			cipher = createEncryptionCipher(createSecretKey(password));
		}

		return cipher;
	}

	public static Cipher createDecryptionCipher(String password, VaultDocumentVersion vaultDocumentVersion,
												byte[] salt, byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, UnsupportedEncodingException {
		long startTime = System.currentTimeMillis();

		Cipher cipher;

		if (vaultDocumentVersion.compareTo(VaultDocumentVersion.VERSION_1_3) == 0) {
			final SecretKey secretKey = createSecretKeyVaultDocumentVersion_1_3(password, salt);

			cipher = Cipher.getInstance(DocumentVersion1_3Constants.CIPHER_ALGORITHM);

			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
		} else {
			cipher = Cipher.getInstance(cipherAlgorithm);

			cipher.init(Cipher.DECRYPT_MODE, createSecretKey(password));
		}

		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("CryptoUtils.createDecryptionCipher: %d ms", elapsedMilliseconds));

		return cipher;
	}

	public static String encryptString(Cipher cipher, String plainText) throws UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		long startTime = System.currentTimeMillis();

		// Silently convert null strings to empty strings.
		if (plainText == null) {
			plainText = StringLiterals.EmptyString;
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
