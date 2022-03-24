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

package com.ericbt.vault3base.async_tasks.search;

import android.app.AlertDialog;
import android.os.AsyncTask;

import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.SearchHit;

public class SearchTask extends AsyncTask<SearchTaskParameters, SearchHit, SearchTaskResult> {
	private SearchTaskParameters searchTaskParameters;
	
	@Override
	protected SearchTaskResult doInBackground(SearchTaskParameters... parameters) {
		searchTaskParameters = parameters[0];
		
		SearchTaskResult result = new SearchTaskResult();
		
		try {
			Globals.getApplication().getVaultDocument().search(searchTaskParameters.getSearch(), this);
		} catch (Exception ex) {
			ex.printStackTrace();
			result.setException(ex);
		}

		return result;
	}

	public void publishProgress(SearchHit searchHit) {
		super.publishProgress(searchHit);
	}
	
	@Override
	protected void onProgressUpdate(SearchHit... values) {
		searchTaskParameters.getSearchActivity().update(values[0]);
	}

	@Override
	protected void onPostExecute(SearchTaskResult result) {
		searchTaskParameters.getSearchActivity().setEnabled(true);
		
		if (result.getException() != null) {
			new AlertDialog.Builder(searchTaskParameters.getSearchActivity())
					.setTitle("Search")
					.setMessage("Cannot search.")
					.setPositiveButton("OK", null)
					.create()
					.show();
		}
		
		searchTaskParameters.getSearchActivity().searchCompleted();
	}

	@Override
	protected void onCancelled() {
		searchTaskParameters.getSearchActivity().searchCompleted();
	}
}
