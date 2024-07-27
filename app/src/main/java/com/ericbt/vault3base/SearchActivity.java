/*
  Vault 3
  (C) Copyright 2024, Eric Bergman-Terrell
  
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ericbt.vault3base.async.workers.Search;

public class SearchActivity extends AsyncTaskActivity {
	private SearchResultsArrayAdapter searchResultsAdapter;
	private Button cancelButton, searchButton, searchOptions;
	private EditText searchText;
	private Search search;
    private SearchParameters searchParameters;
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

        ListView searchResultsListView = findViewById(R.id.SearchResultsListView);
		
		searchResultsAdapter = new SearchResultsArrayAdapter(this, R.id.SearchResultsTextView);
		searchResultsListView.setAdapter(searchResultsAdapter);

		searchResultsAdapter.setSearchActivity(this);
		
		searchHitsText = findViewById(R.id.SearchHitsText);
		
		cancelButton = findViewById(R.id.Cancel);

		searchButton = findViewById(R.id.Go);
		
		searchOptions = findViewById(R.id.SearchOptions);
		
		searchOptions.setOnClickListener(v -> {
			Intent intent = new Intent(SearchActivity.this, SearchOptionsActivity.class);
startActivity(intent);
		});
		
		searchParameters = new SearchParameters();
		searchText = findViewById(R.id.SearchText);
		
		searchText.setText(VaultPreferenceActivity.getSearchText());
		
		searchParameters.setSearchText(searchText.getEditableText().toString());
		
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
		searchText.setOnKeyListener((v, keyCode, event) -> {
			boolean result = false;

			// If the event is a key-down event on the "enter" button
			if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && searchButton.isEnabled() && searchButton.getVisibility() == View.VISIBLE) {
				searchButton.performClick();
				result = true;
			}

			return result;
		});
		
		enableSearchButton(true);
		
		searchButton.setOnClickListener(v -> doSearch());
		
		cancelButton.setOnClickListener(v -> {
			redoSearchOnResume = false;

			if (search != null) {
				search.cancel();
				search = null;
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
			
			searchParameters.setMatchCase(VaultPreferenceActivity.getSearchMatchCase());
			searchParameters.setMatchWholeWord(VaultPreferenceActivity.getSearchMatchWholeWords());
			searchParameters.setSearchFields(VaultPreferenceActivity.getSearchFields());
			searchParameters.setSearchScope(VaultPreferenceActivity.getSearchScope());
			searchParameters.setSearchScopeID(getIntent().getExtras().getInt(StringLiterals.SelectedOutlineItemId));
			searchParameters.setMustFind(VaultPreferenceActivity.getSearchMustFind());
			
			searchParameters.prepareToSearch();
			
			VaultPreferenceActivity.setSearchText(searchText.getEditableText().toString());
	
			setEnabled(false);
			
			search = new Search();
			search.search(this, searchParameters);
		}
	}

	private void enableSearchButton(boolean enable) {
		final String text = searchText.getEditableText().toString().trim();
		
		searchParameters.setSearchText(text);
		
		searchButton.setEnabled(
				enable && Globals.getApplication().getVaultDocument() != null && !text.isEmpty());
	}
	
	public void searchCompleted() {
		search = null;
		setEnabled(true);
	}
	
	public void update(SearchHit searchHit) {
		searchResultsAdapter.add(searchHit);
		
		if (searchHitsText.getVisibility() != View.VISIBLE) {
			searchHitsText.setVisibility(View.VISIBLE);
		}
		
		final int hits = searchResultsAdapter.getCount();
		final int maxHits = VaultPreferenceActivity.getMaxSearchHits();
		
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
		final Intent returnData = new Intent();
		returnData.putExtra(StringLiterals.SelectedOutlineItemParentId, outlineItemParentId);

		if (search != null) {
			search.cancel();
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
