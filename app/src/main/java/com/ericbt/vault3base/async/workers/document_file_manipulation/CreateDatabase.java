/*
  Vault 3
  (C) Copyright 2024, Eric Bergman-Terrell

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

package com.ericbt.vault3base.async.workers.document_file_manipulation;

import android.app.AlertDialog;
import android.net.Uri;
import android.util.Log;

import com.ericbt.vault3base.DocumentFileUtils;
import com.ericbt.vault3base.FileActivity;
import com.ericbt.vault3base.FileUtils;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.VaultDocument;
import com.ericbt.vault3base.async.Async;

import java.io.File;

public class CreateDatabase extends Async {
    public void createDatabase(String dbPath, Uri sourceFileUri, Uri folderUri,
                               FileActivity fileActivity) {
        submit(() -> {
            Throwable exception = null;

            try {
                VaultDocument.createNewVaultDocument(dbPath);

                DocumentFileUtils.updateDocumentFile(
                        fileActivity,
                        new File(dbPath).getName(),
                        sourceFileUri);

                // We don't need to keep the temporary or journal files.
                FileUtils.deleteDatabaseFile(dbPath);
            }
            catch (Throwable ex) {
                Log.e(StringLiterals.LogTag, String.format(
                        "CreateDatabase: cannot create db file %s exception %s",
                        dbPath,
                        ex.getMessage()),
                        ex);

                exception = ex;
            }

            new FindVaultFiles().findVaultFiles(fileActivity, folderUri);

            final boolean success = exception == null;

            handler.post(() -> {
                if (!success) {
                    new AlertDialog.Builder(fileActivity)
                            .setTitle(String.format("New %s Document", StringLiterals.ProgramName))
                            .setMessage(String.format("Cannot create %s.", dbPath))
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }
            });
        });
    }
}
