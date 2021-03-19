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

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class VaultApplication extends Application {
	private VaultDocument vaultDocument;

	public VaultDocument getVaultDocument() {
		return vaultDocument;
	}

	public void setVaultDocument(VaultDocument vaultDocument, Vault3 vault3Activity) {
		Log.i(StringLiterals.LogTag, String.format("VaultApplication.setVaultDocument setting vaultDocument%s", vaultDocument == null ? " to null" : ""));
		
		if (vault3Activity != null) {
			vault3Activity.updateStateWhenDocumentChanged();
		}
		
		if (this.vaultDocument != null) {
			this.vaultDocument.close();
		}
		
		this.vaultDocument = vaultDocument;
		
		// Re-initialize saved application state since the document has changed.
		ApplicationState applicationState = null;
		
		if (vaultDocument != null) {
			applicationState = new ApplicationState(vaultDocument.getDatabase().getPath(), 1);
		}
		
		VaultPreferenceActivity.putApplicationState(applicationState);
	}

	public void setVaultDocument(VaultDocument vaultDocument) {
		setVaultDocument(vaultDocument, null);
	}
	
	private PasswordCache passwordCache;
	
	public PasswordCache getPasswordCache() {
		return passwordCache;
	}

	@Override
	public void onCreate() {
		Log.i(StringLiterals.LogTag, "VaultApplication.onCreate");

		super.onCreate();

		passwordCache = new PasswordCache();
		FontListInitializer.initialize();
		
		Globals.setApplication(this);

		Thread.setDefaultUncaughtExceptionHandler(new CustomUncaughtExceptionHandler());
	}

	@Override
	public void onLowMemory() {
		Log.i(StringLiterals.LogTag, "VaultApplication.onLowMemory");
		
		super.onLowMemory();
		
		int bytesReleased = SQLiteDatabase.releaseMemory();
		Log.i(StringLiterals.LogTag, String.format("Released %d bytes", bytesReleased));
	}

	@Override
	public void onTerminate() {
		Log.i(StringLiterals.LogTag, "VaultApplication.onTerminate");
		
		super.onTerminate();
	}
}
