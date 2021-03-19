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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RenameDocumentActivity extends Activity {
	private String sourceFilePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.rename_document_dialog);
        
        final EditText destinationPath = (EditText) findViewById(R.id.DestinationPath);

        TextView rootFolder = (TextView) findViewById(R.id.RootFolder);
        rootFolder.setText(String.format("%s/", VaultPreferenceActivity.getRootFolderPath()));
        
        TextView fileType = (TextView) findViewById(R.id.FileType);
        fileType.setText(StringLiterals.FileType);
        
		setTitle(String.format("%s: Rename Document", getString(R.string.app_name)));
		
		TextView message = (TextView) findViewById(R.id.Message);
		
		sourceFilePath = getIntent().getExtras().getString(StringLiterals.SourceFilePath);
		message.setText(String.format("Rename %s to:", sourceFilePath));
        
        final Button okButton = (Button) findViewById(R.id.OKButton);
        okButton.setEnabled(false);
        
        okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String destinationPathString = String.format("%s/%s%s", 
															 VaultPreferenceActivity.getRootFolderPath(), 
															 destinationPath.getEditableText().toString().trim(), 
															 StringLiterals.FileType); 

				Intent returnData = new Intent();
				returnData.putExtra(StringLiterals.OldFilePath, sourceFilePath);
				returnData.putExtra(StringLiterals.NewFilePath, destinationPathString);
				setResult(RESULT_OK, returnData);
				finish();
			}
		});
        
        final TextView errorMessage = (TextView) findViewById(R.id.ErrorMessage);

        destinationPath.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String destinationPathString = destinationPath.getEditableText().toString().trim();
				
				boolean enabled = destinationPathString.length() > 0;

				okButton.setEnabled(enabled);
				errorMessage.setVisibility(enabled ? View.GONE : View.VISIBLE);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

        Button cancelButton = (Button) findViewById(R.id.CancelButton);
        
        cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
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
