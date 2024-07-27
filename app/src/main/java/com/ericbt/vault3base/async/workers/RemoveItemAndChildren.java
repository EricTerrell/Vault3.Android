/*
  Vault 3
  (C) Copyright 2024, Eric Bergman-Terrell

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
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.Vault3;
import com.ericbt.vault3base.async.Async;

import java.util.List;

public class RemoveItemAndChildren extends Async {
    public void removeItemAndChildren(OutlineItem outlineItem, Vault3 vault3Activity) {
        submit(() -> {
            Throwable exception = null, fatalException = null;

            List<Integer> outlineItemsRemoved = null;

            try {
                outlineItemsRemoved = Globals.getApplication().getVaultDocument()
                        .removeOutlineItem(outlineItem.getId());
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                      String.format("RemoveItemAndChildren: Exception %s", ex.getMessage()),
                      ex);

                exception = ex;
            }

            OutlineItem parentOutlineItem = null;

            try {
                parentOutlineItem = Globals.getApplication().getVaultDocument().getOutlineItem(outlineItem.getParentId());
            }
            catch (Throwable ex) {
                fatalException = ex;
            }

            final Throwable exception2 = exception, fatalException2 = fatalException;
            final List<Integer> outlineItemsRemoved2 = outlineItemsRemoved;
            final OutlineItem parentOutlineItem2 = parentOutlineItem;

            handler.post(() -> {
                if (fatalException2 != null) {
                    ExitApplication.exit();
                } else {
                    vault3Activity.setEnabled(true);

                    if (exception2 != null) {
                        new AlertDialog.Builder(vault3Activity)
                                .setTitle("Remove")
                                .setMessage("Cannot remove outline item.")
                                .setPositiveButton("OK", null)
                                .create()
                                .show();
                    } else {
                        Globals.getApplication().getVaultDocument().setDirty(true);

                        // If user removed an item that was in the process of being
                        // moved, cancel the move.
                        for (int removedOutlineItemID : outlineItemsRemoved2) {
                            if (vault3Activity.getMovingOutlineItem() != null) {
                                if (vault3Activity.getMovingOutlineItem().getId() == removedOutlineItemID) {
                                    vault3Activity.setMovingOutlineItem(null);
                                    break;
                                }
                            }
                        }
                    }

                    // If we just removed the last child of a non-root item, time to go
                    // up.
                    if (!parentOutlineItem2.isRoot()
                            && !parentOutlineItem2.getHasChildren()) {
                        new UpdateNavigateListItem().updateNavigateListItem(
                                parentOutlineItem2.getParentId(),
                                vault3Activity);
                    } else {
                        vault3Activity.updateUIs(parentOutlineItem2);
                    }

                    // Do we need to display the "Add Item" button after the last top level item was removed?
                    if (parentOutlineItem2.isRoot() && !parentOutlineItem2.getHasChildren()) {
                        vault3Activity.enableAddItem(true);
                    }
                }
            });
        });
    }
}
