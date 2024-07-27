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
import com.ericbt.vault3base.async.Async;

public class DeleteDocumentFile extends Async {
    public void deleteDocumentFile(Uri documentUri, Uri folderUri, String databasePath,
                                   FileActivity fileActivity) {
        submit(() -> {
            Throwable exception = null;
            boolean deleted = false;

            try {
                if (documentUri != null) {
                    deleted = DocumentFileUtils.removeDocumentAndTempFile(
                            fileActivity, documentUri);
                } else {
                    deleted = FileUtils.deleteDatabaseFile(databasePath);
                }
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("DeleteDocumentFile: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            new FindVaultFiles().findVaultFiles(fileActivity, folderUri);

            final Throwable exception2 = exception;
            final boolean deleted2 = deleted;

            handler.post(() -> {
                if (!deleted2 || exception2 != null) {
                    new AlertDialog.Builder(fileActivity)
                            .setTitle("Delete File")
                            .setMessage(
                                    String.format(
                                            "Could not delete \"%s\"",
                                            databasePath))
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }
            });
        });
    }
}
