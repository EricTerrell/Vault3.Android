/*
  Vault 3
  (C) Copyright 2022, Eric Bergman-Terrell
  
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

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

public class SaveChangesTask extends AsyncTask<SaveChangesTaskParameters, Void, SaveChangesTaskResult> {
	private SaveChangesTaskParameters parameters;
	
	@Override
	protected SaveChangesTaskResult doInBackground(SaveChangesTaskParameters... params) {
		parameters = params[0];
		
		final SaveChangesTaskResult result = new SaveChangesTaskResult();
		
		try {
			DocumentFileUtils.updateDocumentFile(
					parameters.getActivity(),
					parameters.getCurrentDocumentName(),
					parameters.getSourceFileUri());
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("CopyDocumentTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(SaveChangesTaskResult result) {
		if (result.getException() != null) {
			new AlertDialog.Builder(parameters.getActivity())
					.setTitle("Save Changes")
					.setMessage(String.format("Cannot save changes to %s: %s",
					parameters.getCurrentDocumentName(),
					result.getException().getMessage()))
					.setPositiveButton("OK", null)
					.create()
					.show();
		}
		else {
			Globals.getApplication().getVaultDocument().setDirty(false);

			parameters.getActivity().setEnabled(true);
		}
	}
}
