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

package com.ericbt.vault3base.async_tasks.remove_item_and_children;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.vault3base.ExitApplication;
import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.async_tasks.update_navigate_list_item.UpdateNavigateListItemTask;
import com.ericbt.vault3base.async_tasks.update_navigate_list_item.UpdateNavigateListItemTaskParameters;

public class RemoveItemAndChildrenTask extends AsyncTask<RemoveItemAndChildrenTaskParameters, Void, RemoveItemAndChildrenTaskResult> {
	private RemoveItemAndChildrenTaskParameters parameters;
	
	@Override
	protected RemoveItemAndChildrenTaskResult doInBackground(RemoveItemAndChildrenTaskParameters... params) {
		parameters = params[0];
		
		RemoveItemAndChildrenTaskResult result = new RemoveItemAndChildrenTaskResult();
		
		try {
			result.setOutlineItemsRemoved(Globals.getApplication().getVaultDocument().removeOutlineItem(parameters.getOutlineItem().getId()));
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("RemoveItemAndChildrenTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		OutlineItem parentOutlineItem;

		try {
			parentOutlineItem = Globals.getApplication().getVaultDocument().getOutlineItem(parameters.getOutlineItem().getParentId());
			result.setParentOutlineItem(parentOutlineItem);
		}
		catch (Throwable ex) {
			result.setFatalException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(RemoveItemAndChildrenTaskResult result) {
		if (result.getFatalException() != null) {
			ExitApplication.exit();
		} else {
			parameters.getVault3Activity().setEnabled(true);
			
			if (result.getException() != null) {
				new AlertDialog.Builder(parameters.getVault3Activity())
						.setTitle("Remove")
						.setMessage("Cannot remove outline item.")
						.setPositiveButton("OK", null)
						.create()
						.show();
			} else {
				Globals.getApplication().getVaultDocument().setDirty(true);

				// If user removed an item that was in the process of being
				// moved, cancel the move.
				for (int removedOutlineItemID : result.getOutlineItemsRemoved()) {
					if (parameters.getVault3Activity().getMovingOutlineItem() != null) {
						if (parameters.getVault3Activity().getMovingOutlineItem().getId() == removedOutlineItemID) {
							parameters.getVault3Activity().setMovingOutlineItem(null);
							break;
						}
					}
				}
			}

			// If we just removed the last child of a non-root item, time to go
			// up.
			if (!result.getParentOutlineItem().isRoot()
					&& !result.getParentOutlineItem().getHasChildren()) {
				UpdateNavigateListItemTaskParameters updateNavigateListItemTaskParameters = new UpdateNavigateListItemTaskParameters(result.getParentOutlineItem().getParentId(), parameters.getVault3Activity());
				(new UpdateNavigateListItemTask()).execute(updateNavigateListItemTaskParameters);
			} else {
				parameters.getVault3Activity().updateUIs(result.getParentOutlineItem());
			}
			
			// Do we need to display the "Add Item" button after the last top level item was removed?
			if (result.getParentOutlineItem().isRoot() && !result.getParentOutlineItem().getHasChildren()) {
				parameters.getVault3Activity().enableAddItem(true);
			}
		}
	}
}
