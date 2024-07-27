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

package com.ericbt.vault3base.async.workers;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.ericbt.vault3base.DocumentFileUtils;
import com.ericbt.vault3base.FileActivity;
import com.ericbt.vault3base.FileUtils;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.async.Async;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OpenDocumentFile extends Async {
    public void openDocumentFile(Uri sourceFileUri, String sourceFileName,
                                 FileActivity fileActivity) {
        submit(() -> {
            Throwable exception = null;
            String destFilePath = null;

            try {
                FileUtils.deleteAllTempFiles(fileActivity);

                destFilePath = copyDocumentFile(fileActivity, sourceFileUri, sourceFileName);
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("OpenDocumentFile: Exception %s",
                                ex.getMessage()), ex);

                exception = ex;
            }

            final boolean success = exception == null;
            final String destFilePath2 = destFilePath;

            handler.post(() -> {
                if (!success) {
                    new AlertDialog.Builder(fileActivity)
                            .setTitle("Open Document")
                            .setMessage(
                                    String.format(
                                            "Cannot copy %s",
                                            sourceFileUri.toString()))
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                } else {
                    // Cause temporary Vault 3 file to be loaded.
                    final Intent returnData = new Intent();
                    returnData.putExtra(StringLiterals.Action, StringLiterals.Load);
                    returnData.putExtra(StringLiterals.DBPath, destFilePath2);

                    fileActivity.setResult(RESULT_OK, returnData);
                    fileActivity.finish();
                }
            });
        });
    }

    private String copyDocumentFile(Context context, Uri sourceFileUri, String sourceFileName)
            throws IOException {
        Log.i(StringLiterals.LogTag,
                String.format("OpenDocumentFile.copyDocumentFile %s %s thread: %d",
                sourceFileUri.toString(),
                sourceFileName,
                Thread.currentThread().getId()));

        final String destFilePath = String.format("%s/%s",
                DocumentFileUtils.getTempFolderPath(context),
                sourceFileName);

        try (final InputStream inputStream =
                     context.getContentResolver().openInputStream(sourceFileUri);
             final OutputStream outputStream = new FileOutputStream(destFilePath)) {
            final long bytes = FileUtils.copy(inputStream, outputStream);

            Log.i(StringLiterals.LogTag, String.format("OpenDocumentFileTask: bytes: %,d", bytes));
        }

        return destFilePath;
    }
}
