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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;

public class LicenseTermsActivity extends Activity {
	private boolean allowCancel;
	private AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		allowCancel = getIntent().getBooleanExtra(StringLiterals.AllowCancel, true);

		if (allowCancel) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		setContentView(R.layout.license_terms_dialog);
		
		setTitle(String.format("%s: License Terms", StringLiterals.ProgramName));

		final RadioButton acceptLicenseTerms = (RadioButton) findViewById(R.id.AcceptLicenseTerms);
		acceptLicenseTerms.setChecked(VaultPreferenceActivity.getUserAcceptedTerms());
		
		final RadioButton rejectLicenseTerms = (RadioButton) findViewById(R.id.RejectLicenseTerms);
		rejectLicenseTerms.setChecked(!VaultPreferenceActivity.getUserAcceptedTerms());
		
		Button okButton = (Button) findViewById(R.id.OKButton);

		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final boolean userAcceptedTerms = acceptLicenseTerms.isChecked();
				
				VaultPreferenceActivity.putUserAcceptedTerms(userAcceptedTerms);
				
				if (!userAcceptedTerms) {
					AlertDialog.Builder userRejectedTermsDialogBuilder = new AlertDialog.Builder(LicenseTermsActivity.this);
					userRejectedTermsDialogBuilder.setTitle(String.format("Rejected %s License Terms", StringLiterals.ProgramName));
					userRejectedTermsDialogBuilder.setMessage(String.format("You rejected the %s license terms. Please uninstall %s immediately.", StringLiterals.ProgramName, StringLiterals.ProgramName));
					userRejectedTermsDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							alertDialog.dismiss();
							
							finish();
							
							Intent intent = new Intent(getApplicationContext(), Vault3.class);
						    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						    intent.putExtra("EXIT", true);
						    startActivity(intent);
						}
					});
					
					userRejectedTermsDialogBuilder.setCancelable(false);
					
					alertDialog = userRejectedTermsDialogBuilder.create();
					alertDialog.show();
				}
				else {
					finish();
				}
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
