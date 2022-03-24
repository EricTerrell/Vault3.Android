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

package com.ericbt.vault3base.async_tasks.update_navigate_list_item;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.VaultDocument;

public class UpdateNavigateListItemTask extends AsyncTask<UpdateNavigateListItemTaskParameters, Void, UpdateNavigateListItemTaskResult> {
	private UpdateNavigateListItemTaskParameters parameters;
	
	@Override
	protected UpdateNavigateListItemTaskResult doInBackground(UpdateNavigateListItemTaskParameters... params) {
		OutlineItem outlineItem;

		parameters = params[0];
		
		UpdateNavigateListItemTaskResult result = new UpdateNavigateListItemTaskResult();
		
		result.setOutlineItemID(parameters.getOutlineItemID());

		try {
			outlineItem = Globals.getApplication().getVaultDocument().getOutlineItem(parameters.getOutlineItemID());
			outlineItem.setSelected(!outlineItem.isRoot());

			result.setOutlineItem(outlineItem);
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("UpdateNavigateListItemTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(UpdateNavigateListItemTaskResult result) {
		parameters.getVault3Activity().setEnabled(true);
		
		if (result.getException() == null) {
			parameters.getVault3Activity().updateUIs(result.getOutlineItem());
			parameters.getVault3Activity().enableAddItem(!result.getOutlineItem().getHasChildren());
		}
		else {
			VaultDocument.closeDocument();
			parameters.getVault3Activity().updateGUIWhenCurrentDocumentOpenedOrClosed(false);

			VaultDocument vaultDocument = Globals.getApplication().getVaultDocument();

			String message;

			if (vaultDocument != null) {
				message = String.format("Cannot retrieve outline item %d in %s.", result.getOutlineItemID(), Globals.getApplication().getVaultDocument().getDatabase().getPath());
			}
			else {
				message = String.format("Cannot retrieve outline item %d", result.getOutlineItemID());
			}

			new AlertDialog.Builder(parameters.getVault3Activity())
					.setTitle("Error")
					.setMessage(message)
					.setPositiveButton("OK", null)
					.create()
					.show();
		}
	}
}
