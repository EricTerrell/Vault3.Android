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

package com.ericbt.vault3base.async_tasks.move_item_down;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.StringLiterals;

public class MoveItemDownTask extends AsyncTask<MoveItemDownTaskParameters, Void, MoveItemDownTaskResult> {
	private MoveItemDownTaskParameters parameters;
	
	@Override
	protected MoveItemDownTaskResult doInBackground(MoveItemDownTaskParameters... params) {
		parameters = params[0];
		
		MoveItemDownTaskResult result = new MoveItemDownTaskResult();
		
		try {
			Globals.getApplication().getVaultDocument().moveDown(parameters.getOutlineItem());
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("MoveItemDownTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(MoveItemDownTaskResult result) {
		parameters.getVault3Activity().setEnabled(true);
		
		if (result.getException() != null) {
			new AlertDialog.Builder(parameters.getVault3Activity())
					.setTitle("Move Down")
					.setMessage("Cannot move outline item down.")
					.setPositiveButton("OK", null)
					.create()
					.show();
		} else {
			Globals.getApplication().getVaultDocument().setDirty(true);

			parameters.getVault3Activity().update();
		}
	}
}
