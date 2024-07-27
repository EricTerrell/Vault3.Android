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

import android.util.Log;

import com.ericbt.vault3base.AsyncTaskActivity;
import com.ericbt.vault3base.FileUtils;
import com.ericbt.vault3base.StringLiterals;
import com.ericbt.vault3base.async.Async;

public class DeleteAllTempFiles extends Async {
    public void deleteAllTempFiles(AsyncTaskActivity activity) {
        submit(() -> {
            try {
                FileUtils.deleteAllTempFiles(activity);
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag,
                        String.format("DeleteAllTempFiles: Exception %s", ex.getMessage()), ex);
            }

            handler.post(() -> {
                activity.enable(true);
            });
        });
    }
}
