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

public class SearchHit {
	private OutlineItem parent;

	public OutlineItem getParent() {
		return parent;
	}

	public void setParent(OutlineItem parent) {
		this.parent = parent;
	}
	
	private OutlineItem hit;

	public OutlineItem getHit() {
		return hit;
	}

	public void setHit(OutlineItem hit) {
		this.hit = hit;
	}
	
	public SearchHit(OutlineItem hit, OutlineItem parent) {
		this.hit = hit;
		this.parent = parent;
	}
}
