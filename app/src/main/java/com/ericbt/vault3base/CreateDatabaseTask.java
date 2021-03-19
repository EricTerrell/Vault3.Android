/*
  Vault 3
  (C) Copyright 2021, Eric Bergman-Terrell
  
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

public class CreateDatabaseTask extends AsyncTask<CreateDatabaseTaskParameters, Void, CreateDatabaseTaskResult> {
	private CreateDatabaseTaskParameters parameters;
	
	@Override
	protected CreateDatabaseTaskResult doInBackground(CreateDatabaseTaskParameters... params) {
		parameters = params[0];
		
		String dbPath = parameters.getdbPath();
		
		CreateDatabaseTaskResult result = new CreateDatabaseTaskResult(dbPath);
		
		try {
			VaultDocument.createNewVaultDocument(dbPath);
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
		
		if (createDatabaseTaskResult.getException() == null) {
			parameters.getFileActivity().loadNewDocument(createDatabaseTaskResult.getDbPath());
		}
		else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parameters.getFileActivity());
			alertDialogBuilder.setTitle(String.format("New %s Document", StringLiterals.ProgramName));
			alertDialogBuilder.setMessage(String.format("Cannot create %s.", createDatabaseTaskResult.getDbPath()));
			alertDialogBuilder.setPositiveButton("OK", null);
			
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}
}
