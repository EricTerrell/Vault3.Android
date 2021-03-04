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
