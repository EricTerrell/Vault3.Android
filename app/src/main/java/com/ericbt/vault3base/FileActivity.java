/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell
  
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
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.ericbt.vault3base.async.workers.document_file_manipulation.CloneDocumentFile;
import com.ericbt.vault3base.async.workers.document_file_manipulation.CreateDatabase;
import com.ericbt.vault3base.async.workers.document_file_manipulation.DeleteDocumentFile;
import com.ericbt.vault3base.async.workers.document_file_manipulation.DeleteDocumentFileSilent;
import com.ericbt.vault3base.async.workers.document_file_manipulation.FindVaultFiles;
import com.ericbt.vault3base.async.workers.OpenDocumentFile;

public class FileActivity extends AsyncTaskActivity {
	private FileArrayAdapter arrayAdapter;
	private Button refreshButton, browseButton, newButton, passwordButton;
	private TextView currentDocument;
	private DocumentFile selectedFile;

	private static final int REMOVE_DOCUMENT = 1;
	private static final int NEW_DOCUMENT = 2;
	private static final int CHANGE_PASSWORD = 3;
	private static final int RENAME_DOCUMENT = 4;
	private static final int COPY_DOCUMENT = 5;
	private static final int BROWSE = 6;
	private static final int CHANGE_PASSWORD_PROCESSING = 7;

	private Uri folderUri;

	private boolean searching = false;

