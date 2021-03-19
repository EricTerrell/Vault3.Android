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

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

public class AddItemTask extends AsyncTask<AddItemTaskParameters, Void, AddItemTaskResult> {
	private AddItemTaskParameters parameters;
	
	@Override
	protected AddItemTaskResult doInBackground(AddItemTaskParameters... params) {
		parameters = params[0];
		
		AddItemTaskResult result = new AddItemTaskResult();
		
		try {
			Globals.getApplication().getVaultDocument().add(parameters.getNewOutlineItem(), parameters.getSelectedOutlineItem(), parameters.getAddAbove());
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("AddItemTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(AddItemTaskResult result) {
		parameters.getVault3Activity().setEnabled(true);
		
		if (result.getException() != null) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parameters.getVault3Activity());
			alertDialogBuilder.setTitle("Add");
			alertDialogBuilder.setMessage("Cannot add outline item.");
			alertDialogBuilder.setPositiveButton("OK", null);

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		} else {
			parameters.getVault3Activity().update(parameters.getSelectedOutlineItem().getParentId());
			
			// Tell the user how to add the next item.
			if (parameters.getDisplayHint()) {
				displayHint();
			}
		}
	}
	
	private void displayHint() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parameters.getVault3Activity());
		alertDialogBuilder.setTitle("Add");
		alertDialogBuilder.setMessage("To add more outline items, or to manipulate existing items, long-click an outline item or click the wrench icon.");
		alertDialogBuilder.setPositiveButton("OK", null);

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}
