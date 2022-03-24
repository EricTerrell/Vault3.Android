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
import android.os.Bundle;

import com.ericbt.vault3base.async_tasks.upgrade_vault_document.UpgradeVaultDocumentTask;
import com.ericbt.vault3base.async_tasks.upgrade_vault_document.UpgradeVaultDocumentTaskParameters;

public class UpgradeVaultDocumentActivity extends Activity {
	public static final int RESULT_EXCEPTION = RESULT_FIRST_USER;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.upgrade_vault_document);
		
		setTitle("Upgrade Vault 3 Document");
		
		new UpgradeVaultDocumentTask().execute(new UpgradeVaultDocumentTaskParameters(getIntent().getExtras().getString(StringLiterals.DBPath), this));
	}

	@Override
	public void onBackPressed() {
	}
}
