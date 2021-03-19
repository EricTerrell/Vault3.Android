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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FileActivity extends AsyncTaskActivity {
	private boolean searchRequested = true;
	private FileArrayAdapter arrayAdapter;
	private FindVaultFilesTask findVaultFilesTask;
	private ListView vaultFilesListView;
	private Button searchForVaultFilesButton, cancelButton, newButton, passwordButton, closeButton;
	private TextView currentDocument;
	private String selectedFilePath;
	
	private static final int REMOVE_DOCUMENT = 1;
	private static final int NEW_DOCUMENT = 2;
	private static final int CHANGE_PASSWORD = 3;
	private static final int RENAME_DOCUMENT = 4;
	private static final int COPY_DOCUMENT = 5;
	
	private void requestSearch() { searchRequested = true; }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(StringLiterals.LogTag, "FileActivity.onCreate");
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.file);

		setTitle(String.format("%s - File", getString(R.string.app_name)));

		searchForVaultFilesButton = (Button) findViewById(R.id.SearchForVaultFiles);
		searchForVaultFilesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createRootFolderIfNecessary();
				
				if (rootFolderExists()) {
					searchForVaultFiles();
				}
				else {
					promptUserToSpecifyRootFolder();
				}
			}
		});
		
		cancelButton = (Button) findViewById(R.id.Cancel);
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
		
		arrayAdapter = new FileArrayAdapter(this, R.layout.file_list, R.id.FileListTextView);
		
		vaultFilesListView = (ListView) findViewById(R.id.VaultFilesListView);
		vaultFilesListView.setEmptyView(findViewById(R.id.EmptyVaultFilesListView));

		registerForContextMenu(vaultFilesListView);
				
		vaultFilesListView.setAdapter(arrayAdapter);
		
		vaultFilesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!enabled) {
					cancel();
				}
				
				Intent returnData = new Intent();
				returnData.putExtra(StringLiterals.Action, StringLiterals.Load);
				returnData.putExtra(StringLiterals.DBPath, (String) vaultFilesListView.getItemAtPosition(position));	
				setResult(RESULT_OK, returnData);
				finish();
			}
		});

		newButton = (Button) findViewById(R.id.New);
		
		newButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createRootFolderIfNecessary();

                Intent intent = new Intent(FileActivity.this, NewDocumentActivity.class);
                startActivityForResult(intent, NEW_DOCUMENT);     
			}
		});
		
		passwordButton = (Button) findViewById(R.id.Password);
		
		passwordButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FileActivity.this, ChangePasswordActivity.class);
				startActivityForResult(intent, CHANGE_PASSWORD);
			}
		});
		
		currentDocument = (TextView) findViewById(R.id.CurrentDocument);

		closeButton = (Button) findViewById(R.id.Close);
		
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnData = new Intent();
				returnData.putExtra(StringLiterals.Action, StringLiterals.Close);
				setResult(RESULT_OK, returnData);
				finish();
			}
		});

		createRootFolderIfNecessary();
		
		update();
		
		setResult(RESULT_CANCELED);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (searchRequested) {
			searchRequested = false;
			
			searchForVaultFiles();
		}
	}

	private void searchForVaultFiles() {
		arrayAdapter.clear();

		setEnabled(false);
		
		findVaultFilesTask = new FindVaultFilesTask();
		findVaultFilesTask.execute(new FindVaultFilesTaskParameters(this));
	}
	
	public void update(String filePath) {
		arrayAdapter.add(filePath);
	}
	
	public void enableForSearch(boolean enabled) {
		this.enabled = enabled;
		
		cancelButton.setEnabled(!enabled);
		searchForVaultFilesButton.setEnabled(enabled);
		arrayAdapter.setEnabled(enabled);
	}

	public void enable(boolean enabled) {
		getActionBar().setDisplayHomeAsUpEnabled(enabled);

		searchForVaultFilesButton.setEnabled(enabled);
		newButton.setEnabled(enabled);
		
		passwordButton.setEnabled(enabled && Globals.getApplication().getVaultDocument() != null);
		closeButton.setEnabled(enabled && Globals.getApplication().getVaultDocument() != null);

		arrayAdapter.setEnabled(enabled);
		
		cancelButton.setEnabled(!enabled);
	}
	
	private void cancel() {
		if (findVaultFilesTask != null) {
			findVaultFilesTask.cancel(true);
			findVaultFilesTask = null;
		}
		
		setEnabled(true);
	}
	
	private void createRootFolderIfNecessary() {
		String rootFolderPath = VaultPreferenceActivity.getRootFolderPath();
		
		File rootFolder = new File(rootFolderPath);
		
		if (!rootFolder.exists()) {
			rootFolder.mkdirs();
		}
	}
	
	private boolean rootFolderExists() {
		String rootFolderPath = VaultPreferenceActivity.getRootFolderPath();
		
		File rootFolder = new File(rootFolderPath);
		
		return rootFolder.exists();
	}
	
	private void promptUserToSpecifyRootFolder() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Cannot Search");
		alertDialogBuilder.setMessage("Turn off USB storage if it is on.\r\n\r\nPlease specify the Root Folder that will contain all your Vault 3 documents if you have not already done so.");
		
		alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(FileActivity.this, VaultPreferenceActivity.class));
			}
		});

		alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		alertDialogBuilder.create().show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		if (view.getId() == R.id.VaultFilesListView) {
			AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
			selectedFilePath = arrayAdapter.getItem(adapterContextMenuInfo.position);
			
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.file_context_menu, menu);

			menu.setHeaderTitle(new File(selectedFilePath).getName());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = true;

		if (item.getItemId() == R.id.RemoveMenuItem) {
			Intent intent = new Intent(this, RemoveDocumentActivity.class);
			intent.putExtra(StringLiterals.FilePath, selectedFilePath);
			startActivityForResult(intent, REMOVE_DOCUMENT);
		} else if (item.getItemId() == R.id.CopyMenuItem) {
			Intent intent = new Intent(this, CopyDocumentActivity.class);
			intent.putExtra(StringLiterals.SourceFilePath, selectedFilePath);
			startActivityForResult(intent, COPY_DOCUMENT);
		} else if (item.getItemId() == R.id.RenameMenuItem) {
			Intent intent = new Intent(this, RenameDocumentActivity.class);
			intent.putExtra(StringLiterals.SourceFilePath, selectedFilePath);
			startActivityForResult(intent, RENAME_DOCUMENT);
		} else {
			result = super.onContextItemSelected(item);
		}

		return result;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
		case REMOVE_DOCUMENT: {
			if (resultCode == RESULT_OK) {
				String action = data.getStringExtra(StringLiterals.Action);
				String filePath = data.getStringExtra(StringLiterals.FilePath);
	
				File file = new File(filePath);
				
				if (file.delete()) {
					String journalPath = String.format("%s-journal", filePath);
					file = new File(journalPath);
					file.delete();
					
					if (StringLiterals.RemoveCurrentDocument.equals(action)) {
							Intent returnData = new Intent();
							returnData.putExtra(StringLiterals.Action, StringLiterals.RemoveCurrentDocument);
							setResult(RESULT_OK, returnData);
							finish();
					}
					else {
						update();
						
						requestSearch();
					}
				}
				else {
					Log.e(StringLiterals.LogTag, String.format("FileActivity.onActivityResult: Cannot delete %s", filePath));
				}
			}
		break;
		}
		
		case NEW_DOCUMENT: {
			if (resultCode == RESULT_OK) {
				setEnabled(false);
				
				new CreateDatabaseTask().execute(new CreateDatabaseTaskParameters(data.getStringExtra(StringLiterals.FilePath), this));
			}
		}
		break;
		
		case CHANGE_PASSWORD: {
			if (resultCode == RESULT_OK) {
				setEnabled(false);
				
				new ChangePasswordTask().execute(new ChangePasswordTaskParameters(data.getStringExtra(StringLiterals.NewPassword), this));
			}
		}
		break;
		
		case RENAME_DOCUMENT: {
			if (resultCode == RESULT_OK) {
				String oldFilePath = data.getExtras().getString(StringLiterals.OldFilePath);
				String newFilePath = data.getExtras().getString(StringLiterals.NewFilePath);
				
				File sourceFile = new File(oldFilePath);
				boolean renamed = sourceFile.renameTo(new File(newFilePath));
				
				if (renamed) {
					if (Globals.getApplication().getVaultDocument() != null && Globals.getApplication().getVaultDocument().getDatabase().getPath().equals(oldFilePath)) {
						Intent returnData = new Intent();
						returnData.putExtra(StringLiterals.Action, StringLiterals.Close);
						setResult(RESULT_OK, returnData);
						finish();
					}
					else {
						update();
						
						requestSearch();
					}
				}
				else {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
					alertDialogBuilder.setTitle("Rename Document");
					alertDialogBuilder.setMessage(String.format("Cannot rename %s to %s", oldFilePath, newFilePath));
					alertDialogBuilder.setPositiveButton("OK", null);
					
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				}
				
			}
		}
		break;
		
		case COPY_DOCUMENT: {
			if (resultCode == RESULT_OK) {
				setEnabled(false);
				
				CopyDocumentTaskParameters copyDocumentTaskParameters = 
						new CopyDocumentTaskParameters(data.getStringExtra(StringLiterals.SourceFilePath), 
													   data.getStringExtra(StringLiterals.DestinationFilePath), 
													   this);
				new CopyDocumentTask().execute(copyDocumentTaskParameters);
			}
		}
		break;
		}
	}
	
	public void loadNewDocument(String dbPath) {
		Intent returnData = new Intent();
		returnData.putExtra(StringLiterals.Action, StringLiterals.Load);
		returnData.putExtra(StringLiterals.DBPath, dbPath);	
		setResult(RESULT_OK, returnData);
		finish();
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

    /**
     * Automatically do a search after a file has been copied, so that the new file is displayed.
     */
    public void programmaticSearch() {
        if (rootFolderExists()) {
            searchForVaultFiles();
        }
    }

}
