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

package com.ericbt.vault3base.async_tasks.change_password;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.vault3base.DocumentFileUtils;
import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.VaultPreferenceActivity;

import java.io.File;

public class ChangePasswordTask extends AsyncTask<ChangePasswordTaskParameters, Void, ChangePasswordTaskResult> {
	private ChangePasswordTaskParameters parameters;
	
	@Override
	protected ChangePasswordTaskResult doInBackground(ChangePasswordTaskParameters... params) {
		parameters = params[0];
		
		final ChangePasswordTaskResult result = new ChangePasswordTaskResult();
		
		try {
			// Change password in temp file.
			Globals.getApplication().getVaultDocument().changePassword(parameters.getNewPassword());

			final String fileName = new File(Globals.getApplication().getVaultDocument().getDatabase().getPath()).getName();

			// Update corresponding document.
			DocumentFileUtils.updateDocumentFile(
					parameters.getFileActivity(),
					fileName,
					VaultPreferenceActivity.getSelectedFileUri());
		}
		catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("ChangePasswordTask: exception %s", ex.getMessage()));
			result.setException(ex);
		}

		return result;
	}

	@Override
	protected void onPostExecute(ChangePasswordTaskResult result) {
		final String newPassword = parameters.getNewPassword();

		if (result.getException() == null) {
			Globals.getApplication().getPasswordCache().put(Globals.getApplication().getVaultDocument().getDatabase().getPath(), newPassword);
			Globals.getApplication().getVaultDocument().setDirty(false);
		} else {
			new AlertDialog.Builder(parameters.getFileActivity())
					.setTitle("Change Password")
					.setMessage("Cannot change password.")
					.setPositiveButton("OK", (dialog, which) -> {
						dialog.dismiss();

						parameters.getFileActivity().setEnabled(true);
						parameters.getFileActivity().closeActiveDocument();
					})
					.setCancelable(false)
					.create()
					.show();
		}
	}
}
