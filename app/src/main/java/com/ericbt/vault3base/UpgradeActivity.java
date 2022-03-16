/*
  Vault 3
  (C) Copyright 2022, Eric Bergman-Terrell
  
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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class UpgradeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.upgrade_dialog);
        
		setTitle(String.format("%s: Upgrade %s", StringLiterals.ProgramName, StringLiterals.ProgramName));
		
        boolean isFreeVersion = Globals.isFreeVersion();
        
        Button upgrade = findViewById(R.id.Upgrade);
        upgrade.setEnabled(isFreeVersion);
        upgrade.setVisibility(isFreeVersion ? View.VISIBLE : View.INVISIBLE);
        
    	upgrade.setOnClickListener(v -> {
			try {
				String appStore = getResources().getString(R.string.app_store);

				if (!appStore.equals("BN")) {
					String paidVersionURL = getResources().getString(R.string.paid_version_url);
					Log.i(StringLiterals.LogTag, String.format("UpgradeDialog.show: paidVersionURL: %s", paidVersionURL));

					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paidVersionURL));
					startActivity(intent);
				}
				else {
					String ean = getResources().getString(R.string.bn_EAN);

					Intent intent = new Intent();
					intent.setAction("com.bn.sdk.shop.details");
					intent.putExtra("product_details_ean", ean);
					startActivity(intent);
				}
			}
			catch (Throwable ex) {
				Log.e(StringLiterals.LogTag, String.format("UpgradeDialog.show exception: %s", ex.getMessage()));
			}
		});
		
    	Button cancelButton = findViewById(R.id.CancelButton);
    	
    	cancelButton.setOnClickListener(v -> finish());
	}
}
