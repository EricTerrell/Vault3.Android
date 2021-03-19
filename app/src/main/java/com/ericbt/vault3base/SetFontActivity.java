/*
  Vault 3
  (C) Copyright 2021, Eric Bergman-Terrell
  
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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class SetFontActivity extends Activity {
	private Spinner fontSpinner, fontStyleSpinner, fontSizeSpinner, colorSpinner;
	private TextView preview;
	private String typefaceName;
	private int sizeInPoints;
	private int sizeInPixels;
	private int fontStyle;
	private int fontColor;

    private static final String Sans      = "sans";
	private static final String Serif     = "serif";
	private static final String Monospace = "monospace";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Globals.getApplication().getVaultDocument() == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.set_font_dialog);
        
		setTitle(String.format("%s - Set Font", getString(R.string.app_name)));
		
		fontSpinner = (Spinner) findViewById(R.id.Font);
		
		fontSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String font = (String) parent.getItemAtPosition(pos);

                switch (font) {
                    case "Sans Serif":
                        typefaceName = Sans;
                        break;
                    case "Serif":
                        typefaceName = Serif;
                        break;
                    case "Monospace":
                        typefaceName = Monospace;
                        break;
                }
				
				updatePreview();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		fontStyleSpinner = (Spinner) findViewById(R.id.FontStyle);
		
		fontStyleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				fontStyle = FontUtils.getTextStyle((String) parent.getItemAtPosition(pos));
				
				updatePreview();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		fontSizeSpinner = (Spinner) findViewById(R.id.FontSize);
		
		fontSizeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				String size = (String) parent.getItemAtPosition(pos);
				String[] sizeParts = size.split(" ");
				
				sizeInPoints = Integer.parseInt(sizeParts[0]);
				
				sizeInPixels = FontUtils.pointsToPixels(sizeInPoints);
				
				updatePreview();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		colorSpinner = (Spinner) findViewById(R.id.Color);
		
		colorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				
				String colorName = (String) parent.getItemAtPosition(pos);

				fontColor = FontUtils.getColor(colorName);

				updatePreview();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		preview = (TextView) findViewById(R.id.Preview);

        Button okButton = (Button) findViewById(R.id.OKButton);
		
		okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnData = new Intent();
                returnData.putExtra(StringLiterals.NewFontName, SetFontActivity.this.typefaceName);
                returnData.putExtra(StringLiterals.NewFontSizeInPoints, (float) SetFontActivity.this.sizeInPoints);
                returnData.putExtra(StringLiterals.NewFontStyle, SetFontActivity.this.fontStyle);
                returnData.putExtra(StringLiterals.NewFontColor, SetFontActivity.this.fontColor);
                setResult(RESULT_OK, returnData);

                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.CancelButton);
		
		cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
		
		setUIForCurrentFontAndColor();
	}

	private void setUIForCurrentFontAndColor() {
		String fontName = getIntent().getExtras().getString(StringLiterals.FontName);

        switch (fontName) {
            case Sans:
                fontSpinner.setSelection(0);
                break;
            case Serif:
                fontSpinner.setSelection(1);
                break;
            case Monospace:
                fontSpinner.setSelection(2);
                break;
        }
		
		int fontSize = (int) getIntent().getExtras().getFloat(StringLiterals.FontSizeInPoints);
		
		for (int i = 0; i < fontSizeSpinner.getCount(); i++) {
			String currentItem = (String) fontSizeSpinner.getItemAtPosition(i);

			String[] sizeParts = currentItem.split(" ");
			int currentSize = Integer.parseInt(sizeParts[0]);
			
			if (currentSize == fontSize) {
				fontSizeSpinner.setSelection(i);
				break;
			}
		}
		
		int fontStyle = getIntent().getExtras().getInt(StringLiterals.FontStyle);
		
		if (fontStyle == Typeface.NORMAL) {
			fontStyleSpinner.setSelection(0);
		}
		else if (fontStyle == Typeface.BOLD) {
			fontStyleSpinner.setSelection(1);
		}
		else if (fontStyle == Typeface.ITALIC) {
			fontStyleSpinner.setSelection(2);
		}
		else if (fontStyle == Typeface.BOLD_ITALIC) {
			fontStyleSpinner.setSelection(3);
		}

		int red = getIntent().getExtras().getInt(StringLiterals.Red);
		int green = getIntent().getExtras().getInt(StringLiterals.Green);
		int blue = getIntent().getExtras().getInt(StringLiterals.Blue);
		
		RGBColor initialColor = new RGBColor(red, green, blue);
		
		for (int i = 0; i < colorSpinner.getCount(); i++) {
			String colorName = (String) colorSpinner.getItemAtPosition(i);
			
			int color = FontUtils.getColor(colorName);
			RGBColor currentColor = new RGBColor(Color.red(color), Color.green(color), Color.blue(color));
			
			if (initialColor.equals(currentColor)) {
				colorSpinner.setSelection(i);
				break;
			}
		}
	}
	
	private void updatePreview() {
		int width = preview.getWidth();
		int height = preview.getHeight();
		
		Typeface typeface = Typeface.create(typefaceName, fontStyle);

		preview.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeInPixels);
		preview.setTypeface(typeface, fontStyle);
		
		preview.setTextColor(fontColor);
		
		preview.setWidth(width);
		preview.setHeight(height);
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
