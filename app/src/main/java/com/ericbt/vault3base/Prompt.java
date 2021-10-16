/*
  Vault 3
  (C) Copyright 2021, Eric Bergman-Terrell
  
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

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Button;

public class Prompt {
	private final Activity activity;
	private final String title;
    private final String message;
    private final String positiveButtonText;
    private final String negativeButtonText;
	private final int defaultButton;
	private AlertDialog promptDialog;
	
	public Prompt(Activity activity, String title, String message, String positiveButtonText, String negativeButtonText, int defaultButton) {
		this.activity = activity;
		this.title = title;
		this.message = message;
		this.positiveButtonText = positiveButtonText;
		this.negativeButtonText = negativeButtonText;
		this.defaultButton = defaultButton;
	}
	
	public void onBackPressed() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		
		alertDialogBuilder.setPositiveButton(positiveButtonText, (dialog, which) -> {
			promptDialog.dismiss();

			activity.finish();
		});

		alertDialogBuilder.setNegativeButton(negativeButtonText, (dialog, which) -> {
		});
		
		promptDialog = alertDialogBuilder.create();
		promptDialog.setCancelable(false);
		promptDialog.show();

		Button button = promptDialog.getButton(defaultButton);
		
		button.requestFocus();
		button.setSelected(true);
	}
}
