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
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ericbt.vault3base.CustomBroadcastReceiver;
import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.TextDisplayUpdate;
import com.ericbt.vault3base.async.Async;

public class UpdateOutlineItem extends Async {
    public void updateOutlineItem(OutlineItem outlineItem, String newTitle, String newText,
                                  TextDisplayUpdate textDisplayUpdate, Context context) {
        submit(() -> {
            Throwable exception = null;
            OutlineItem retrievedOutlineItem = null;

            try {
                Globals.getApplication().getVaultDocument().updateOutlineItem(outlineItem,
                        newTitle,
                        newText);

                retrievedOutlineItem = Globals.getApplication().getVaultDocument()
                        .getOutlineItem(outlineItem.getId(), false);
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("UpdateOutlineItem: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            final boolean success = exception == null;
            final OutlineItem retrievedOutlineItem2 = retrievedOutlineItem;

            handler.post(() -> {
                if (!success) {
                    new AlertDialog.Builder(textDisplayUpdate.getAsyncTaskActivity())
                            .setTitle("Edit Text")
                            .setMessage("Cannot update item.")
                            .setPositiveButton("OK", null)
                            .create()
                            .show();

                    textDisplayUpdate.getAsyncTaskActivity().setEnabled(true);
                }
                else {
                    notifyOfUpdate(retrievedOutlineItem2, context);

                    textDisplayUpdate.update(retrievedOutlineItem2);
                }
            });
        });
    }

    private void notifyOfUpdate(OutlineItem outlineItem, Context context) {
        final Intent intent = new Intent(CustomBroadcastReceiver.UPDATE_TEXT)
                .putExtra(CustomBroadcastReceiver.ID, outlineItem.getId())
                .putExtra(CustomBroadcastReceiver.NEW_TITLE, outlineItem.getTitle())
                .putExtra(CustomBroadcastReceiver.NEW_TEXT, outlineItem.getText());

        context.sendBroadcast(intent);
    }
}
