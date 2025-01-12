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

import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.Vault3;
import com.ericbt.vault3base.async.Async;

public class AddItem extends Async {
    public void addItem(OutlineItem newOutlineItem, OutlineItem selectedOutlineItem,
                        boolean addAbove, boolean displayHint, Vault3 vault3Activity) {
        submit(() -> {
            Throwable exception = null;

            try {
                Globals.getApplication().getVaultDocument()
                        .add(newOutlineItem, selectedOutlineItem, addAbove);
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("AddItem: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            final boolean success = exception == null;

            handler.post(() -> {
                if (success) {
                    Globals.getApplication().getVaultDocument().setDirty(true);

                    vault3Activity.update(selectedOutlineItem.getParentId());

                    // Tell the user how to add the next item.
                    if (displayHint) {
                        displayHint(vault3Activity);
                    }
                } else {
                    new AlertDialog.Builder(vault3Activity)
                            .setTitle("Add")
                            .setMessage("Cannot add outline item.")
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }
            });
        });
    }

    private void displayHint(Vault3 vault3Activity) {
        new AlertDialog.Builder(vault3Activity)
                .setTitle("Add")
                .setMessage("To add more outline items, or to manipulate existing items, long-click an outline item or click the wrench icon.")
                .setPositiveButton("OK", null)
                .create()
                .show();
    }
}
