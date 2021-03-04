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

import android.content.Context;

public class UpdateOutlineItemTaskParameters {
	private OutlineItem outlineItem;

	public OutlineItem getOutlineItem() {
		return outlineItem;
	}

	private String newTitle;

	public String getNewTitle() {
		return newTitle;
	}

	private String newText;

	public String getNewText() {
		return newText;
	}
	
	private TextDisplayUpdate textDisplayUpdate;
	
	public TextDisplayUpdate getTextDisplayUpdate() { return textDisplayUpdate; }

	private Context context;

	public Context getContext() {
		return context;
	}

	public UpdateOutlineItemTaskParameters(OutlineItem outlineItem, String newTitle, String newText, TextDisplayUpdate textDisplayUpdate, Context context) {
		this.outlineItem = outlineItem;
		this.newTitle = newTitle;
		this.newText = newText;
		this.textDisplayUpdate = textDisplayUpdate;
		this.context = context;
	}
}
