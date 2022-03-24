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

package com.ericbt.vault3base.async_tasks.indent_item;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.StringLiterals;

public class IndentItemTask extends AsyncTask<IndentItemTaskParameters, Void, IndentItemTaskResult> {
	private IndentItemTaskParameters parameters;
	
	@Override
	protected IndentItemTaskResult doInBackground(IndentItemTaskParameters... params) {
		parameters = params[0];
		
		IndentItemTaskResult result = new IndentItemTaskResult();
		
		try {
			int newParentID = Globals.getApplication().getVaultDocument().indent(parameters.getOutlineItem());
			result.setNewParentID(newParentID);
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("IndentItemTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(IndentItemTaskResult result) {
		parameters.getVault3Activity().setEnabled(true);

		if (result.getException() != null) {
			new AlertDialog.Builder(parameters.getVault3Activity())
					.setTitle("Indent")
					.setMessage("Cannot indent outline item.")
					.setPositiveButton("OK", null)
					.create()
					.show();
		} else {
			Globals.getApplication().getVaultDocument().setDirty(true);

			parameters.getVault3Activity().update(result.getNewParentID());
		}
	}
}
