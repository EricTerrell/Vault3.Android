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

import androidx.documentfile.provider.DocumentFile;

import com.ericbt.vault3base.FileActivity;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.async.Async;

import java.util.Arrays;
import java.util.Locale;

public class FindVaultFiles extends Async {
    public void findVaultFiles(FileActivity fileActivity, Uri folderUri) {
        submit(() -> {
            Throwable exception = null;
            DocumentFile[] documentFiles = null;

            try {
                documentFiles = getFileList(fileActivity, folderUri);
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag, String.format("FindVaultFiles: Exception %s",
                        ex.getMessage()), ex);

                exception = ex;
            }

            final boolean success = exception == null;
            final DocumentFile[] documentFiles2 = documentFiles;

            handler.post(() -> {
                fileActivity.setEnabled(true);
                fileActivity.setSearching(false);

                fileActivity.updateFileList(documentFiles2);

                if (!success) {
                    new AlertDialog.Builder(fileActivity)
                            .setTitle(String.format("Search for %s Documents", StringLiterals.ProgramName))
                            .setMessage("Cannot find documents.")
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }
            });
        });
    }

    private DocumentFile[] getFileList(FileActivity fileActivity, Uri folderUri) {
        final DocumentFile[] documentFiles =
                DocumentFile.fromTreeUri(fileActivity, folderUri).listFiles();

        return Arrays.stream(documentFiles)
                .filter(documentFile ->
                                documentFile != null &&
                                documentFile.getName() != null &&
                                documentFile.getName()
                                        .toLowerCase(Locale.ROOT)
                                        .endsWith(StringLiterals.FileType))
                .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                .toArray(DocumentFile[]::new);
    }
}
