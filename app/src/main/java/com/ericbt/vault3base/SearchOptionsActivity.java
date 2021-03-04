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

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.ericbt.vault3base.Search.MustFind;
import com.ericbt.vault3base.Search.SearchFields;
import com.ericbt.vault3base.Search.SearchScope;

public class SearchOptionsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_options_dialog);
        
        setTitle(String.format("%s: Search Options", getString(R.string.app_name)));

        final Spinner searchScope = (Spinner) findViewById(R.id.SearchScope);
        searchScope.setSelection(VaultPreferenceActivity.getSearchScope() == SearchScope.SelectedOnly ? 0 : 1);
        
        final Spinner searchFields = (Spinner) findViewById(R.id.SearchFields);
        searchFields.setSelection(VaultPreferenceActivity.getSearchFields() == SearchFields.Titles ? 0 : 1);
        
        final Spinner mustFind = (Spinner) findViewById(R.id.MustFind);
        mustFind.setSelection(VaultPreferenceActivity.getSearchMustFind() == MustFind.AtLeastOneWordOrPhrase ? 0 : 1);
        
        final CheckBox matchWholeWords = (CheckBox) findViewById(R.id.MatchWholeWords);
        matchWholeWords.setChecked(VaultPreferenceActivity.getSearchMatchWholeWords());
        
        final CheckBox matchCase = (CheckBox) findViewById(R.id.MatchCase);
        matchCase.setChecked(VaultPreferenceActivity.getSearchMatchCase());
        
        final Spinner maxSearchHitsSpinner = (Spinner) findViewById(R.id.MaxSearchHits);
        String maxSearchHits = String.valueOf(VaultPreferenceActivity.getMaxSearchHits());
        
        for (int i = 0; i < maxSearchHitsSpinner.getCount(); i++) {
        	String value = (String) maxSearchHitsSpinner.getItemAtPosition(i);
        	
        	if (value.equals(maxSearchHits)) {
        		maxSearchHitsSpinner.setSelection(i);
        		break;
        	}
        }
        
        final CheckBox sortSearchResults = findViewById(R.id.SortSearchResults);
        
        sortSearchResults.setChecked(VaultPreferenceActivity.getSortSearchResults());
        
        Button okButton = (Button) findViewById(R.id.OKButton);
       
        okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				VaultPreferenceActivity.setSearchScope(searchScope.getSelectedItemPosition() == 0 ? Search.SearchScope.SelectedOnly : Search.SearchScope.All);
				VaultPreferenceActivity.setSearchFields(searchFields.getSelectedItemPosition() == 0 ? Search.SearchFields.Titles : Search.SearchFields.TitlesAndText);
				VaultPreferenceActivity.setSearchMustFind(mustFind.getSelectedItemPosition() == 0 ? Search.MustFind.AtLeastOneWordOrPhrase : Search.MustFind.AllWordsAndPhrases);
				
				VaultPreferenceActivity.setSearchMatchWholeWorlds(matchWholeWords.isChecked());
				VaultPreferenceActivity.setSearchMatchCase(matchCase.isChecked());

				int maxSearchHits = Integer.valueOf((String) maxSearchHitsSpinner.getSelectedItem());
				VaultPreferenceActivity.setMaxSearchHits(maxSearchHits);
				
				VaultPreferenceActivity.setSortSearchResults(sortSearchResults.isChecked());
				
				finish();
			}
		});
        
        Button cancelButton = (Button) findViewById(R.id.CancelButton);
        
        cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			finish();
			
			result = true;
		}
		
		return result;
	}

}
