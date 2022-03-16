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
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CloneDocumentFileTask extends AsyncTask<CloneDocumentFileTaskParameters, Void, CloneDocumentFileTaskResult> {
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		Log.i(StringLiterals.LogTag, "CloneDocumentFileTask: onPreExecute");
	}

	private CloneDocumentFileTaskParameters parameters;
	
	@Override
	protected CloneDocumentFileTaskResult doInBackground(CloneDocumentFileTaskParameters... params) {
		parameters = params[0];
		
		final CloneDocumentFileTaskResult result = new CloneDocumentFileTaskResult();
		
		try {
			cloneDocumentFile(
					parameters.getFileActivity(), parameters.getSourceDocumentUri(),
					parameters.getDestDocumentUri());

			if (parameters.getRemoveSourceDocument()) {
				DocumentFileUtils.removeDocumentAndTempFile(
						parameters.getFileActivity(),
						parameters.getSourceDocumentUri());
			}
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("CloneDocumentTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(CloneDocumentFileTaskResult result) {
		if (result.getException() != null) {
			new AlertDialog.Builder(parameters.getFileActivity())
					.setTitle("Copy Document")
					.setMessage(String.format("Cannot copy %s", parameters.getSourceDocumentUri().toString()))
					.setPositiveButton("OK", null)
					.create()
					.show();
		}

		if (parameters.getRemoveSourceDocument()) {
			parameters.getFileActivity().closeActiveDocument();
		}

		parameters.getFileActivity().programmaticSearch();
	}

	private void cloneDocumentFile(Context context, Uri sourceDocumentUri, Uri destDocumentUri) throws IOException {
		Log.i(StringLiterals.LogTag, String.format("cloneDocumentFile %s %s thread: %d",
				sourceDocumentUri.toString(),
				destDocumentUri,
				Thread.currentThread().getId()));

		try (final InputStream inputStream = context.getContentResolver().openInputStream(sourceDocumentUri);
			 final OutputStream outputStream = context.getContentResolver().openOutputStream(destDocumentUri)) {
			FileUtils.copy(inputStream, outputStream);
		}
	}
}
