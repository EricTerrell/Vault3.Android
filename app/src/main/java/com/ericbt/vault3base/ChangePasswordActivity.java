/*
  Vault 3
  (C) Copyright 2024, Eric Bergman-Terrell

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
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

public class ChangePasswordActivity extends Activity {
	private EditText newPasswordEditText, newPasswordAgainEditText;
	private TextView errorMessage;
	private final String passwordToShortErrorMessage = String.format("Password must be at least %d characters long.", CryptoUtils.getMinPasswordLength());
	private CheckBox requirePassword, forceUppercase, showPassword;
	private Button okButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Globals.getApplication().getVaultDocument() == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.change_password_dialog);

		setTitle(String.format("%s: Change Password", getString(R.string.app_name)));

		errorMessage = findViewById(R.id.ErrorMessage);
		errorMessage.setText(passwordToShortErrorMessage);

		newPasswordEditText = findViewById(R.id.NewPassword);
		newPasswordAgainEditText = findViewById(R.id.NewPasswordAgain);

		InputFilter[] inputFilters = new InputFilter[]{PasswordUI.createPasswordInputFilter()};

		newPasswordEditText.setFilters(inputFilters);
		newPasswordAgainEditText.setFilters(inputFilters);

		requirePassword = findViewById(R.id.RequirePassword);

		forceUppercase = findViewById(R.id.ForceUppercasePassword);

		forceUppercase.setChecked(VaultPreferenceActivity.getForceUppercasePasswords());

		forceUppercase.setOnCheckedChangeListener((buttonView, isChecked) -> updatePasswordInputType());

		showPassword = findViewById(R.id.ShowPassword);

		showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
			VaultPreferenceActivity.putShowPasswords(isChecked);
			updatePasswordInputType();
		});

		showPassword.setChecked(VaultPreferenceActivity.getShowPasswords());

		updatePasswordInputType();

		requirePassword.setChecked(Globals.getApplication().getVaultDocument().isEncrypted());
		requirePassword.setText(String.format("Require a password to access %s",
				new File(Globals.getApplication().getVaultDocument().getDatabase().getPath()).getName()));

		requirePassword.setOnCheckedChangeListener((buttonView, isChecked) -> updateGUIWhenChangesAreMade());

		newPasswordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateGUIWhenChangesAreMade();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		newPasswordAgainEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateGUIWhenChangesAreMade();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		okButton = findViewById(R.id.OKButton);

		okButton.setOnClickListener(v -> {
			VaultPreferenceActivity.putUppercasePasswords(forceUppercase.isChecked());

			String newPassword = newPasswordEditText.getEditableText().toString().trim();

			if (forceUppercase.isChecked()) {
				newPassword = newPassword.toUpperCase();
			}

			final Intent returnData = new Intent();
			returnData.putExtra(StringLiterals.NewPassword, newPassword);
			setResult(RESULT_OK, returnData);

			finish();
		});

		final Button cancelButton = findViewById(R.id.CancelButton);

		cancelButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);
			finish();
		});

		updateGUIWhenChangesAreMade();
	}

	private void updateGUIWhenChangesAreMade() {
		String newPassword = newPasswordEditText.getEditableText().toString().trim();
		String newPasswordAgain = newPasswordAgainEditText.getEditableText().toString().trim();
		
		if (forceUppercase.isChecked()) {
			newPassword = newPassword.toUpperCase();
			newPasswordAgain = newPasswordAgain.toUpperCase();
		}
		
		boolean error = false;
		
		if (requirePassword.isChecked()) {
			if (newPassword.length() < CryptoUtils.getMinPasswordLength() || newPasswordAgain.length() < CryptoUtils.getMinPasswordLength()) {
				errorMessage.setText(passwordToShortErrorMessage);
				error = true;
			}
			else if ((!forceUppercase.isChecked() && !newPassword.equals(newPasswordAgain)) || newPassword.compareToIgnoreCase(newPasswordAgain) != 0) {
				errorMessage.setText("Passwords must match.");
				error = true;
			}
		}

		newPasswordEditText.setEnabled(requirePassword.isChecked());
		newPasswordAgainEditText.setEnabled(requirePassword.isChecked());
		
		errorMessage.setVisibility(error ? View.VISIBLE : View.GONE);

		if (okButton != null) {
			okButton.setEnabled(!error);
		}
	}

	private void updatePasswordInputType() {
		EditText[] passwordEditTexts = new EditText[] { newPasswordEditText, newPasswordAgainEditText };

		for (EditText passwordEditText : passwordEditTexts) {
			PasswordUI.updatePasswordInputType(passwordEditText, forceUppercase.isChecked(), showPassword.isChecked());
		}

        updateGUIWhenChangesAreMade();
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
