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

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.async.Async;

public class DeleteDocumentFileSilent extends Async {
    public void deleteDocumentFileSilent(Uri documentUri, Context context) {
        submit(() -> {
            try {
                final DocumentFile documentFile =
                        DocumentFile.fromSingleUri(context, documentUri);

                documentFile.delete();
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("DeleteDocumentFileSilent: Exception %s",
                                ex.getMessage()), ex);
            }
        });
    }
}
