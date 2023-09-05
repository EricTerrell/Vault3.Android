/*
  Vault 3
  (C) Copyright 2023, Eric Bergman-Terrell
  
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

import java.io.File;
import java.util.HashMap;

import android.util.Log;

public class PasswordCache {
	private static final HashMap<String, String> cache = new HashMap<>();
	
	public void put(String filePath, String password) {
		if (VaultPreferenceActivity.getCachePasswords()) {
			try {
				String canonicalFilePath = new File(filePath).getCanonicalPath();
	
				Log.i(StringLiterals.LogTag, String.format("PasswordCache.put: canonicalFilePath: %s", canonicalFilePath));
				
				cache.put(canonicalFilePath, password);
			}
			catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public String get(String filePath) {
		String password = null;
		
		if (VaultPreferenceActivity.getCachePasswords()) {
			try {
				String canonicalFilePath = new File(filePath).getCanonicalPath();
				
				Log.i(StringLiterals.LogTag, String.format("PasswordCache.get: canonicalFilePath: %s", canonicalFilePath));
	
				password = cache.get(canonicalFilePath);
			}
			catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
		
		return password;
	}
	
	public static void clear() {
		Log.i(StringLiterals.LogTag, "PasswordCache.clear");
		
		cache.clear();
	}
}
