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

public class EditItemActivity extends Activity {
	private Button okButton;
    private EditText title, text;
	private TextView errorMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        EditMode.setEditMode(this);

		if (Globals.getApplication().getVaultDocument() == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.edit_item_dialog);
		
		setTitle(String.format("%s - Edit", getString(R.string.app_name)));

        errorMessage = findViewById(R.id.ErrorMessage);
        
		title = findViewById(R.id.Title);
		text = findViewById(R.id.Text);

        title.setText(getIntent().getExtras().getString(StringLiterals.Title));
        text.setText(getIntent().getExtras().getString(StringLiterals.Text));
        
        okButton = findViewById(R.id.OKButton);
        
        okButton.setOnClickListener(v -> {
			String currentTitleValue = title.getEditableText().toString();
			String currentTextValue = text.getEditableText().toString();

			if (!currentTitleValue.equals(getIntent().getExtras().getString(StringLiterals.Title)) ||
				!currentTextValue.equals(getIntent().getExtras().getString(StringLiterals.Text))) {
				Intent returnData = new Intent();
				returnData.putExtra(StringLiterals.Title, currentTitleValue);
				returnData.putExtra(StringLiterals.Text, currentTextValue);
				returnData.putExtra(StringLiterals.OutlineItemId, getIntent().getExtras().getInt(StringLiterals.OutlineItemId));
				returnData.putExtra(StringLiterals.OutlineItemParentId, getIntent().getExtras().getInt(StringLiterals.OutlineItemParentId));
				setResult(RESULT_OK, returnData);

				Globals.getApplication().getVaultDocument().setDirty(true);
			}
			else {
				setResult(RESULT_CANCELED);
			}

			finish();
		});

        Button cancelButton = findViewById(R.id.CancelButton);
        
        cancelButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);
			finish();
		});
        
		okButton.setEnabled(false);
		
		title.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateUI();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		text.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateUI();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		updateUI();
	}
	
	private void updateUI() {
		String titleText = title.getEditableText().toString().trim();

		boolean error = titleText.length() == 0;
		EditItemActivity.this.okButton.setEnabled(!error);
		errorMessage.setVisibility(error ? View.VISIBLE : View.GONE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			if (changesExist()) {
				new Prompt(this, "Edit", "Discard Changes?", "Yes", "No", AlertDialog.BUTTON_POSITIVE).onBackPressed();
			}
			else {
				finish();
				
				result = true;
			}
		}
		
		return result;
	}

	/**
	 * Prompt the user before discarding changes.
	 */
	@Override
	public void onBackPressed() {
		if (changesExist()) {
			new Prompt(this, "Edit", "Discard Changes?", "Yes", "No", AlertDialog.BUTTON_POSITIVE).onBackPressed();
		}
		else {
			super.onBackPressed();
		}
	}

	/**
	 * Determine if changes have been made that might need to be saved.
	 * @return true if changes need to be saved
	 */
	private boolean changesExist() {
		return !getIntent().getExtras().getString(StringLiterals.Title).equals(title.getEditableText().toString()) ||
			   !getIntent().getExtras().getString(StringLiterals.Text).equals(text.getEditableText().toString());
	}
}
