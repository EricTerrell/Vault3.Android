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

import android.util.Log;

import com.ericbt.vault3base.ChangePasswordProcessingActivity;
import com.ericbt.vault3base.DocumentFileUtils;
import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.VaultPreferenceActivity;
import com.ericbt.vault3base.async.Async;
import com.ericbt.vault3base.async.IReportProgress;
import com.ericbt.vault3base.async.UpdateStatus;

import java.io.File;

public class ChangePassword extends Async implements IReportProgress {
    private ChangePasswordProcessingActivity changePasswordProcessingActivity;

    public void changePassword(String newPassword, ChangePasswordProcessingActivity
            changePasswordProcessingActivity) {
        this.changePasswordProcessingActivity = changePasswordProcessingActivity;

        submit(() -> {
            Throwable exception = null;

            try {
                // Change password in temp file.
                Globals.getApplication().getVaultDocument()
                        .changePassword(newPassword, this);

                final String fileName =
                        new File(
                                Globals.getApplication().getVaultDocument().getDatabase().getPath()
                        ).getName();

                // Update corresponding document.
                DocumentFileUtils.updateDocumentFile(
                        changePasswordProcessingActivity,
                        fileName,
                        VaultPreferenceActivity.getSelectedFileUri());
            }
            catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("ChangePassword: exception %s",
                                ex.getMessage()), ex);

                exception = ex;
            }

            final Throwable ex = exception;

            handler.post(() -> {
                if (ex == null) {
                    Globals.getApplication().getPasswordCache().put(
                            Globals.getApplication().getVaultDocument().getDatabase().getPath(),
                            newPassword);
                    Globals.getApplication().getVaultDocument().setDirty(false);
                }

                changePasswordProcessingActivity.finish(ex);
            });
        });
    }

    @Override
    public void reportProgress(float percent) {
        handler.post(() -> {
            changePasswordProcessingActivity.progressUpdate(new UpdateStatus(percent));
        });
    }
}
