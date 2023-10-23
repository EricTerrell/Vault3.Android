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

public class TableAndColumnNames {
	public static class OutlineItem {
		public static final String TableName = "OutlineItem";
		
		public static final String ID        = "ID";
		public static final String ParentID  = "ParentID";

		public static final String Title     = "Title";
		public static final String TitleSalt = "TitleSalt";
		public static final String TitleIV   = "TitleIV";

		public static final String Text      = "Text";
		public static final String TextSalt  = "TextSalt";
		public static final String TextIV    = "TextIV";

		public static final String SortOrder = "SortOrder";
		public static final String FontList  = "FontList";
		public static final String Red       = "Red";
		public static final String Green     = "Green";
		public static final String Blue      = "Blue";
	}
	
	public static class VaultDocumentInfo {
		public static final String TableName = "VaultDocumentInfo";

		public static final String Name      = "Name";
		public static final String Value     = "Value";
	}
}
