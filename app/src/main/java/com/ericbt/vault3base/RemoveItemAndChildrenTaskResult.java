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

import java.util.List;

public class RemoveItemAndChildrenTaskResult {
	private OutlineItem parentOutlineItem;

	public OutlineItem getParentOutlineItem() {
		return parentOutlineItem;
	}

	public void setParentOutlineItem(OutlineItem parentOutlineItem) {
		this.parentOutlineItem = parentOutlineItem;
	}
	
	private List<Integer> outlineItemsRemoved;
	
	public List<Integer> getOutlineItemsRemoved() {
		return outlineItemsRemoved;
	}

	public void setOutlineItemsRemoved(List<Integer> outlineItemsRemoved) {
		this.outlineItemsRemoved = outlineItemsRemoved;
	}

	private Throwable exception;

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}
	
	private Throwable fatalException;

	public Throwable getFatalException() {
		return fatalException;
	}

	public void setFatalException(Throwable fatalException) {
		this.fatalException = fatalException;
	}
}
