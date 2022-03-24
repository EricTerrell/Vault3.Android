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

import java.io.File;

public class CreateDatabaseTask extends AsyncTask<CreateDatabaseTaskParameters, Void, CreateDatabaseTaskResult> {
	private CreateDatabaseTaskParameters parameters;
	
	@Override
	protected CreateDatabaseTaskResult doInBackground(CreateDatabaseTaskParameters... params) {
		parameters = params[0];
		
		final String dbPath = parameters.getdbPath();
		
		final CreateDatabaseTaskResult result = new CreateDatabaseTaskResult(dbPath);
		
		try {
			VaultDocument.createNewVaultDocument(dbPath);

			DocumentFileUtils.updateDocumentFile(
					parameters.getFileActivity(),
					new File(dbPath).getName(),
					parameters.getSourceFileUri());

			// We don't need to keep the temporary or journal files.
			FileUtils.deleteDatabaseFile(dbPath);
		}
		catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("CreateDatabaseTask: cannot create db file %s exception %s", dbPath, ex.getMessage()));
			
			result.setException(ex);
		}

		return result;
	}

	@Override
	protected void onPostExecute(CreateDatabaseTaskResult createDatabaseTaskResult) {
		parameters.getFileActivity().setEnabled(true);

		parameters.getFileActivity().programmaticSearch();

		if (createDatabaseTaskResult.getException() != null) {
			new AlertDialog.Builder(parameters.getFileActivity())
					.setTitle(String.format("New %s Document", StringLiterals.ProgramName))
					.setMessage(String.format("Cannot create %s.", createDatabaseTaskResult.getDbPath()))
					.setPositiveButton("OK", null)
					.create()
					.show();
		}
	}
}
