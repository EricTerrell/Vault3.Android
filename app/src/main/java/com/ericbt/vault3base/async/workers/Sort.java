/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell

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

import com.ericbt.vault3base.ExitApplication;
import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.NavigateArrayAdapter;
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.Vault3;
import com.ericbt.vault3base.async.Async;

public class Sort extends Async {
    private Vault3 vault3Activity;

    public void sort(OutlineItem outlineItem, Vault3 vault3Activity) {
        this.vault3Activity = vault3Activity;

        submit(() -> {
            Throwable exception = null;

            try {
                Globals.getApplication().getVaultDocument().sort(outlineItem);
            }
            catch (Exception ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("Sort: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            OutlineItem retrievedOutlineItem = null;

            // Now that its children are sorted, we need to re-retrieve the outline item.
            try {
                retrievedOutlineItem = Globals.getApplication().getVaultDocument()
                        .getOutlineItem(outlineItem.getId());
            }
            catch (Exception ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("Sort.sort: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            final Throwable exception2 = exception;
            final OutlineItem retrievedOutlineItem2 = retrievedOutlineItem;

            handler.post(() -> {
                if (exception2 != null) {
                    ExitApplication.exit();
                } else {
                    Globals.getApplication().getVaultDocument().setDirty(true);

                    vault3Activity.setEnabled(true);

                    if (exception2 != null) {
                        new AlertDialog.Builder(vault3Activity)
                                .setTitle("Sort")
                                .setMessage("Cannot sort.")
                                .setPositiveButton("OK", null)
                                .create()
                                .show();
                    }

                    try {
                        final NavigateArrayAdapter navigateArrayAdapter = (NavigateArrayAdapter)
                                vault3Activity.getNavigateListView().getAdapter();

                        navigateArrayAdapter.setOutlineItem(retrievedOutlineItem2);
                        vault3Activity.enableDisableButtons();

                        vault3Activity.enable(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }
}
