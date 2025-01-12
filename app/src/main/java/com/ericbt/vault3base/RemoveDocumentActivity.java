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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class RemoveDocumentActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.remove_document_dialog);

		setTitle(String.format("%s: Remove Document", getString(R.string.app_name)));
		
		TextView message = findViewById(R.id.Message);
		
		final String documentUri = getIntent().getExtras().getString(StringLiterals.DocumentUri);
		message.setText(String.format("Remove %s?", DocumentFileUtils.getName(documentUri)));

		Button okButton = findViewById(R.id.OKButton);

		okButton.setOnClickListener(v -> {
			boolean removedCurrentDocument = VaultDocument.closeCurrentDocumentWhenDeletedOrRenamed(documentUri);

			Intent returnData = new Intent();
			returnData.putExtra(StringLiterals.Action, removedCurrentDocument ? StringLiterals.RemoveCurrentDocument : StringLiterals.RemoveDocument);
			returnData.putExtra(StringLiterals.DocumentUri, documentUri);
			setResult(RESULT_OK, returnData);
			finish();
		});
		
		Button cancelButton = findViewById(R.id.CancelButton);
		
		cancelButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);
			finish();
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
