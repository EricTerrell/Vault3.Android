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

package com.ericbt.vault3base.async_tasks.delete_all_temp_files;

import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.vault3base.FileUtils;
import com.ericbt.vault3base.StringLiterals;

public class DeleteAllTempFilesTask extends
				AsyncTask<DeleteAllTempFilesTaskParameters, Void, DeleteAllTempFilesTaskResult> {
	private DeleteAllTempFilesTaskParameters parameters;

	@Override
	protected DeleteAllTempFilesTaskResult
							doInBackground(DeleteAllTempFilesTaskParameters... params) {
		parameters = params[0];
		
		final DeleteAllTempFilesTaskResult result = new DeleteAllTempFilesTaskResult();

		try {
			FileUtils.deleteAllTempFiles(parameters.getActivity());
		} catch (Throwable ex) {
			Log.e(StringLiterals.LogTag,
					String.format("DeleteAllTempFilesTask: Exception %s", ex.getMessage()));
			ex.printStackTrace();
			result.setException(ex);
		}
		
		return result;
	}

	@Override
	protected void onPostExecute(DeleteAllTempFilesTaskResult result) {
		parameters.getActivity().enable(true);
	}
}
