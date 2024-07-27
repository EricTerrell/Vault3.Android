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

package com.ericbt.vault3base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class CustomBroadcastReceiver extends BroadcastReceiver {
    public final static String UPDATE_TEXT = "UPDATE_TEXT";

    public final static String ID = "ID";
    public final static String NEW_TITLE = "NEW_TITLE";
    public final static String NEW_TEXT = "NEW_TEXT";

    public final static String UPDATE_FONT = "UPDATE_FONT";

    public final static String FONT_LIST = "FONT_LIST";
    public final static String RED = "RED";
    public final static String GREEN = "GREEN";
    public final static String BLUE = "BLUE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(StringLiterals.LogTag, "CustomBroadcastReceiver.onReceive");
    }
}
