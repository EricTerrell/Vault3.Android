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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

// Based on http://www.codeproject.com/Tips/631965/Android-Edit-Text-with-Cross-Icon-x
public class ClearableEditText extends EditText {
	public ClearableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			this.setBackgroundResource(android.R.drawable.edit_text);
		}

		String value = "";
		final String viewMode = "editing";
		final String viewSide = "right";
		final Drawable x = getResources().getDrawable(R.drawable.clear_icon);

		x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
		Drawable x2 = viewMode.equals("never") ? null : viewMode
				.equals("always") ? x : viewMode.equals("editing") ? (value
				.equals("") ? null : x)
				: viewMode.equals("unlessEditing") ? (value.equals("") ? x
						: null) : null;
		
		// Display search icon in text field
		final Drawable searchIcon = getResources().getDrawable(
				android.R.drawable.ic_search_category_default);
		searchIcon.setBounds(0, 0, x.getIntrinsicWidth(),
				x.getIntrinsicHeight());

		setCompoundDrawables(searchIcon, null, viewSide.equals("right") ? x2
				: null, null);

		setOnTouchListener((v, event) -> {
			if (getCompoundDrawables()[viewSide.equals("left") ? 0 : 2] == null) {
				return false;
			}
			if (event.getAction() != MotionEvent.ACTION_UP) {
				v.performClick();

				return false;
			}
			// x pressed
			if ((viewSide.equals("left") && event.getX() < getPaddingLeft()
					+ x.getIntrinsicWidth())
					|| (viewSide.equals("right") && event.getX() > getWidth()
							- getPaddingRight() - x.getIntrinsicWidth())) {
				Drawable x3 = viewMode.equals("never") ? null : viewMode
						.equals("always") ? x
						: viewMode.equals("editing") ? null : viewMode
								.equals("unlessEditing") ? x : null;
				setText("");
				setCompoundDrawables(searchIcon, null,
						viewSide.equals("right") ? x3 : null, null);
			}

			return false;
		});
		addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Drawable x4 = viewMode.equals("never") ? null : viewMode
						.equals("always") ? x
						: viewMode.equals("editing") ? (getText().toString()
								.equals("") ? null : x) : viewMode
								.equals("unlessEditing") ? (getText()
								.toString().equals("") ? x : null) : null;
				setCompoundDrawables(ClearableEditText.this.getEditableText().toString().length() == 0 ? searchIcon : null, null,
						viewSide.equals("right") ? x4 : null, null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

}
