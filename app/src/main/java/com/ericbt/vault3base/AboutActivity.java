package com.ericbt.vault3base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.about_dialog);
		
		setTitle(String.format("%s: About %s", StringLiterals.ProgramName, StringLiterals.ProgramName));
		
        boolean isFreeVersion = Globals.isFreeVersion();
        
        Button upgrade = (Button) findViewById(R.id.Upgrade);
        upgrade.setEnabled(isFreeVersion);
        upgrade.setVisibility(isFreeVersion ? View.VISIBLE : View.INVISIBLE);
        
        if (isFreeVersion) {
        	upgrade.setOnClickListener(v -> {
				Intent intent = new Intent(AboutActivity.this, UpgradeActivity.class);
				startActivity(intent);
			});
        }
		
		Button readLicenseTermsButton = (Button) findViewById(R.id.ReadLicenseTerms);
		
		readLicenseTermsButton.setOnClickListener(v -> {
			Intent intent = new Intent(AboutActivity.this, LicenseTermsActivity.class);
			intent.putExtra(StringLiterals.AllowCancel, true);
startActivity(intent);
		});
	
		TextView version = (TextView) findViewById(R.id.Version);
		
		String versionName = "";
		
		try {
			versionName = AboutActivity.this.getPackageManager().getPackageInfo(AboutActivity.this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			
			Log.e(StringLiterals.LogTag, "AboutDialog.show: cannot get version name");
		}
		
		version.setText(String.format("%s, Version %s", StringLiterals.FullProgramName, versionName));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			finish();
			
			result = true;
		}
		
		return result;
	}
}
