package com.ericbt.vault3base;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import commonCode.VaultDocumentVersion;

@RunWith(AndroidJUnit4.class)
public class VaultDocumentVersionTests {
    // Run "ant_build_common_code.xml" in Vault3.Desktop code base
    // to create latest jar.
    @Test
    public void testMaxDocumentVersion() {
        assertEquals("1.3",
                VaultDocumentVersion.getLatestVaultDocumentVersion().toString());
    }
}
