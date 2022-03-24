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

package com.ericbt.vault3base.async_tasks.update_navigate_list_item;

import com.ericbt.vault3base.Vault3;

public class UpdateNavigateListItemTaskParameters {
	private final int outlineItemID;

	public int getOutlineItemID() {
		return outlineItemID;
	}
	
	private final Vault3 vault3Activity;
	
	public Vault3 getVault3Activity() {
		return vault3Activity;
	}

	public UpdateNavigateListItemTaskParameters(int outlineItemID, Vault3 vault3Activity) {
		this.outlineItemID = outlineItemID;
		this.vault3Activity = vault3Activity;
	}
}
