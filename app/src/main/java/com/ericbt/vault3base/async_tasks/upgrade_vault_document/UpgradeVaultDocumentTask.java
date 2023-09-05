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

package com.ericbt.vault3base.async_tasks.upgrade_vault_document;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.UpgradeVaultDocumentActivity;
import com.ericbt.vault3base.VaultDocument;

public class UpgradeVaultDocumentTask extends AsyncTask<UpgradeVaultDocumentTaskParameters, Void, UpgradeVaultDocumentTaskResult> {
	private UpgradeVaultDocumentTaskParameters parameters;

	@Override
	protected UpgradeVaultDocumentTaskResult doInBackground(UpgradeVaultDocumentTaskParameters... params) {
		parameters = params[0];
		UpgradeVaultDocumentTaskResult result = new UpgradeVaultDocumentTaskResult();

		try {
			SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(parameters.getDBPath(), null);
			final VaultDocument vaultDocument = new VaultDocument(database);

			result = new UpgradeVaultDocumentTaskResult();

			vaultDocument.upgradeVaultDocument();
			
			vaultDocument.close();
		} catch (Throwable ex) {
			result.setException(ex);
		}

		return result;
	}

	@Override
	protected void onPostExecute(UpgradeVaultDocumentTaskResult result) {
		Intent intent = parameters.getUpgradeVaultDocumentActivity().getIntent();

		if (result.getException() == null) {
			parameters.getUpgradeVaultDocumentActivity().setResult(Activity.RESULT_OK, intent);
		} else {
			intent.putExtra(StringLiterals.ExceptionMessage, result.getException().getMessage());
			parameters.getUpgradeVaultDocumentActivity().setResult(UpgradeVaultDocumentActivity.RESULT_EXCEPTION, intent);
		}
		
		parameters.getUpgradeVaultDocumentActivity().finish();
	}
}
