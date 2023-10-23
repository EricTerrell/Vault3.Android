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

package com.ericbt.vault3base.async.workers;

import android.app.AlertDialog;
import android.util.Log;

import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.SearchActivity;
import com.ericbt.vault3base.SearchHit;
import com.ericbt.vault3base.SearchParameters;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.async.Async;

public class Search extends Async {
    private SearchActivity searchActivity;

    public void search(SearchActivity searchActivity, SearchParameters searchParameters) {
        this.searchActivity = searchActivity;

        submit(() -> {
            Throwable exception = null;

            try {
                Globals.getApplication().getVaultDocument().search(searchParameters, this);
            } catch (Exception ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("Search: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            final boolean success = exception == null;

            handler.post(() -> {
                searchActivity.setEnabled(true);

                if (!success) {
                    new AlertDialog.Builder(searchActivity)
                            .setTitle("Search")
                            .setMessage("Cannot search.")
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }

                searchActivity.searchCompleted();
            });
        });
    }

    public void reportProgress(SearchHit searchHit) {
        handler.post(() -> {
            searchActivity.update(searchHit);
        });
    }

    private boolean isCancelled = false;

    public boolean isCancelled() { return isCancelled; }

    public void cancel() {
        synchronized (this) {
            isCancelled = true;
        }
    }
}
