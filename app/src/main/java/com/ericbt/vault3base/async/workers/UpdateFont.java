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
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ericbt.vault3base.CustomBroadcastReceiver;
import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.TextDisplayUpdate;
import com.ericbt.vault3base.async.Async;

import fonts.AndroidFont;
import fonts.FontList;

public class UpdateFont extends Async {
    public void updateFont(AndroidFont font, OutlineItem outlineItem, int color,
                           TextDisplayUpdate textDisplayUpdate, Context context) {
        submit(() -> {
            Throwable exception = null;

            try {
                Globals.getApplication().getVaultDocument().updateFont(outlineItem,
                        font,
                        color);

                Globals.getApplication().getVaultDocument()
                        .getOutlineItem(outlineItem.getId(), false);
            }
            catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("UpdateFont: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            final Throwable exception2 = exception;

            handler.post(() -> {
                textDisplayUpdate.setEnabled(true);

                if (exception2 == null) {
                    notifyOfUpdate(outlineItem, context);

                    textDisplayUpdate.update(outlineItem);
                }
                else {
                    final String message = String.format("Cannot update font: %s",
                            exception2.getMessage());

                    new AlertDialog.Builder(textDisplayUpdate.getAsyncTaskActivity())
                            .setTitle("Cannot Update Font")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                }
            });
        });
    }

    private void notifyOfUpdate(OutlineItem outlineItem, Context context) {
        final Intent intent = new Intent(CustomBroadcastReceiver.UPDATE_FONT)
                .putExtra(CustomBroadcastReceiver.ID, outlineItem.getId())
                .putExtra(CustomBroadcastReceiver.FONT_LIST, FontList.serialize(outlineItem.getFontList()))
                .putExtra(CustomBroadcastReceiver.RED, outlineItem.getColor().getRed())
                .putExtra(CustomBroadcastReceiver.GREEN, outlineItem.getColor().getGreen())
                .putExtra(CustomBroadcastReceiver.BLUE, outlineItem.getColor().getBlue());

        context.sendBroadcast(intent);
    }
}
