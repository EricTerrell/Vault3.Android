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

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import fonts.AndroidFont;
import fonts.FontList;

public class TextActivity extends AsyncTaskActivity implements TextDisplayUpdate {
	private TextFragment textFragment;

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);

		if (fragment instanceof TextFragment) {
			textFragment = (TextFragment) fragment;
		}
	}

	public static void addTextData(OutlineItem outlineItem, Intent intent, boolean currentItemIsParent) {
		intent.putExtra(StringLiterals.OutlineItemId, outlineItem.getId());

		if (currentItemIsParent) {
			intent.putExtra(StringLiterals.OutlineItemParentId, outlineItem.getId());
		}
		else {
			intent.putExtra(StringLiterals.OutlineItemParentId, outlineItem.getParentId());
		}

		intent.putExtra(StringLiterals.Title, outlineItem.getTitle());
		intent.putExtra(StringLiterals.Text, outlineItem.getText());

		AndroidFont font = outlineItem.getFont();

		if (font != null) {
			intent.putExtra(StringLiterals.FontName, font.getName());
			intent.putExtra(StringLiterals.FontSizeInPoints, font.getSizeInPoints());
			intent.putExtra(StringLiterals.FontStyle, font.getStyle());
		}

		RGBColor color = outlineItem.getColor();

		intent.putExtra(StringLiterals.Red, color.getRed());
		intent.putExtra(StringLiterals.Green, color.getGreen());
		intent.putExtra(StringLiterals.Blue, color.getBlue());

		String fontList = FontList.serialize(outlineItem.getFontList());
		intent.putExtra(StringLiterals.FontList, fontList);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(StringLiterals.LogTag, "TextActivity.onCreate");

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.text);

		if (Globals.getApplication().getVaultDocument() == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		setTitle(String.format("%s - Text", getString(R.string.app_name)));

		enable(true);

		setResult(RESULT_OK, getIntent());
	}

	public void enable(boolean enabled) {
		textFragment.enable(enabled);
	}
	
	private void update(boolean enable, OutlineItem outlineItem) {
		textFragment.update(enable, outlineItem);
	}
	
	public void update(OutlineItem outlineItem) {
		update(true, outlineItem);
	}

	@Override
	public AsyncTaskActivity getAsyncTaskActivity() {
		return this;
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
