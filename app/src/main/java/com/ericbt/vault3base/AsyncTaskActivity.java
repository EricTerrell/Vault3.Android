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

import android.app.Activity;

public abstract class AsyncTaskActivity extends Activity {
	protected boolean enabled = true;
	
	public abstract void enable(boolean enabled);
	
	public void setEnabled(boolean enabled) 
	{ 
		this.enabled = enabled; 
		
		enable(enabled);
	}
	
	@Override
	public void onBackPressed() {
		if (enabled) {
			super.onBackPressed();
		}
	}
	
}
