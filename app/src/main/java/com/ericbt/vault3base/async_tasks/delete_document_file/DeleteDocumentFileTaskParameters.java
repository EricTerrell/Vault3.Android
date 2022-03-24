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

package com.ericbt.vault3base.async_tasks.delete_document_file;

import android.net.Uri;

import com.ericbt.vault3base.AsyncTaskActivity;

public class DeleteDocumentFileTaskParameters {
	private Uri documentUri;

	public Uri getDocumentUri() { return documentUri; }

	private final String databasePath;

	public String getDatabasePath() {
		return databasePath;
	}

	private AsyncTaskActivity activity;

	public AsyncTaskActivity getActivity() { return activity; }

	public DeleteDocumentFileTaskParameters(Uri documentUri, String databasePath, AsyncTaskActivity activity) {
		this.documentUri = documentUri;
		this.databasePath = databasePath;
		this.activity = activity;
	}
}
