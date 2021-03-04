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

import java.util.HashMap;
import java.util.Map;

import fonts.AndroidFont;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

public class FontUtils {
	private static Map<String, Integer> colorMap;

	private final static float pointsPerInch = 72.0f;
	
	private final static AndroidFont defaultFont = new AndroidFont("sans", 6.5f, Typeface.NORMAL); 

	static {
		colorMap = new HashMap<>();
		
		colorMap.put("Black", Color.BLACK);
		colorMap.put("Blue", Color.BLUE);
		colorMap.put("Cyan", Color.CYAN);
		colorMap.put("Dark Gray", Color.DKGRAY);
		colorMap.put("Gray", Color.GRAY);
		colorMap.put("Light Gray", Color.LTGRAY);
		colorMap.put("Green", Color.GREEN);
		colorMap.put("Magenta", Color.MAGENTA);
		colorMap.put("Yellow", Color.YELLOW);
		colorMap.put("Red", Color.RED);
	}

	public static Integer getColor(String colorName) {
		return colorMap.get(colorName);
	}
	
	public static AndroidFont getDefaultFont() {
		return defaultFont; 
	}

	public static int pointsToPixels(float points) {
		final DisplayMetrics displayMetrics = Globals.getApplication().getApplicationContext().getResources().getDisplayMetrics();

		return (int) (points / pointsPerInch * displayMetrics.densityDpi);
	}
	
	public static float pixelsToPoints(int pixels) {
		final DisplayMetrics displayMetrics = Globals.getApplication().getApplicationContext().getResources().getDisplayMetrics();

		return (int) (((float) pixels / (float) displayMetrics.densityDpi) * pointsPerInch);
	}
	
	public static int getTextStyle(String styleText) {
		int style = Typeface.NORMAL;

        switch (styleText) {
            case "Normal":
                style = Typeface.NORMAL;
                break;
            case "Bold":
                style = Typeface.BOLD;
                break;
            case "Italic":
                style = Typeface.ITALIC;
                break;
            case "Bold & Italic":
                style = Typeface.BOLD_ITALIC;
                break;
        }
		
		return style;
	}
}
