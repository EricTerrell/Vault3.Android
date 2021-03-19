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

import android.content.Context;

import fonts.AndroidFont;

public class UpdateFontTaskParameters {
	private AndroidFont font;

	public AndroidFont getFont() {
		return font;
	}

	public void setFont(AndroidFont font) {
		this.font = font;
	}
	
	private OutlineItem outlineItem;

	public OutlineItem getOutlineItem() {
		return outlineItem;
	}

	public void setOutlineItem(OutlineItem outlineItem) {
		this.outlineItem = outlineItem;
	}
	
	private int color;

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
	
	private final TextDisplayUpdate textDisplayUpdate;
	
	public TextDisplayUpdate getTextDisplayUpdate() { return textDisplayUpdate; }

	private final Context context;

	public Context getContext() {
		return context;
	}

	public UpdateFontTaskParameters(AndroidFont font, OutlineItem outlineItem, int color, TextDisplayUpdate textDisplayUpdate, Context context) {
		this.font = font;
		this.outlineItem = outlineItem;
		this.color = color;
		this.textDisplayUpdate = textDisplayUpdate;
		this.context = context;
	}
}
