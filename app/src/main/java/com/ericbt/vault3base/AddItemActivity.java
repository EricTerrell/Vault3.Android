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
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class AddItemActivity extends Activity {
	private Button okButton;
    private EditText title, text;
	
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

		setContentView(R.layout.add_item_dialog);
		
        final TextView errorMessage = (TextView) findViewById(R.id.ErrorMessage);
        
		title = (EditText) findViewById(R.id.Title);
		text = (EditText) findViewById(R.id.Text);

		final RadioButton addAboveRadioButton = (RadioButton) findViewById(R.id.AddAboveSelectedItem);
		
        setTitle(String.format("%s: Add", StringLiterals.ProgramName));
        
        okButton = (Button) findViewById(R.id.OKButton);
        
        okButton.setOnClickListener(v -> {
			Intent returnData = new Intent();
			returnData.putExtra(StringLiterals.Title, title.getEditableText().toString());
			returnData.putExtra(StringLiterals.Text, text.getEditableText().toString());
			returnData.putExtra(StringLiterals.AddAbove, addAboveRadioButton.isChecked());
			returnData.putExtra(StringLiterals.DisplayHint, getIntent().getBooleanExtra(StringLiterals.DisplayHint, false));
			returnData.putExtra(StringLiterals.SelectedOutlineItemId, getIntent().getIntExtra(StringLiterals.SelectedOutlineItemId, 0));
			returnData.putExtra(StringLiterals.SelectedOutlineItemSortOrder, getIntent().getIntExtra(StringLiterals.SelectedOutlineItemSortOrder, 0));
			returnData.putExtra(StringLiterals.SelectedOutlineItemParentId, getIntent().getIntExtra(StringLiterals.SelectedOutlineItemParentId, 1));
			setResult(RESULT_OK, returnData);

			finish();
		});

        Button cancelButton = (Button) findViewById(R.id.CancelButton);
        
        cancelButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);
			finish();
		});
        
        boolean aboveOrBelowPrompt = getIntent().getBooleanExtra(StringLiterals.AboveOrBelowPrompt, false);
        
        if (!aboveOrBelowPrompt) {
        	RadioGroup selectAboveOrBelowSelectedItem = (RadioGroup) findViewById(R.id.AddAboveOrBelowSelectedItem);
        	selectAboveOrBelowSelectedItem.setVisibility(View.GONE);
        }
        
		okButton.setEnabled(false);
		
		title.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String titleText = title.getEditableText().toString().trim();

				boolean error = titleText.length() == 0;
				AddItemActivity.this.okButton.setEnabled(!error);
				errorMessage.setVisibility(error ? View.VISIBLE : View.GONE);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			if (changesExist()) {
				new Prompt(this, "Add", "Discard Changes?", "Yes", "No", AlertDialog.BUTTON_POSITIVE).onBackPressed();
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
			new Prompt(this, "Add", "Discard Changes?", "Yes", "No", AlertDialog.BUTTON_POSITIVE).onBackPressed();
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
		return title.getEditableText().toString().trim().length() > 0 || text.getEditableText().toString().trim().length() > 0;
	}
}
