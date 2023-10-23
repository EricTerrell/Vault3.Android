package com.ericbt.vault3base;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import commonCode.VaultDocumentVersion;

@RunWith(AndroidJUnit4.class)
public class CryptoTests {
    private final static String PASSWORD = "PASSWORD";
    private final static String PLAINTEXT = "rutrum tellus pellentesque eu tincidunt tortor aliquam nulla facilisi cras fermentum odio eu feugiat pretium nibh ipsum consequat nisl vel pretium lectus quam id leo in vitae turpis massa sed elementum tempus egestas sed sed risus pretium quam vulputate dignissim suspendisse in est ante in nibh mauris cursus mattis molestie a iaculis at erat pellentesque adipiscing commodo elit at imperdiet dui accumsan sit amet nulla facilisi morbi tempus iaculis urna id volutpat lacus laoreet non curabitur gravida arcu ac tortor dignissim convallis aenean et tortor at risus viverra adipiscing at in tellus integer feugiat scelerisque varius morbi enim nunc faucibus";

    @Test
    public void testRoundTrip() throws InvalidAlgorithmParameterException, NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final VaultDocumentVersion latestVersion = VaultDocumentVersion.getLatestVaultDocumentVersion();

        for (int i = 0; i < 100; i++) {
            final byte[] salt = CryptoUtils.createSalt();
            final byte[] iv = CryptoUtils.createIV();

            final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(PASSWORD, latestVersion, salt, iv);

            final String cipherText = CryptoUtils.encryptString(encryptionCipher, PLAINTEXT);

            Assert.assertNotEquals(PLAINTEXT, cipherText);

            final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(PASSWORD, latestVersion, salt, iv);

            final String decryptedText = CryptoUtils.decryptString(decryptionCipher, cipherText);
            Assert.assertEquals(PLAINTEXT, decryptedText);

            final double increasePercent = (((double) cipherText.length() / (double) PLAINTEXT.length()) - 1.0) * 100.0;

            if (i == 0) {
                System.out.println(String.format("\r\nplaintext: %d\r\nciphertext: %d\r\ngrowth: %.1f%%",
                        PLAINTEXT.length(), cipherText.length(), increasePercent));
            }
        }
    }
}
