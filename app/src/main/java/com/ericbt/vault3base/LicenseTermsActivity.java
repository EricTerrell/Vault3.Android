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
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;

public class LicenseTermsActivity extends Activity {
	private boolean allowCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		allowCancel = getIntent().getBooleanExtra(StringLiterals.AllowCancel, true);

		if (allowCancel) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		setContentView(R.layout.license_terms_dialog);

		setTitle(String.format("%s: License Terms", StringLiterals.ProgramName));

		final RadioButton acceptLicenseTerms = findViewById(R.id.AcceptLicenseTerms);
		acceptLicenseTerms.setChecked(VaultPreferenceActivity.getUserAcceptedTerms());

		final RadioButton rejectLicenseTerms = findViewById(R.id.RejectLicenseTerms);
		rejectLicenseTerms.setChecked(!VaultPreferenceActivity.getUserAcceptedTerms());

		final Button okButton = findViewById(R.id.OKButton);

		okButton.setOnClickListener(v -> {
			final boolean userAcceptedTerms = acceptLicenseTerms.isChecked();

			VaultPreferenceActivity.putUserAcceptedTerms(userAcceptedTerms);

			if (!userAcceptedTerms) {
				new AlertDialog.Builder(LicenseTermsActivity.this)
						.setTitle(String.format("Rejected %s License Terms", StringLiterals.ProgramName))
						.setMessage(String.format("You rejected the %s license terms. Please uninstall %s immediately.", StringLiterals.ProgramName, StringLiterals.ProgramName))
						.setPositiveButton("OK", (dialog, which) -> {
							dialog.dismiss();

							finish();

							final Intent intent = new Intent(getApplicationContext(), Vault3.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra(StringLiterals.Exit, true);
							startActivity(intent);
						})
						.setCancelable(false)
						.create()
						.show();
			} else {
				finish();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		if (allowCancel) {
			super.onBackPressed();
		}
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
