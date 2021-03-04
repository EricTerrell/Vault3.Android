/*
  Vault 3
  (C) Copyright 2015, Eric Bergman-Terrell
  
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

public class MoveItemTask extends AsyncTask<MoveItemTaskParameters, Void, MoveItemTaskResult> {
	private MoveItemTaskParameters parameters;
	
	@Override
	protected MoveItemTaskResult doInBackground(MoveItemTaskParameters... params) {
		parameters = params[0];
		
		MoveItemTaskResult result = new MoveItemTaskResult();
		
		try {
			Globals.getApplication().getVaultDocument().moveTo(parameters.getMovingOutlineItem(), parameters.getSelectedOutlineItem(), parameters.getPlaceAbove());
			parameters.getVault3Activity().setMovingOutlineItem(null);
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("MoveItemTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(MoveItemTaskResult result) {
		parameters.getVault3Activity().setEnabled(true);
		
		if (result.getException() != null) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parameters.getVault3Activity());
			alertDialogBuilder.setTitle("Move");
			alertDialogBuilder.setMessage("Cannot move outline item.");
			alertDialogBuilder.setPositiveButton("OK", null);

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		} else {
			parameters.getVault3Activity().update(parameters.getSelectedOutlineItem().getParentId());
		}
	}
}
