package com.ericbt.vault3base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

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

        errorMessage = (TextView) findViewById(R.id.ErrorMessage);
        errorMessage.setText(passwordToShortErrorMessage);

        newPasswordEditText = (EditText) findViewById(R.id.NewPassword);
        newPasswordAgainEditText = (EditText) findViewById(R.id.NewPasswordAgain);
        
        InputFilter[] inputFilters = new InputFilter[] { PasswordUI.createPasswordInputFilter() };
        
        newPasswordEditText.setFilters(inputFilters);
        newPasswordAgainEditText.setFilters(inputFilters);

        requirePassword = (CheckBox) findViewById(R.id.RequirePassword);
        
        forceUppercase = (CheckBox) findViewById(R.id.ForceUppercasePassword);
        
        forceUppercase.setChecked(VaultPreferenceActivity.getForceUppercasePasswords());

        forceUppercase.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updatePasswordInputType();
			}
		});

		showPassword = (CheckBox) findViewById(R.id.ShowPassword);

		showPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				VaultPreferenceActivity.putShowPasswords(isChecked);
				updatePasswordInputType();
			}
		});

		showPassword.setChecked(VaultPreferenceActivity.getShowPasswords());

		updatePasswordInputType();
        
        requirePassword.setChecked(Globals.getApplication().getVaultDocument().isEncrypted());
        requirePassword.setText(String.format("Require a password to access %s", Globals.getApplication().getVaultDocument().getDatabase().getPath()));
        
        requirePassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updateGUIWhenChangesAreMade();
				}
		});
        
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
        
        okButton = (Button) findViewById(R.id.OKButton);
        
        okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				VaultPreferenceActivity.putUppercasePasswords(forceUppercase.isChecked());
				
				String newPassword = newPasswordEditText.getEditableText().toString().trim();
				
				if (forceUppercase.isChecked()) {
					newPassword = newPassword.toUpperCase();
				}
				
				Intent returnData = new Intent();
				returnData.putExtra(StringLiterals.NewPassword, newPassword);
				setResult(RESULT_OK, returnData);
				finish();
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
		EditText passwordEditTexts[] = new EditText[] { newPasswordEditText, newPasswordAgainEditText };

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
