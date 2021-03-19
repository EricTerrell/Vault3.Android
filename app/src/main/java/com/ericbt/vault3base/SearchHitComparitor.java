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

import java.text.Collator;
import java.util.Comparator;

public class SearchHitComparitor implements Comparator<SearchHit> {
	private final Collator collator = Collator.getInstance();
	
	@Override
	public int compare(SearchHit searchHit1, SearchHit searchHit2) {
		return collator.compare(searchHit1.getHit().getTitle(), searchHit2.getHit().getTitle());
	}
}
