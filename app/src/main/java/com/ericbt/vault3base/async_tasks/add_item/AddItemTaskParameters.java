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

package com.ericbt.vault3base.async_tasks.add_item;

import com.ericbt.vault3base.OutlineItem;
import com.ericbt.vault3base.Vault3;

public class AddItemTaskParameters {
	private final OutlineItem newOutlineItem;
	private final OutlineItem selectedOutlineItem;
	private final boolean addAbove;
	
	public OutlineItem getNewOutlineItem() {
		return newOutlineItem;
	}

	public OutlineItem getSelectedOutlineItem() {
		return selectedOutlineItem;
	}

	public boolean getAddAbove() {
		return addAbove;
	}
	
	private final boolean displayHint;
	
	public boolean getDisplayHint() {
		return displayHint;
	}
	
	private final Vault3 vault3Activity;
	
	public Vault3 getVault3Activity() {
		return vault3Activity;
	}
	
	public AddItemTaskParameters(OutlineItem newOutlineItem, OutlineItem selectedOutlineItem, boolean addAbove, boolean displayHint, Vault3 vault3Activity) {
		this.newOutlineItem = newOutlineItem;
		this.selectedOutlineItem = selectedOutlineItem;
		this.addAbove = addAbove;
		this.displayHint = displayHint;
		this.vault3Activity = vault3Activity;
	}
}
