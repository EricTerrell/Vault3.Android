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

package com.ericbt.vault3base.async_tasks.search;

import com.ericbt.vault3base.Search;
import com.ericbt.vault3base.SearchActivity;

public class SearchTaskParameters {
	private final SearchActivity searchActivity;
	
	public SearchActivity getSearchActivity() { return searchActivity; }
	
	private final Search search;
	
	public Search getSearch() { return search; }
	
	public SearchTaskParameters(SearchActivity searchActivity, Search search) {
		this.searchActivity = searchActivity;
		this.search = search;
	}
}
