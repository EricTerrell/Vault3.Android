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

package com.ericbt.vault3base.async_tasks.update_outline_item;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.vault3base.CustomBroadcastReceiver;
import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;

public class UpdateOutlineItemTask extends AsyncTask<UpdateOutlineItemTaskParameters, Void, UpdateOutlineItemTaskResult> {
	private UpdateOutlineItemTaskParameters parameters;
	
	@Override
	protected UpdateOutlineItemTaskResult doInBackground(UpdateOutlineItemTaskParameters... params) {
		parameters = params[0];
		
		UpdateOutlineItemTaskResult result = new UpdateOutlineItemTaskResult();
		
		try {
			Globals.getApplication().getVaultDocument().updateOutlineItem(parameters.getOutlineItem(),
														 				  parameters.getNewTitle(), 
														 				  parameters.getNewText());
			
			OutlineItem retrievedOutlineItem = Globals.getApplication().getVaultDocument().getOutlineItem(parameters.getOutlineItem().getId(), false);
			
			result.setOutlineItem(retrievedOutlineItem);
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, String.format("UpdateOutlineItemTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(UpdateOutlineItemTaskResult result) {
		if (result.getException() != null) {
			new AlertDialog.Builder(parameters.getTextDisplayUpdate().getAsyncTaskActivity())
					.setTitle("Edit Text")
					.setMessage("Cannot update item.")
					.setPositiveButton("OK", null)
					.create()
					.show();
			
			parameters.getTextDisplayUpdate().getAsyncTaskActivity().setEnabled(true);
		}
		else {
			notifyOfUpdate(result.getOutlineItem());

			parameters.getTextDisplayUpdate().update(result.getOutlineItem());
		}
	}

	private void notifyOfUpdate(OutlineItem outlineItem) {
		final Intent intent = new Intent(CustomBroadcastReceiver.UPDATE_TEXT)
				.putExtra(CustomBroadcastReceiver.ID, outlineItem.getId())
				.putExtra(CustomBroadcastReceiver.NEW_TITLE, outlineItem.getTitle())
				.putExtra(CustomBroadcastReceiver.NEW_TEXT, outlineItem.getText());

		parameters.getContext().sendBroadcast(intent);
	}
}
