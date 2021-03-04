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

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends AsyncTaskActivity {
	private SearchResultsArrayAdapter searchResultsAdapter;
	private Button cancelButton, searchButton, searchOptions;
	private EditText searchText;
	private SearchTask searchTask;
    private Search search;
	private TextView searchHitsText;
	private boolean redoSearchOnResume = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(StringLiterals.LogTag, "SearchActivity.onCreate");

		if (Globals.getApplication().getVaultDocument() == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.search);
		
		setTitle(String.format("%s - Search", getString(R.string.app_name)));

        ListView searchResultsListView = (ListView) findViewById(R.id.SearchResultsListView);
		
		searchResultsAdapter = new SearchResultsArrayAdapter(this, R.id.SearchResultsTextView);
		searchResultsListView.setAdapter(searchResultsAdapter);

		searchResultsAdapter.setSearchActivity(this);
		
		searchHitsText = (TextView) findViewById(R.id.SearchHitsText);
		
		cancelButton = (Button) findViewById(R.id.Cancel);

		searchButton = (Button) findViewById(R.id.Go);
		
		searchOptions = (Button) findViewById(R.id.SearchOptions);
		
		searchOptions.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SearchActivity.this, SearchOptionsActivity.class);
                startActivity(intent);     
			}
		});
		
		search = new Search();
		searchText = (EditText) findViewById(R.id.SearchText);
		
		searchText.setText(VaultPreferenceActivity.getSearchText());
		
		search.setSearchText(searchText.getEditableText().toString());
		
		searchText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				enableSearchButton(true);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// Initiate a search when user types ENTER.
		searchText.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		    	boolean result = false;
		    	
		        // If the event is a key-down event on the "enter" button
		        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && searchButton.isEnabled() && searchButton.getVisibility() == View.VISIBLE) {
		        	searchButton.performClick();
		        	result = true;
		        }
		        
		        return result;
		    }
		});
		
		enableSearchButton(true);
		
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doSearch();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				redoSearchOnResume = false;

				if (searchTask != null) {
					searchTask.cancel(true);
					searchTask = null;
				}
			}
		});

		if (savedInstanceState != null) {
			redoSearchOnResume = savedInstanceState.getBoolean(StringLiterals.RedoSearchOnResume);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (redoSearchOnResume) {
			doSearch();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(StringLiterals.RedoSearchOnResume, redoSearchOnResume);
	}

	private void doSearch() {
		if (searchButton.isEnabled()) {
			redoSearchOnResume = true;
			
			searchHitsText.setVisibility(View.GONE);
			
			searchResultsAdapter.clear();
			
			search.setMatchCase(VaultPreferenceActivity.getSearchMatchCase());
			search.setMatchWholeWord(VaultPreferenceActivity.getSearchMatchWholeWords());
			search.setSearchFields(VaultPreferenceActivity.getSearchFields());
			search.setSearchScope(VaultPreferenceActivity.getSearchScope());
			search.setSearchScopeID(getIntent().getExtras().getInt(StringLiterals.SelectedOutlineItemId));
			search.setMustFind(VaultPreferenceActivity.getSearchMustFind());
			
			search.prepareToSearch();
			
			VaultPreferenceActivity.setSearchText(searchText.getEditableText().toString());
	
			setEnabled(false);
			
			searchTask = new SearchTask();
			searchTask.execute(new SearchTaskParameters(SearchActivity.this, search));
		}
	}

	private void enableSearchButton(boolean enable) {
		String text = searchText.getEditableText().toString().trim();
		
		search.setSearchText(text);
		
		searchButton.setEnabled(enable && Globals.getApplication().getVaultDocument() != null && text.length() > 0);
	}
	
	public void searchCompleted() {
		searchTask = null;
		setEnabled(true);
	}
	
	public void update(SearchHit searchHit) {
		searchResultsAdapter.add(searchHit);
		
		if (searchHitsText.getVisibility() != View.VISIBLE) {
			searchHitsText.setVisibility(View.VISIBLE);
		}
		
		int hits = searchResultsAdapter.getCount();
		int maxHits = VaultPreferenceActivity.getMaxSearchHits();
		
		searchHitsText.setText(String.format("Hits: %d Max: %d", hits, maxHits));
	}

	private void enable(boolean enabled, boolean enableCancelButton) {
		getActionBar().setDisplayHomeAsUpEnabled(enabled);
		
		searchButton.setEnabled(enabled && searchText.getEditableText().toString().trim().length() > 0);
		
		searchText.setEnabled(enabled);

		cancelButton.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
		cancelButton.setEnabled(enableCancelButton);
		searchOptions.setEnabled(enabled);
	}
	
	public void enable(boolean enabled) {
		enable(enabled, !enabled);
	}
	
	public void goToList(int outlineItemParentId) {
		Intent returnData = new Intent();
		returnData.putExtra(StringLiterals.SelectedOutlineItemParentId, outlineItemParentId);

		if (searchTask != null) {
			searchTask.cancel(true);
		}

		setResult(RESULT_OK, returnData);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (enabled && item.getItemId() == android.R.id.home) {
			finish();
			
			result = true;
		}
		
		return result;
	}

}
