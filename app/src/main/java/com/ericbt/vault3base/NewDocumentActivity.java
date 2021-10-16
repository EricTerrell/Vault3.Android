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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewDocumentActivity extends Activity {
	private Button okButton;
    private EditText newDocumentPath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.new_document_dialog);
		
        okButton = (Button) findViewById(R.id.OKButton);
        Button cancelButton = (Button) findViewById(R.id.CancelButton);
        
        TextView rootFolderPath = (TextView) findViewById(R.id.RootFolderPath);
        rootFolderPath.setText(String.format("%s/", VaultPreferenceActivity.getRootFolderPath()));
        
        newDocumentPath = (EditText) findViewById(R.id.NewDocumentPath);

        setTitle(String.format("%s: New Document", getString(R.string.app_name)));

        final TextView errorMessage = (TextView) findViewById(R.id.ErrorMessage);
        
        okButton.setOnClickListener(v -> {
			final String dbPath = String.format("%s/%s.vl3", VaultPreferenceActivity.getRootFolderPath(), newDocumentPath.getEditableText());

			File dbFile = new File(dbPath);

			if (dbFile.exists()) {
				AlertDialog.Builder fileExistsAlertDialogBuilder = new AlertDialog.Builder(NewDocumentActivity.this);
				fileExistsAlertDialogBuilder.setTitle(String.format("New %s Document", StringLiterals.ProgramName));
				fileExistsAlertDialogBuilder.setMessage(String.format("File %s already exists. Replace it with a new document?", dbFile));

				fileExistsAlertDialogBuilder.setPositiveButton("OK", (dialog, which) -> createDatabase(dbPath));

				fileExistsAlertDialogBuilder.setNegativeButton("Cancel", null);

				AlertDialog fileExistsAlertDialog = fileExistsAlertDialogBuilder.create();

				fileExistsAlertDialog.show();
			}
			else {
				Intent returnData = new Intent();
				returnData.putExtra(StringLiterals.FilePath, dbPath);
				setResult(RESULT_OK, returnData);
				createDatabase(dbPath);
			}
		});
        
        cancelButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);
			finish();
		});
        
        newDocumentPath.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String filePath = newDocumentPath.getEditableText().toString().trim();

				boolean error = filePath.length() == 0;
				okButton.setEnabled(!error);
				errorMessage.setVisibility(error ? View.VISIBLE : View.INVISIBLE);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void createDatabase(String dbPath) {
		Intent returnData = new Intent();
		returnData.putExtra(StringLiterals.FilePath, dbPath);
		setResult(RESULT_OK, returnData);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			finish();
			
			result = true;
		}
		
		return result;
	}

}
