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

package com.ericbt.vault3base;

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class DocumentFileUtils {
    public static File getTempFolderPath(Context context) {
        return context.getFilesDir();
    }

    public static String getFileName(Uri selectedFileUri) {
        final String[] segments = selectedFileUri.getPath().split("/");

        return segments[segments.length - 1];
    }

    public static void updateDocumentFile(Context context, String sourceFileName, Uri destFileUri) throws IOException {
        final String sourceFilePath = String.format("%s/%s",
                DocumentFileUtils.getTempFolderPath(context),
                sourceFileName);

        try (final InputStream inputStream = new FileInputStream(sourceFilePath);
             final OutputStream outputStream = context.getContentResolver().openOutputStream(destFileUri)) {
            FileUtils.copy(inputStream, outputStream);
        }
    }

    public static String getName(String documentUri) {
        final Uri uri = Uri.parse(documentUri);
        final List<String> pathSegments = uri.getPathSegments();

        final String[] segments = pathSegments.get(pathSegments.size() - 1).split("/");

        return segments[segments.length - 1];
    }

    public static boolean removeDocumentAndTempFile(Context context, Uri documentUri) {
        boolean deleted = false;

        try {
            deleted = DocumentsContract.deleteDocument(context.getContentResolver(), documentUri);

            final String tempFilePath = String.format(
                    "%s/%s",
                    DocumentFileUtils.getTempFolderPath(context),
                    DocumentFileUtils.getName(documentUri.toString())
            );

            final boolean tempFileDeleted = new File(tempFilePath).delete();

            if (!tempFileDeleted) {
                Log.e(StringLiterals.LogTag, String.format("Could not delete %s", tempFilePath));
            }

            final String journalPath =
                    String.format("%s%s", tempFilePath, StringLiterals.JournalSuffix);

            final File journalFile = new File(journalPath);

            if (journalFile.exists()) {
                final boolean journalFileDeleted = journalFile.delete();

                if (!journalFileDeleted) {
                    Log.w(StringLiterals.LogTag,
                            String.format("Could not delete %s", journalFile.getPath()));
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return deleted;
    }
}
