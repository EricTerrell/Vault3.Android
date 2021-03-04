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

public class SortTask extends AsyncTask<SortTaskParameters, Void, SortTaskResult> {
	private SortTaskParameters parameters;
	
	@Override
	protected SortTaskResult doInBackground(SortTaskParameters... params) {
		parameters = params[0];

		SortTaskResult result = new SortTaskResult();
		
		try {
			Globals.getApplication().getVaultDocument().sort(parameters.getOutlineItem());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result.setException(ex);
		}
		
		// Now that its children are sorted, we need to re-retrieve the outline item.
		try {
			OutlineItem outlineItem = Globals.getApplication().getVaultDocument().getOutlineItem(parameters.getOutlineItem().getId());
			result.setOutlineItem(outlineItem);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			result.setFatalException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(SortTaskResult result) {
		if (result.getFatalException() != null) {
			ExitApplication.exit();
		}
		else {
			parameters.getVault3Activity().setEnabled(true);
			
			if (result.getException() != null) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parameters.getVault3Activity());
				alertDialogBuilder.setTitle("Sort");
				alertDialogBuilder.setMessage("Cannot sort.");
				alertDialogBuilder.setPositiveButton("OK", null);
				
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
			
			NavigateArrayAdapter navigateArrayAdapter = (NavigateArrayAdapter) parameters.getVault3Activity().getNavigateListView().getAdapter();
	
			try {
				navigateArrayAdapter.setOutlineItem(result.getOutlineItem());
				parameters.getVault3Activity().enableDisableButtons();
				
				parameters.getVault3Activity().enable(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
