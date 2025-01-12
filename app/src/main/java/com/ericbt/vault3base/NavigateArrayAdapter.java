/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell
  
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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericbt.vault3base.async.workers.UpdateNavigateListItem;

public class NavigateArrayAdapter extends ArrayAdapter<OutlineItem> {
	private OutlineItem outlineItem;
	private final Context context;
	private boolean enabled;

	public NavigateArrayAdapter(Context context, int resource,
			int textViewResourceId, OutlineItem outlineItem) {
		super(context, resource, textViewResourceId, outlineItem.getChildren());

		this.outlineItem = outlineItem;
		this.context = context;
		enabled = true;
	}

	public OutlineItem getOutlineItem() {
		return outlineItem;
	}
	
	public void setOutlineItem(OutlineItem outlineItem) {
		((Vault3) getContext()).getNavigateListView().setFastScrollEnabled(false);
		
		this.outlineItem = outlineItem;

		((Vault3) getContext()).getNavigateListView().setFastScrollEnabled(false);

		clear();
		
		for (OutlineItem child : outlineItem.getChildren()) {
			this.add(child);
		}
		
		notifyDataSetChanged();
	}

	@Override
	public OutlineItem getItem(int position) {
		return outlineItem.getChildren().get(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        final OutlineItem outlineItem = getItem(position);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	convertView = infalInflater.inflate(R.layout.navigate_item, parent, false);
        }

    	final TextView textView = convertView.findViewById(R.id.Title);
    	textView.setText(outlineItem.getTitle());
    	
    	if (!enabled) {
    		textView.setTextColor(Color.DKGRAY);
    	}

    	// Allow items to flash when selected.
        convertView.setClickable(true); 
        convertView.setFocusable(true); 

    	final ImageView imageView = convertView.findViewById(R.id.HasChildren);
    	imageView.setEnabled(enabled);
    	imageView.setVisibility(outlineItem.getHasChildren() ? View.VISIBLE : View.INVISIBLE);

		// Item is selected if user is on a tablet in landscape orientation, and the item has been
		// clicked on previously.
		convertView.setBackgroundColor(
				outlineItem.isSelected() ? Color.BLUE : convertView.getSolidColor());

    	textView.setOnClickListener(v -> {
			if (isActivityEnabled()) {
				final Vault3 vault3 = (Vault3) this.getContext();

				if (vault3.getTextFragment() != null) {
					if (vault3.getParentLayout().isEnabled()) {
						vault3.getParentLayout().setBackgroundColor(Color.DKGRAY);
					}

					selectOutlineItem(outlineItem);
					TextActivity.addTextData(outlineItem, vault3.getTextFragment().getActivity().getIntent(), false);
					vault3.getTextFragment().update(true, outlineItem);
				} else {
					final Intent intent = new Intent(getContext(), TextActivity.class);
					TextActivity.addTextData(outlineItem, intent, false);
					vault3.startActivityForResult(intent, Vault3.TEXT);
				}
			}
		});

		textView.setOnLongClickListener(v -> false);

		imageView.setOnClickListener(v -> {
			if (isActivityEnabled() && outlineItem.getHasChildren()) {
				int firstVisibleItemPos = ((Vault3) getContext()).getNavigateListView().getFirstVisiblePosition();

				((Vault3) getContext()).saveScrollPosition(outlineItem.getParentId(), firstVisibleItemPos);

				((Vault3) getContext()).enable(false);

				new UpdateNavigateListItem().updateNavigateListItem(
						outlineItem.getId(), ((Vault3) getContext()));
			}
		});

		final ImageView build = convertView.findViewById(R.id.build);

		if (VaultPreferenceActivity.getDisplayWrenchIcon()) {
			build.setVisibility(View.VISIBLE);

			build.setOnClickListener(v ->
			{
				if (isActivityEnabled()) {
					v.performLongClick();
				}
			});
		}
		else {
			build.setVisibility(View.GONE);
		}

		convertView.setEnabled(enabled);
		
        return convertView;
	}

	public void selectOutlineItem(OutlineItem outlineItem) {
		for (int i = 0; i < getCount(); i++) {
			OutlineItem currentOutlineItem = getItem(i);

			currentOutlineItem.setSelected(currentOutlineItem.getId() == outlineItem.getId());
		}

		notifyDataSetChanged();
	}

	public OutlineItem getOutlineItem(int outlineItemId) {
		// Root item?
		if (outlineItemId == outlineItem.getId()) {
			return outlineItem;
		}

		// Child items?
		for (int i = 0; i < getCount(); i++) {
			OutlineItem outlineItem = getItem(i);

			if (outlineItem.getId() == outlineItemId) {
				return outlineItem;
			}
		}

		// Not found
		return null;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

		((Vault3) getContext()).setParentTitle(outlineItem.getTitle());
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean isActivityEnabled() {
		final Vault3 vault3 = (Vault3) this.getContext();

		return vault3.enabled;
	}
}
