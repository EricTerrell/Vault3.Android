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

import java.util.ArrayList;
import java.util.List;

import fonts.AndroidFont;
import fonts.FontList;

public class OutlineItem {
	public final static int ROOT_ID = 1;

	private int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private String title;
	
	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return title != null ? title : "";
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private String text;

	public String getText() {
		return text != null ? text : "";
	}

	public void setText(String text) {
		this.text = text;
	}

	private FontList fontList;
	
	public FontList getFontList() {
		return fontList;
	}

	public void setFontList(FontList fontList) {
		this.fontList = fontList;
	}

	private boolean selected;

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	/**
	 * Return the most recently used font for this platform that is actually available on this platform. 
	 * If no such font exists, return the default font.
	 * @return font to use to render this outline item.
	 */
	public AndroidFont getFont() {
		AndroidFont font;
		
		if (fontList != null) {
			font = (AndroidFont) fontList.getFont();
		}
		else if (VaultPreferenceActivity.useDefaultTextFontAndColor()) {
			font = VaultPreferenceActivity.getDefaultTextFont();	
		}
		else {
			font = FontUtils.getDefaultFont();
		}
		
		return font;
	}
	
	private RGBColor color;
	
	public RGBColor getColor() {
		RGBColor color = this.color;
		
		if (color != null && color.isDefaulted() && VaultPreferenceActivity.useDefaultTextFontAndColor()) {
			final RGBColor newColor = VaultPreferenceActivity.getDefaultTextFontColor();
			
			if (newColor != null) {
				color = newColor;
			}
		}
		
		return color;
	}

	public void setColor(RGBColor color) {
		this.color = color;
	}

	private final List<OutlineItem> children;

	public OutlineItem() {
		children = new ArrayList<>();
	}

	private int parentId;
	
	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	private int sortOrder;
	
	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public void addChild(OutlineItem childNode) {
		children.add(childNode);
	}
	
	public List<OutlineItem> getChildren() {
		return children;
	}

	private boolean hasChildren;
	
	public boolean getHasChildren() {
		return hasChildren;
	}
	
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	
	public boolean isRoot() {
		return id == ROOT_ID;
	}
}
