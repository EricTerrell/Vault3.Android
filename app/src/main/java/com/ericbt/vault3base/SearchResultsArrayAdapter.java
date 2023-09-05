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

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Comparator;

public class SearchResultsArrayAdapter extends ArrayAdapter<SearchHit> {
	private final Context context;
	private final int textViewResourceId;
    private SearchActivity searchActivity;
	private final SearchHitComparitor searchHitComparitor = new SearchHitComparitor();

	public SearchResultsArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		
		this.context = context;
		this.textViewResourceId = textViewResourceId;
	}

    public void setSearchActivity(SearchActivity searchActivity) {
        this.searchActivity = searchActivity;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        if (convertView == null) {
        	convertView = infalInflater.inflate(R.layout.search_item, parent, false);
        }
        
        final SearchHit searchHit = getItem(position);

        TextView textView = convertView.findViewById(textViewResourceId);
    	textView.setText(searchHit.getHit().getTitle());
    	
    	textView.setOnClickListener(view -> {
            Intent intent = new Intent(SearchResultsArrayAdapter.this.getContext(), TextActivity.class);
            TextActivity.addTextData(searchHit.getHit(), intent, false);
            SearchResultsArrayAdapter.this.getContext().startActivity(intent);
        });

        ImageView viewList = convertView.findViewById(R.id.view_list);

        viewList.setOnClickListener(v -> searchActivity.goToList(searchHit.getParent().getId()));

        return convertView;
	}

    @Override
    public void add(@Nullable SearchHit object) {
        super.add(object);

        if (VaultPreferenceActivity.getSortSearchResults()) {
            sort(searchHitComparitor);
        }
    }

    @Override
    public void sort(@NonNull Comparator<? super SearchHit> comparator) {
        super.sort(comparator);
    }

}
