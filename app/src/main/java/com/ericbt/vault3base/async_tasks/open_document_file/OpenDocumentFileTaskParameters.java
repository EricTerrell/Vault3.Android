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

package com.ericbt.vault3base.async_tasks.open_document_file;

import android.net.Uri;

import com.ericbt.vault3base.FileActivity;

public class OpenDocumentFileTaskParameters {
	private final Uri sourceFileUri;

	public Uri getSourceFileUri() {
		return sourceFileUri;
	}

	private final String sourceFileName;

	public String getSourceFileName() { return sourceFileName; }

	private final FileActivity fileActivity;

	public FileActivity getFileActivity() { return fileActivity; }

	public OpenDocumentFileTaskParameters(Uri sourceFileUri, String sourceFileName,
										  FileActivity fileActivity) {
		this.sourceFileUri = sourceFileUri;
		this.sourceFileName = sourceFileName;
		this.fileActivity = fileActivity;
	}
}
