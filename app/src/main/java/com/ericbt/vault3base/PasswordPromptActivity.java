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

package com.ericbt.vault3base;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

public class PasswordPromptActivity extends Activity {
    private EditText passwordEditText;
    private CheckBox forceUppercase, showPassword;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.password_prompt_dialog);
		
        passwordEditText = findViewById(R.id.Password);
        
        passwordEditText.setFilters(new InputFilter[] { PasswordUI.createPasswordInputFilter() });
        
        forceUppercase = findViewById(R.id.ForceUppercasePassword);
        
        forceUppercase.setChecked(VaultPreferenceActivity.getForceUppercasePasswords());
        
        forceUppercase.setOnCheckedChangeListener((buttonView, isChecked) -> PasswordUI.updatePasswordInputType(passwordEditText, forceUppercase.isChecked(), showPassword.isChecked()));

		showPassword = findViewById(R.id.ShowPassword);

		showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
			VaultPreferenceActivity.putShowPasswords(isChecked);
			PasswordUI.updatePasswordInputType(passwordEditText, forceUppercase.isChecked(), showPassword.isChecked());
		});

		showPassword.setChecked(VaultPreferenceActivity.getShowPasswords());

		PasswordUI.updatePasswordInputType(passwordEditText, forceUppercase.isChecked(), showPassword.isChecked());
        
        final TextView errorMessage = findViewById(R.id.ErrorMessage);
        errorMessage.setText(String.format("Password must contain at least %d characters.", CryptoUtils.getMinPasswordLength()));
        errorMessage.setBackgroundColor(Color.RED);
        
        setTitle(String.format("%s: Enter Password", getString(R.string.app_name)));
        
        TextView message = findViewById(R.id.Message);
        message.setText(String.format("Enter password for %s:", new File(getIntent().getExtras().getString(StringLiterals.DBPath)).getName()));
        
        final Button okButton = findViewById(R.id.OKButton);
        okButton.setEnabled(false);
        
        okButton.setOnClickListener(v -> {
			VaultPreferenceActivity.putUppercasePasswords(forceUppercase.isChecked());

			String passwordText = passwordEditText.getEditableText().toString().trim();

			if (forceUppercase.isChecked()) {
				passwordText = passwordText.toUpperCase();
			}

			try {
				final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(getIntent().getExtras().getString(StringLiterals.DBPath), null);
				final VaultDocument vaultDocument = new VaultDocument(database);

				if (vaultDocument.setPassword(passwordText)) {
					Intent returnData = new Intent();
					returnData.putExtra(StringLiterals.DBPath, vaultDocument.getDatabase().getPath());

					vaultDocument.close();

					returnData.putExtra(StringLiterals.Password, passwordText);
					setResult(RESULT_OK, returnData);
					finish();
				}
				else {
					new Builder(PasswordPromptActivity.this)
							.setTitle("Invalid Password")
							.setMessage(
									String.format(
										"You entered an incorrect password for %s.",
										new File(vaultDocument.getDatabase().getPath()).getName()))
							.setPositiveButton("OK", (dialog, which) -> passwordEditText.setText(StringLiterals.EmptyString))
							.create()
							.show();
				}
			}
			catch (Exception ex) {
				Log.e(StringLiterals.LogTag, "PasswordPromptActivity", ex);
			}
		});
        
        final Button cancelButton = findViewById(R.id.CancelButton);

        cancelButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);
			finish();
		});
        
		passwordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				final String passwordText = passwordEditText.getEditableText().toString().trim();
				
				final boolean error = passwordText.length() < CryptoUtils.getMinPasswordLength();
				okButton.setEnabled(!error);
				errorMessage.setVisibility(error ? View.VISIBLE : View.GONE);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		// Automatically submit when user types ENTER...
		passwordEditText.setOnKeyListener((v, keyCode, event) -> {
			boolean result = false;

			if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && okButton.isEnabled()) {
				okButton.performClick();
				result = true;
			}

			return result;
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
