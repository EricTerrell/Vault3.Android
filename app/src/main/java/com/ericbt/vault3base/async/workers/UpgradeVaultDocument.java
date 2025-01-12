/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell

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

package com.ericbt.vault3base.async.workers;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ericbt.vault3base.DocumentFileUtils;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.UpgradeVaultDocumentActivity;
import com.ericbt.vault3base.VaultDocument;
import com.ericbt.vault3base.VaultPreferenceActivity;
import com.ericbt.vault3base.async.Async;
import com.ericbt.vault3base.async.IReportProgress;
import com.ericbt.vault3base.async.UpdateStatus;

import java.io.File;

public class UpgradeVaultDocument extends Async implements IReportProgress {
    private UpgradeVaultDocumentActivity upgradeVaultDocumentActivity;

    public void upgradeVaultDocument(String dbPath, String password,
                                     UpgradeVaultDocumentActivity updateVaultDocumentActivity) {
        this.upgradeVaultDocumentActivity = updateVaultDocumentActivity;

        submit(() -> {
            Throwable exception = null;

            try {
                final SQLiteDatabase database =
                        SQLiteDatabase.openOrCreateDatabase(dbPath, null);
                final VaultDocument vaultDocument = new VaultDocument(database);

                vaultDocument.upgradeVaultDocument(password, this);

                vaultDocument.close();

                final String databaseFileName = new File(database.getPath()).getName();

                DocumentFileUtils.updateDocumentFile(
                        upgradeVaultDocumentActivity,
                        databaseFileName,
                        VaultPreferenceActivity.getSelectedFileUri());
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("UpgradeVaultDocument: Exception %s",
                                ex.getMessage()), ex);

                exception = ex;
            }

            final Throwable exception2 = exception;

            handler.post(() -> {
                upgradeVaultDocumentActivity.finish(exception2, dbPath);
            });
        });
    }

    @Override
    public void reportProgress(float percent) {
        handler.post(() -> {
            upgradeVaultDocumentActivity.progressUpdate(new UpdateStatus(percent));
        });
    }
}
