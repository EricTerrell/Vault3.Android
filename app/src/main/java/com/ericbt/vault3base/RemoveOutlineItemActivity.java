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
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class RemoveOutlineItemActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Globals.getApplication().getVaultDocument() == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.remove_outline_item_dialog);

		setTitle(String.format("%s: Remove", StringLiterals.ProgramName));
		
		TextView message = findViewById(R.id.Message);
		
		message.setText(String.format("Remove %s?", getIntent().getExtras().getString(StringLiterals.Title)));

		Button okButton = findViewById(R.id.OKButton);

		okButton.setOnClickListener(v -> {
			setResult(RESULT_OK, getIntent());
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
