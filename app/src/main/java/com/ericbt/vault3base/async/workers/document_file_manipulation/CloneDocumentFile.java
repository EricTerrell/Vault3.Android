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

package com.ericbt.vault3base.async.workers.document_file_manipulation;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.ericbt.vault3base.DocumentFileUtils;
import com.ericbt.vault3base.FileActivity;
import com.ericbt.vault3base.FileUtils;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.async.Async;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CloneDocumentFile extends Async {
    public void cloneDocumentFile(Uri sourceDocumentUri,
                              Uri destDocumentUri,
                              Uri folderUri,
                              FileActivity fileActivity,
                              boolean removeSourceDocument) {

        submit(() -> {
            Throwable exception = null;

            try {
                cloneDocumentFile(fileActivity, sourceDocumentUri, destDocumentUri);

                if (removeSourceDocument) {
                    DocumentFileUtils.removeDocumentAndTempFile(
                            fileActivity,
                            sourceDocumentUri);
                }
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("CloneDocumentFile: Exception %s",
                                ex.getMessage()), ex);

                exception = ex;
            }

            new FindVaultFiles().findVaultFiles(fileActivity, folderUri);

            final boolean success = exception == null;

            handler.post(() -> {
                if (!success) {
                    new AlertDialog.Builder(fileActivity)
                            .setTitle("Copy Document")
                            .setMessage(String.format(
                                    "Cannot copy %s", sourceDocumentUri.toString()))
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }
            });
        });
    }

    private void cloneDocumentFile(Context context, Uri sourceDocumentUri, Uri destDocumentUri)
            throws IOException {
        Log.i(StringLiterals.LogTag, String.format("cloneDocumentFile %s %s thread: %d",
                sourceDocumentUri.toString(),
                destDocumentUri,
                Thread.currentThread().getId()));

        try (final InputStream inputStream =
                     context.getContentResolver().openInputStream(sourceDocumentUri);
             final OutputStream outputStream =
                     context.getContentResolver().openOutputStream(destDocumentUri)) {
            FileUtils.copy(inputStream, outputStream);
        }
    }
}
