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
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import android.util.Log;

public class Search {
	private String searchText;
	
	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public enum SearchFields { Titles, TitlesAndText }

	private SearchFields searchFields;

	public SearchFields getSearchFields() {
		return searchFields;
	}

	public void setSearchFields(SearchFields searchFields) {
		this.searchFields = searchFields;
	}

	public enum SearchScope { SelectedOnly, All }
	
	private SearchScope searchScope;
	
	public SearchScope getSearchScope() {
		return searchScope;
	}

	public void setSearchScope(SearchScope searchScope) {
		this.searchScope = searchScope;
	}
	
	private int searchScopeID = -1;
	
	public int getSearchScopeID() {
		return searchScopeID;
	}

	public void setSearchScopeID(int searchScopeID) {
		this.searchScopeID = searchScopeID;
	}

	public enum MustFind { AtLeastOneWordOrPhrase, AllWordsAndPhrases }
	
	private MustFind mustFind;
	
	public void setMustFind(MustFind mustFind) {
		this.mustFind = mustFind;
	}

	private boolean matchWholeWord;
	
	public void setMatchWholeWord(boolean matchWholeWord) {
		this.matchWholeWord = matchWholeWord;
	}

	private boolean matchCase;
	
	public void setMatchCase(boolean matchCase) {
		this.matchCase = matchCase;
	}

	private String[] getSearchTokens() {
		// Ensure that double quotes are paired.
		int doubleQuotes = 0;
		
		for (int i = 0; i < searchText.length(); i++) {
			if (searchText.charAt(i) == '"') {
				doubleQuotes++;
			}
		}
		
		// If there are an odd number of double quotes, add one to the end.
		if ((doubleQuotes % 2) == 1) {
			searchText += '"';
		}
		
		StringBuilder unquotedText = new StringBuilder();

		StringBuilder quotedText = new StringBuilder();
		List<String> quotedTextList = new ArrayList<>();
		
		boolean insideQuotes = false;
		
		for (int i = 0; i < searchText.length(); i++) {
			char ch = searchText.charAt(i);

			if (ch == '"') {
				if (insideQuotes) {
					quotedTextList.add(quotedText.toString());
					quotedText.setLength(0);
					unquotedText.append(' ');
				}
				
				insideQuotes = !insideQuotes;
			}
			else {
				if (!insideQuotes) {
					unquotedText.append(ch);
				}
				else {
					quotedText.append(ch);
				}
			}
		}

		// Create a list containing all tokens, quoted and unquoted.
		final List<String> tokenList = new ArrayList<>(quotedTextList);
		
		final String[] unquotedTokens = unquotedText.toString().split(" ");

        Collections.addAll(tokenList, unquotedTokens);

		final List<String> finalTokenList = new ArrayList<>();
		
		for (String token : tokenList) {
			token = token.trim();
			
			// Remove empty and blank tokens.
			if (token.length() > 0) {
				finalTokenList.add(token);
			}
		}

		return finalTokenList.toArray(new String[0]);
	}

	/**
	 * Return an array of Pattern objects based on the search text and search options
	 * @return array of Pattern objects
	 */
	private Pattern[] getSearchPatterns() {
		String[] searchTokens = getSearchTokens();
		
		Log.i(StringLiterals.LogTag, "Search Tokens:");
		
		for (String searchToken : searchTokens) {
			Log.i(StringLiterals.LogTag, searchToken);
		}
	
		List<Pattern> patterns = new ArrayList<>(searchTokens.length);
		
		Log.i(StringLiterals.LogTag, "Search regular expressions:");
		
		String wordBoundary = matchWholeWord ? "\\b" : "";
		
		for (String searchToken : searchTokens) {
			Pattern pattern = matchCase ? Pattern.compile(wordBoundary + Pattern.quote(searchToken) + wordBoundary) : Pattern.compile(wordBoundary + Pattern.quote(searchToken) + wordBoundary, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			patterns.add(pattern);
			
			Log.i(StringLiterals.LogTag, searchToken);
		}
		
		return patterns.toArray(new Pattern[0]);
	}
	
	private Pattern[] searchPatterns;

	public void prepareToSearch() {
		searchPatterns = getSearchPatterns();
	}
	
	private boolean isMatch(String text) {
		boolean isMatch;

		int matches = 0;

        for (Pattern searchPattern : searchPatterns) {
            if (searchPattern.matcher(text).find()) {
                matches++;
            } else if (mustFind == MustFind.AllWordsAndPhrases) {
                break;
            }
        }
		
		isMatch = (mustFind == MustFind.AllWordsAndPhrases && matches == searchPatterns.length) || (mustFind == MustFind.AtLeastOneWordOrPhrase && matches > 0);
		
		return isMatch;
	}
	
	public boolean isMatch(OutlineItem outlineItem) {
		return isMatch(outlineItem.getTitle()) || (searchFields == SearchFields.TitlesAndText && isMatch(outlineItem.getText()));
	}
}
