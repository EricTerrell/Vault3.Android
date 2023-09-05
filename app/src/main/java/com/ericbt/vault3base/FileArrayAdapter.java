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

import java.io.File;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.documentfile.provider.DocumentFile;

public class FileArrayAdapter extends ArrayAdapter<DocumentFile> {
	private final Context context;
	private final int resource;
	private final int textViewResourceId;
	private boolean enabled;
	
	public FileArrayAdapter(Context context, int resource, int textViewResourceId) {
		super(context, textViewResourceId);
		
		this.context = context;
		this.resource = resource;
		this.textViewResourceId = textViewResourceId;
		
		enabled = true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	convertView = infalInflater.inflate(resource, null);
        }
        
        final DocumentFile documentFile = getItem(position);
		final String text = documentFile.getName();

        TextView textView = convertView.findViewById(textViewResourceId);
    	textView.setText(new File(text).getName());
    	
		textView.setTextColor(enabled ? Color.WHITE : Color.DKGRAY);

		View display = convertView.findViewById(R.id.display);

		display.setOnClickListener(v -> v.performLongClick());
    	
        return convertView;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		notifyDataSetChanged();
	}
}
