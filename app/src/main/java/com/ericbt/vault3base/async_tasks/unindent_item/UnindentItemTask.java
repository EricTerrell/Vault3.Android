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

package com.ericbt.vault3base.async_tasks.unindent_item;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.StringLiterals;

public class UnindentItemTask extends AsyncTask<UnindentItemTaskParameters, Void, UnindentItemTaskResult> {
	private UnindentItemTaskParameters parameters;
	
	@Override
	protected UnindentItemTaskResult doInBackground(UnindentItemTaskParameters... params) {
		parameters = params[0];
		
		UnindentItemTaskResult result = new UnindentItemTaskResult();
		
		try {
			int newParentID = Globals.getApplication().getVaultDocument().unIndent(parameters.getOutlineItem());
			result.setNewParentID(newParentID);
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("UnindentItemTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(UnindentItemTaskResult result) {
		parameters.getVault3Activity().setEnabled(true);

		if (result.getException() != null) {
			new AlertDialog.Builder(parameters.getVault3Activity())
					.setTitle("Unindent")
					.setMessage("Cannot unindent outline item.")
					.setPositiveButton("OK", null)
					.create()
					.show();
		} else {
			Globals.getApplication().getVaultDocument().setDirty(true);

			parameters.getVault3Activity().update(result.getNewParentID());
		}
	}
}