	public void setSearching(boolean searching) { this.searching = searching; }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(StringLiterals.LogTag, "FileActivity.onCreate");
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.file);

		setTitle(String.format("%s - File", getString(R.string.app_name)));

		refreshButton = findViewById(R.id.Refresh);
		refreshButton.setOnClickListener(v -> {
			searchForVaultFiles();
		});
		
		browseButton = findViewById(R.id.Browse);
		
		browseButton.setOnClickListener(v -> browseForFolder());
		
		arrayAdapter = new FileArrayAdapter(this, R.layout.file_list, R.id.FileListTextView);

		final ListView vaultFilesListView = findViewById(R.id.VaultFilesListView);
		vaultFilesListView.setEmptyView(findViewById(R.id.EmptyVaultFilesListView));

		registerForContextMenu(vaultFilesListView);
				
		vaultFilesListView.setAdapter(arrayAdapter);
		
		vaultFilesListView.setOnItemClickListener((parent, view, position, id) -> {
			conditionallyOpenNewDocument(parent, position);
		});

		newButton = findViewById(R.id.New);
		
		newButton.setOnClickListener(v -> createNewDocument(NEW_DOCUMENT));
		
		passwordButton = findViewById(R.id.Password);
		
		passwordButton.setOnClickListener(v -> {
			final Intent intent = new Intent(FileActivity.this, ChangePasswordActivity.class);
			startActivityForResult(intent, CHANGE_PASSWORD);
		});
		
		currentDocument = findViewById(R.id.CurrentDocument);

		update();

		try {
			folderUri = VaultPreferenceActivity.getFolderUri();

			enableForSearch(true);

			searchForVaultFiles();
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("FileActivity: Exception %s", ex.getMessage()));
			ex.printStackTrace();
		}

		setResult(RESULT_CANCELED);
	}

	private void browseForFolder() {
		final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

		// Fix for this bug: When app first browses and selects a folder, that folder becomes
		// the default. So the next time the user browses, the user must navigate up before
		// ANY folder an be selected.

		final String rootUri = getString(R.string.RootURI);
		intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse(rootUri));

		startActivityForResult(intent, BROWSE);
	}

	private void createNewDocument(int requestCode) {
		final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/text");
		intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, VaultPreferenceActivity.getFolderUri());

		startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		searchForVaultFiles();
	}

	private void searchForVaultFiles() {
		if (folderUri != null && !searching) {
			setSearching(true);

			arrayAdapter.clear();

			setEnabled(false);

			new FindVaultFiles().findVaultFiles(this, folderUri);
		}
	}
	
	public void enableForSearch(boolean enabled) {
		this.enabled = enabled;
		
		browseButton.setEnabled(enabled);

		final boolean folderSpecified = VaultPreferenceActivity.getFolderUri() != null;

		refreshButton.setEnabled(enabled && folderSpecified);

		arrayAdapter.setEnabled(enabled);
	}

	public void enable(boolean enabled) {
		getActionBar().setDisplayHomeAsUpEnabled(enabled);

		final boolean folderSpecified = VaultPreferenceActivity.getFolderUri() != null;

		refreshButton.setEnabled(enabled && folderSpecified);

		newButton.setEnabled(enabled && folderSpecified);
		
		passwordButton.setEnabled(enabled && Globals.getApplication().getVaultDocument() != null);

		arrayAdapter.setEnabled(enabled);

		browseButton.setEnabled(enabled);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		if (enabled && view.getId() == R.id.VaultFilesListView) {
			final AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
			selectedFile = arrayAdapter.getItem(adapterContextMenuInfo.position);
			
			final MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.file_context_menu, menu);

			menu.setHeaderTitle(selectedFile.getName());

			final VaultDocument vaultDocument = Globals.getApplication().getVaultDocument();

			if (vaultDocument != null) {
				final String currentFileName =
						new File(vaultDocument.getDatabase().getPath()).getName();
				final String selectedFileName =
						DocumentFileUtils.getFileName(selectedFile.getUri());

				// If the selected item corresponds to the current Vault 3 file,
				// Don't allow remove or rename. In this case user has to close the file first.
				if (selectedFileName.equals(currentFileName)) {
					final int[] menuItems = new int[]
							{ R.id.RemoveMenuItem, R.id.RenameMenuItem, R.id.CopyMenuItem };

					for (final int menuItem : menuItems) {
						menu.findItem(menuItem).setEnabled(false);
					}

					final String message = String.format(
							getString(R.string.current_doc_must_be_closed),
							currentFileName);

					Toast.makeText(this, message, Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = true;

		if (item.getItemId() == R.id.RemoveMenuItem) {
			final Intent intent = new Intent(this, RemoveDocumentActivity.class);
			intent.putExtra(StringLiterals.DocumentUri, selectedFile.getUri().toString());
			startActivityForResult(intent, REMOVE_DOCUMENT);
		} else if (item.getItemId() == R.id.CopyMenuItem) {
			createNewDocument(COPY_DOCUMENT);
		} else if (item.getItemId() == R.id.RenameMenuItem) {
			createNewDocument(RENAME_DOCUMENT);
		} else {
			result = super.onContextItemSelected(item);
		}

		return result;
	}

	@SuppressLint("WrongConstant")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REMOVE_DOCUMENT: {
				if (resultCode == RESULT_OK) {
					enable(false);

					final Uri documentUri =
							Uri.parse(data.getStringExtra(StringLiterals.DocumentUri));

					new DeleteDocumentFile()
							.deleteDocumentFile(documentUri, folderUri, null,this);
				}
				break;
			}

			case NEW_DOCUMENT: {
				if (resultCode == RESULT_OK) {
					final Uri databaseUri = data.getData();

					final String[] segments = databaseUri.getLastPathSegment().split("/");

					final String tempFileName = segments[segments.length - 1];

					if (tempFileName.toLowerCase(Locale.ROOT).endsWith(StringLiterals.FileType)) {
						final String tempFilePath = String.format("%s/%s",
								DocumentFileUtils.getTempFolderPath(this), tempFileName);

						setEnabled(false);

						new CreateDatabase().createDatabase(tempFilePath, databaseUri, folderUri,
								this);
					} else {
						displayFileTypeRequiredMessage();

						new DeleteDocumentFileSilent().deleteDocumentFileSilent(databaseUri, this);
					}
				}
			}
			break;

			case CHANGE_PASSWORD: {
				if (resultCode == RESULT_OK) {
					final Intent intent = new Intent(FileActivity.this,
							ChangePasswordProcessingActivity.class);

					intent.putExtra(StringLiterals.NewPassword,
							data.getStringExtra(StringLiterals.NewPassword));

					startActivityForResult(intent, CHANGE_PASSWORD_PROCESSING);
				}
			}
			break;

			case CHANGE_PASSWORD_PROCESSING: {
				if (resultCode != RESULT_OK) {
					closeActiveDocument();
				}
			}
			break;

			case RENAME_DOCUMENT: {
				copyOrRenameDocumentFile(data, resultCode, true);
			}
			break;

			case COPY_DOCUMENT: {
				copyOrRenameDocumentFile(data, resultCode, false);
			}
			break;

			case BROWSE: {
				if (data != null) {
					folderUri = data.getData();
					VaultPreferenceActivity.setFolderUri(folderUri);

					final int takeFlags = data.getFlags()
							& (Intent.FLAG_GRANT_READ_URI_PERMISSION
							| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

					getContentResolver().takePersistableUriPermission(folderUri, takeFlags);

					searchForVaultFiles();
				}
			}
			break;
		}
	}

	private void copyOrRenameDocumentFile(Intent data, int resultCode, boolean deleteSourceDocumentFile) {
		if (resultCode == RESULT_OK) {
			final Uri destDocumentFileUri = data.getData();

			if (destDocumentFileUri.toString().toLowerCase(Locale.ROOT).endsWith(StringLiterals.FileType)) {
				setEnabled(false);

				new CloneDocumentFile().cloneDocumentFile(selectedFile.getUri(),
						destDocumentFileUri,
						folderUri,
						this,
						deleteSourceDocumentFile);
			} else {
				displayFileTypeRequiredMessage();

				new DeleteDocumentFileSilent().deleteDocumentFileSilent(destDocumentFileUri,
						this);
			}
		}
	}

	private void update() {
		if (Globals.getApplication().getVaultDocument() != null) {
			currentDocument.setText(new File(Globals.getApplication().getVaultDocument().getDatabase().getPath()).getName());
		}
		else {
			currentDocument.setText("");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (enabled && item.getItemId() == android.R.id.home) {
			finish();
			
			result = true;
		}
		
		return result;
	}

    public void updateFileList(DocumentFile[] documentFiles) {
		arrayAdapter.clear();
		arrayAdapter.addAll(documentFiles);
	}

	private void displayFileTypeRequiredMessage() {
		new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage(String.format("File already exists, or file name is missing file type. File name must end in \"%s\"", StringLiterals.FileType))
				.setPositiveButton("OK", null)
				.create()
				.show();
	}

	public void closeActiveDocument() {
		// If we renamed the active file, need to close it.
		final Intent returnData = new Intent();
		returnData.putExtra(StringLiterals.Action, StringLiterals.Close);
		setResult(RESULT_OK, returnData);
		finish();
	}

	private void openNewDocument(AdapterView<?> parent, int position) {
		setEnabled(false);

		// Close Vault database
		final VaultDocument vaultDocument = Globals.getApplication().getVaultDocument();

		if (vaultDocument != null) {
			vaultDocument.close();

			Globals.getApplication().setVaultDocument(null);
		}

		final DocumentFile selectedFile = (DocumentFile) parent.getAdapter().getItem(position);

		VaultPreferenceActivity.setSelectedFileUri(selectedFile.getUri());

		new OpenDocumentFile().openDocumentFile(selectedFile.getUri(),
				selectedFile.getName(),
				this);
	}

	private void conditionallyOpenNewDocument(AdapterView<?> parent, int position) {
		final VaultApplication application = Globals.getApplication();

		if (application != null && application.getVaultDocument() != null &&
				Globals.getApplication().getVaultDocument().isDirty()) {
			new AlertDialog.Builder(this)
					.setTitle("Open New Document")
					.setMessage("Opening a new document will discard unsaved changes in current document. Open new document?")
					.setPositiveButton("Yes", (dialog, which) -> {
						openNewDocument(parent, position);
					})
					.setNegativeButton("No", null)
					.setCancelable(false)
					.create()
					.show();
		} else {
			openNewDocument(parent, position);
		}
	}
}
