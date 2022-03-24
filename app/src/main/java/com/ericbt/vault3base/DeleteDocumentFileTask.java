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

public class DeleteDocumentFileTask extends
				AsyncTask<DeleteDocumentFileTaskParameters, Void, DeleteDocumentFileTaskResult> {
	private DeleteDocumentFileTaskParameters parameters;

	@Override
	protected DeleteDocumentFileTaskResult
							doInBackground(DeleteDocumentFileTaskParameters... params) {
		parameters = params[0];
		
		final DeleteDocumentFileTaskResult result = new DeleteDocumentFileTaskResult();

		try {
			if (parameters.getDocumentUri() != null) {
				final boolean deleted = DocumentFileUtils.removeDocumentAndTempFile(
						parameters.getActivity(), parameters.getDocumentUri());

				result.setDeleted(deleted);
			} else {
				result.setDeleted(FileUtils.deleteDatabaseFile(parameters.getDatabasePath()));
			}
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag,
					String.format("DeleteDocumentFileTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(DeleteDocumentFileTaskResult result) {
		if (!result.isDeleted() || result.getException() != null) {
			new AlertDialog.Builder(parameters.getActivity())
					.setTitle("Delete File")
					.setMessage(
							String.format(
									"Could not delete \"%s\"",
									parameters.getDatabasePath()))
					.setPositiveButton("OK", null)
					.create()
					.show();
		}

		final AsyncTaskActivity activity = parameters.getActivity();

		activity.enable(true);

		if (activity instanceof Vault3) {
			final Vault3 vault3 = (Vault3) activity;

			// Go back to FileActivity - there is nothing to do in the main activity now that
			// the active file as been closed.
			vault3.navigateToFilesActivity();
		}
	}
}
