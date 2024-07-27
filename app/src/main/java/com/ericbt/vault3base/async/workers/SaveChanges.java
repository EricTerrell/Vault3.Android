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

import android.app.AlertDialog;
import android.net.Uri;
import android.util.Log;

import com.ericbt.vault3base.DocumentFileUtils;
import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.Vault3;
import com.ericbt.vault3base.async.Async;

public class SaveChanges extends Async {
    public void saveChanges(String currentDocumentName, Uri sourceFileUri, Vault3 activity) {
        submit(() -> {
            Throwable exception = null;

            try {
                DocumentFileUtils.updateDocumentFile(
                        activity,
                        currentDocumentName,
                        sourceFileUri);
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("CopyDocument: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            final Throwable exception2 = exception;

            handler.post(() -> {
                if (exception2 != null) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Save Changes")
                            .setMessage(String.format("Cannot save changes to %s: %s",
                                    currentDocumentName,
                                    exception2.getMessage()))
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }
                else {
                    Globals.getApplication().getVaultDocument().setDirty(false);

                    activity.setEnabled(true);
                }
            });
        });
    }
}
