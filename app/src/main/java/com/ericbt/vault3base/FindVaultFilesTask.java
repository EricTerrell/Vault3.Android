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

import java.io.File;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

public class FindVaultFilesTask extends AsyncTask<FindVaultFilesTaskParameters, String, FindVaultFilesTaskResult> {
	private FindVaultFilesTaskParameters findVaultFilesTaskParameters;
	
	@Override
	protected FindVaultFilesTaskResult doInBackground(FindVaultFilesTaskParameters... params) {
		findVaultFilesTaskParameters = params[0];
		
		FindVaultFilesTaskResult result = new FindVaultFilesTaskResult();
		
		try {
			getVaultFilePaths(VaultPreferenceActivity.getRootFolderPath());
		}
		catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("FindVaultFilesTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		findVaultFilesTaskParameters.getFileActivity().update(values[0]);
	}

	@Override
	protected void onCancelled() {
		findVaultFilesTaskParameters.getFileActivity().enableForSearch(true);
	}

	@Override
	protected void onPostExecute(FindVaultFilesTaskResult result) {
		findVaultFilesTaskParameters.getFileActivity().setEnabled(true);
		
		if (result.getException() != null) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(findVaultFilesTaskParameters.getFileActivity());
			alertDialogBuilder.setTitle(String.format("Search for %s Documents", StringLiterals.ProgramName));
			alertDialogBuilder.setMessage("Cannot find documents.");
			alertDialogBuilder.setPositiveButton("OK", null);
			
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}
	
	private void getVaultFilePaths(String rootFolderPath) {
		File rootFolder = new File(rootFolderPath);
		
		if (rootFolder.isDirectory()) {
			File[] list = rootFolder.listFiles();
			
			if (list != null && list.length > 0) {
				for (File child : list) {
					if (!isCancelled()) {
						if (child.isFile()) {
							String fileType = FileUtils.getFileType(child);
							
							if (fileType != null && fileType.equals(VaultDocument.VAULTFILETYPE)) {
								publishProgress(child.getAbsolutePath());
							}
						}
						else if (child.isDirectory()) {
							getVaultFilePaths(child.getAbsolutePath());
						}
					}
				}
			}
		}
	}
}
