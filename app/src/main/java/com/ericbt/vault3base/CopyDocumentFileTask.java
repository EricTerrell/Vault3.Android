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

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyDocumentFileTask extends AsyncTask<CopyDocumentFileTaskParameters, Void, CopyDocumentFileTaskResult> {
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		Log.i(StringLiterals.LogTag, "CopyDocumentFileTask: onPreExecute");
	}

	private CopyDocumentFileTaskParameters parameters;
	
	@Override
	protected CopyDocumentFileTaskResult doInBackground(CopyDocumentFileTaskParameters... params) {
		parameters = params[0];
		
		final CopyDocumentFileTaskResult result = new CopyDocumentFileTaskResult();
		
		try {
			final String destFilePath =
					copyDocumentFile(parameters.getFileActivity(), parameters.getSourceFileUri(),
					parameters.getSourceFileName());

			result.setDestFilePath(destFilePath);
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("CopyDocumentTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(CopyDocumentFileTaskResult result) {
		if (result.getException() != null) {
			new AlertDialog.Builder(parameters.getFileActivity())
					.setTitle("Copy Document")
					.setMessage(String.format("Cannot copy %s", parameters.getSourceFileUri().toString()))
					.setPositiveButton("OK", null)
					.create()
					.show();
		} else {
			// Cause temporary Vault 3 file to be loaded.
			final Intent returnData = new Intent();
			returnData.putExtra(StringLiterals.Action, StringLiterals.Load);
			returnData.putExtra(StringLiterals.DBPath, result.getDestFilePath());

			parameters.getFileActivity().setResult(RESULT_OK, returnData);
			parameters.getFileActivity().finish();
		}
	}

	private String copyDocumentFile(Context context, Uri sourceFileUri, String sourceFileName) throws IOException {
		Log.i(StringLiterals.LogTag, String.format("copyDocumentFile %s %s thread: %d",
				sourceFileUri.toString(),
				sourceFileName,
				Thread.currentThread().getId()));

		final String destFilePath = String.format("%s/%s",
				DocumentFileUtils.getTempFolderPath(context),
				sourceFileName);

		try (final InputStream inputStream = context.getContentResolver().openInputStream(sourceFileUri);
			 final OutputStream outputStream = new FileOutputStream(destFilePath)) {
			final long bytes = FileUtils.copy(inputStream, outputStream);

			Log.i(StringLiterals.LogTag, String.format("CopyDocumentFileTask: bytes: %,d", bytes));
		}

		return destFilePath;
	}
}
