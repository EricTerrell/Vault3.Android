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

public class DocumentAction {
	private String dbFilePath;
	
	public String getDbFilePath() { return dbFilePath; }

	public enum Action {
		Load, Close
	}

	private Action action;
	
	public Action getAction() { return action; }

	private int outlineItemId;
	
	public int getOutlineId() { return outlineItemId; }
	
	private String password; 
	
	public String getPassword() { return password; }

	public DocumentAction(String dbFilePath, Action action, int outlineItemId, String password) {
		this.dbFilePath = dbFilePath;
		this.action = action;
		this.outlineItemId = outlineItemId;
		this.password = password;
	}
}
