/*
  Vault 3
  (C) Copyright 2015, Eric Bergman-Terrell
  
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
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
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
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class PasswordPromptActivity extends Activity {
    private EditText passwordEditText;
    private CheckBox forceUppercase, showPassword;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.password_prompt_dialog);
		
        passwordEditText = (EditText) findViewById(R.id.Password);
        
        passwordEditText.setFilters(new InputFilter[] { PasswordUI.createPasswordInputFilter() });
        
        forceUppercase = (CheckBox) findViewById(R.id.ForceUppercasePassword);
        
        forceUppercase.setChecked(VaultPreferenceActivity.getForceUppercasePasswords());
        
        forceUppercase.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				PasswordUI.updatePasswordInputType(passwordEditText, forceUppercase.isChecked(), showPassword.isChecked());
			}
		});

		showPassword = (CheckBox) findViewById(R.id.ShowPassword);

		showPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				VaultPreferenceActivity.putShowPasswords(isChecked);
				PasswordUI.updatePasswordInputType(passwordEditText, forceUppercase.isChecked(), showPassword.isChecked());
			}
		});

		showPassword.setChecked(VaultPreferenceActivity.getShowPasswords());

		PasswordUI.updatePasswordInputType(passwordEditText, forceUppercase.isChecked(), showPassword.isChecked());
        
        final TextView errorMessage = (TextView) findViewById(R.id.ErrorMessage);
        errorMessage.setText(String.format("Password must contain at least %d characters.", CryptoUtils.getMinPasswordLength()));
        errorMessage.setBackgroundColor(Color.RED);
        
        setTitle(String.format("%s: Enter Password", getString(R.string.app_name)));
        
        TextView message = (TextView) findViewById(R.id.Message);
        message.setText(String.format("Enter password for %s", getIntent().getExtras().getString(StringLiterals.DBPath)));
        
        final Button okButton = (Button) findViewById(R.id.OKButton);
        okButton.setEnabled(false);
        
        okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				VaultPreferenceActivity.putUppercasePasswords(forceUppercase.isChecked());
				
				String passwordText = passwordEditText.getEditableText().toString().trim();
				
				if (forceUppercase.isChecked()) {
					passwordText = passwordText.toUpperCase();
				}
				
				try {
					SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(getIntent().getExtras().getString(StringLiterals.DBPath), null);
					VaultDocument vaultDocument = new VaultDocument(database);
					
					if (vaultDocument.setPassword(passwordText)) {
						Intent returnData = new Intent();
						returnData.putExtra(StringLiterals.DBPath, vaultDocument.getDatabase().getPath());

						vaultDocument.close();

						returnData.putExtra(StringLiterals.Password, passwordText);
						setResult(RESULT_OK, returnData);
						finish();
					}
					else {
				        AlertDialog.Builder alertDialogBuilder = new Builder(PasswordPromptActivity.this);
				        
				        alertDialogBuilder.setTitle("Invalid Password");
				        alertDialogBuilder.setMessage(String.format("You entered an incorrect password for %s.", vaultDocument.getDatabase().getPath()));
				        
				        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								passwordEditText.setText("");
							}
						});
				        
				        alertDialogBuilder.create().show();
					}
				}
				catch (Exception ex) {
					Log.e(StringLiterals.LogTag, "PasswordPromptActivity", ex);
				}
			}
		});
        
        final Button cancelButton = (Button) findViewById(R.id.CancelButton);

        cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
        
		passwordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String passwordText = passwordEditText.getEditableText().toString().trim();
				
				boolean error = passwordText.length() < CryptoUtils.getMinPasswordLength();
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
		passwordEditText.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		    	boolean result = false;
		    	
		        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && okButton.isEnabled()) {
		        	okButton.performClick();
		        	result = true;
		        }
		        
		        return result;
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
