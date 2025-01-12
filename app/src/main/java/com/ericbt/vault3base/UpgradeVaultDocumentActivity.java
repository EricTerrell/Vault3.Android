/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell
  
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

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ericbt.vault3base.async.workers.UpgradeVaultDocument;
import com.ericbt.vault3base.async.UpdateStatus;

import commonCode.VaultDocumentVersion;

public class UpgradeVaultDocumentActivity extends AsyncTaskActivity {
	public static final int RESULT_EXCEPTION = RESULT_FIRST_USER;

	private TextView upgradeWarning, percentText;

	private Button okButton, cancelButton;

	private LinearLayout buttons, percentCompleteUI;

	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.upgrade_vault_document);
		
		setTitle("Upgrade Vault 3 Document");

		final String upgradeWarningFormat = getText(R.string.upgrade_warning).toString();
		final String upgradeWarningText = String.format(upgradeWarningFormat,
				VaultDocumentVersion.getLatestVaultDocumentVersion());

		upgradeWarning = findViewById(R.id.upgrade_warning);
		upgradeWarning.setText(upgradeWarningText);

		buttons = findViewById(R.id.Buttons);
		percentCompleteUI = findViewById(R.id.percentCompleteUI);
		progressBar = findViewById(R.id.progressBar);
		percentText = findViewById(R.id.percentText);

		okButton = findViewById(R.id.OKButton);

		okButton.setOnClickListener(v -> {
			okButton.setEnabled(enabled);
			cancelButton.setEnabled(enabled);

			buttons.setVisibility(View.GONE);
			percentCompleteUI.setVisibility(View.VISIBLE);

			new UpgradeVaultDocument().upgradeVaultDocument(
					getIntent().getExtras().getString(StringLiterals.DBPath),
					getIntent().getExtras().getString(StringLiterals.Password),
					this);
		});

		cancelButton = findViewById(R.id.CancelButton);

		cancelButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);
			finish();
		});
	}

	@Override
	public void enable(boolean enabled) {
	}

	@Override
	public void onBackPressed() {
		// Do not allow user to hit back while the password is being changed.
	}

	public void finish(Throwable ex, String dbPath) {
		final String message = ex == null ? "Document upgraded.\n\nMake sure that all Vault 3 apps (desktop and Android) are updated." : "Cannot upgrade document.";
		final int result = ex == null ? RESULT_OK : RESULT_EXCEPTION;

		new AlertDialog.Builder(this)
				.setTitle("Upgrade Vault 3 Document")
				.setMessage(message)
				.setPositiveButton("OK", (dialog, which) -> {
					dialog.dismiss();

					final Intent returnData = new Intent();
					returnData.putExtra(StringLiterals.DBPath, dbPath);

					setResult(result, returnData);
					finish();
				})
				.setCancelable(false)
				.create()
				.show();
	}

	public void progressUpdate(UpdateStatus updateStatus) {
		progressBar.setProgress((int) updateStatus.getPercent());

		percentText.setText(String.format("%.2f %%", updateStatus.getPercent()));
	}
}
