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
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.AsyncTask;

import fonts.FontList;

public class UpdateFontTask extends AsyncTask<UpdateFontTaskParameters, Void, UpdateFontTaskResult> {
	private UpdateFontTaskParameters parameters;
	
	@Override
	protected UpdateFontTaskResult doInBackground(UpdateFontTaskParameters... params) {
		UpdateFontTaskResult result = new UpdateFontTaskResult();

		parameters = params[0];
		
		try {
			Globals.getApplication().getVaultDocument().updateFont(parameters.getOutlineItem(), 
												  				   parameters.getFont(), 
												  				   parameters.getColor());
			
			OutlineItem retrievedOutlineItem = Globals.getApplication().getVaultDocument().getOutlineItem(parameters.getOutlineItem().getId(), false);
			
			result.setOutlineItem(retrievedOutlineItem);
		}
		catch (Throwable ex) {
			result.setException(ex);
		}

		return result;
	}

	@Override
	protected void onPostExecute(UpdateFontTaskResult result) {
		parameters.getTextDisplayUpdate().setEnabled(true);

		if (result.getException() == null) {
			final OutlineItem outlineItem = result.getOutlineItem();

			notifyOfUpdate(outlineItem);

			parameters.getTextDisplayUpdate().update(result.getOutlineItem());
		}
		else {
			AlertDialog.Builder alertDialogBuilder = new Builder(parameters.getTextDisplayUpdate().getAsyncTaskActivity());
			alertDialogBuilder.setTitle("Cannot Update Font");
			
			String message = String.format("Cannot update font: %s", result.getException().getMessage());
			alertDialogBuilder.setMessage(message);
			alertDialogBuilder.setPositiveButton("OK", null);
			
			alertDialogBuilder.create().show();
		}
	}

	private void notifyOfUpdate(OutlineItem outlineItem) {
		final Intent intent = new Intent(CustomBroadcastReceiver.UPDATE_FONT)
				.putExtra(CustomBroadcastReceiver.ID, outlineItem.getId())
				.putExtra(CustomBroadcastReceiver.FONT_LIST, FontList.serialize(outlineItem.getFontList()))
				.putExtra(CustomBroadcastReceiver.RED, outlineItem.getColor().getRed())
				.putExtra(CustomBroadcastReceiver.GREEN, outlineItem.getColor().getGreen())
				.putExtra(CustomBroadcastReceiver.BLUE, outlineItem.getColor().getBlue());

		parameters.getContext().sendBroadcast(intent);
	}
}
