/*
  Vault 3
  (C) Copyright 2015, Eric Bergman-Terrell
  
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
import android.content.DialogInterface;
import android.widget.Button;

public class Prompt {
	private Activity activity;
	private String title, message, positiveButtonText, negativeButtonText;
	private int defaultButton;
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
		
		alertDialogBuilder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				promptDialog.dismiss();
				
				activity.finish();
			}
		});

		alertDialogBuilder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
		promptDialog = alertDialogBuilder.create();
		promptDialog.setCancelable(false);
		promptDialog.show();

		Button button = promptDialog.getButton(defaultButton);
		
		button.requestFocus();
		button.setSelected(true);
	}
}
