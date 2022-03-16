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

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

public class MoveItemUpTask extends AsyncTask<MoveItemUpTaskParameters, Void, MoveItemUpTaskResult> {
	private MoveItemUpTaskParameters parameters;
	
	@Override
	protected MoveItemUpTaskResult doInBackground(MoveItemUpTaskParameters... params) {
		parameters = params[0];
		
		MoveItemUpTaskResult result = new MoveItemUpTaskResult();
		
		try {
			Globals.getApplication().getVaultDocument().moveUp(parameters.getOutlineItem());
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("MoveItemUpTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(MoveItemUpTaskResult result) {
		parameters.getVault3Activity().setEnabled(true);
		
		if (result.getException() != null) {
			new AlertDialog.Builder(parameters.getVault3Activity())
					.setTitle("Move Up")
					.setMessage("Cannot move outline item up.")
					.setPositiveButton("OK", null)
					.create()
					.show();
		} else {
			Globals.getApplication().getVaultDocument().setDirty(true);

			parameters.getVault3Activity().update();
		}
	}
}
