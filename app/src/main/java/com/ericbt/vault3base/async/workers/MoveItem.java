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

import com.ericbt.vault3base.Globals;
import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.Vault3;
import com.ericbt.vault3base.async.Async;

public class MoveItem extends Async {
    public void moveItem(OutlineItem movingOutlineItem, OutlineItem selectedOutlineItem,
                         boolean placeAbove, Vault3 vault3Activity) {
        submit(() -> {
            Throwable exception = null;

            try {
                Globals.getApplication().getVaultDocument().moveTo(movingOutlineItem,
                        selectedOutlineItem, placeAbove);
                vault3Activity.setMovingOutlineItem(null);
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("MoveItem: Exception %s", ex.getMessage()), ex);

                exception = ex;
            }

            final boolean success = exception == null;

            handler.post(() -> {
                vault3Activity.setEnabled(true);

                if (!success) {
                    new AlertDialog.Builder(vault3Activity)
                            .setTitle("Move")
                            .setMessage("Cannot move outline item.")
                            .setPositiveButton("OK", null)
                            .create()
                            .show();
                } else {
                    Globals.getApplication().getVaultDocument().setDirty(true);

                    vault3Activity.update(selectedOutlineItem.getParentId());
                }
            });
        });
    }
}
