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

package com.ericbt.vault3base.async_tasks.find_vault_files;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.ericbt.vault3base.FileActivity;
import com.ericbt.vault3base.StringLiterals;

import java.util.Arrays;
import java.util.Locale;

public class FindVaultFilesTask extends AsyncTask<FindVaultFilesTaskParameters, String, FindVaultFilesTaskResult> {
	private FindVaultFilesTaskParameters findVaultFilesTaskParameters;

	@Override
	protected FindVaultFilesTaskResult doInBackground(FindVaultFilesTaskParameters... params) {
		findVaultFilesTaskParameters = params[0];

		final FindVaultFilesTaskResult result = new FindVaultFilesTaskResult();

		try {
			final DocumentFile[] documentFiles = getFileList(
					findVaultFilesTaskParameters.getFileActivity(),
					findVaultFilesTaskParameters.getFolderUri());

			result.setDocumentFiles(documentFiles);
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("FindVaultFilesTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();

			result.setException(ex);
		}

		return result;
	}

	@Override
	protected void onPostExecute(FindVaultFilesTaskResult result) {
		findVaultFilesTaskParameters.getFileActivity().setEnabled(true);
		findVaultFilesTaskParameters.getFileActivity().setSearching(false);

		findVaultFilesTaskParameters.getFileActivity().updateFileList(result.getDocumentFiles());

		if (result.getException() != null) {
			new AlertDialog.Builder(findVaultFilesTaskParameters.getFileActivity())
					.setTitle(String.format("Search for %s Documents", StringLiterals.ProgramName))
					.setMessage("Cannot find documents.")
					.setPositiveButton("OK", null)
					.create()
					.show();
		}
	}
	
	private DocumentFile[] getFileList(FileActivity fileActivity, Uri folderUri) {
		final DocumentFile[] documentFiles =
				DocumentFile.fromTreeUri(fileActivity, folderUri).listFiles();

		return Arrays.stream(documentFiles).filter(
				documentFile ->
						documentFile.getName()
								.toLowerCase(Locale.ROOT)
								.endsWith(StringLiterals.FileType))
				.sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
				.toArray(DocumentFile[]::new);
	}
}
