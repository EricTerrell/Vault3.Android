/*
  Vault 3
  (C) Copyright 2022, Eric Bergman-Terrell
  
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

import commonCode.IPlatform.PlatformEnum;

import fonts.AndroidFont;
import fonts.FontList;
import fonts.IFont;

public class FontListInitializer {
	public static void initialize() {
		Map<String, IFont> availableFonts = enumerateAvailableFonts();
		PlatformEnum platform = PlatformEnum.Android;

		FontList.setAvailableFonts(availableFonts);
		FontList.setPlatform(platform);
	}

	private static Map<String, IFont> enumerateAvailableFonts() {
		AndroidFont[] fonts = new AndroidFont[] 
				{ 
					new AndroidFont("sans", 0.0f, 0), 
					new AndroidFont("serif", 0.0f, 0), 
					new AndroidFont("monospace", 0.0f, 0) 
				};

		Map<String, IFont> map = new HashMap<>(fonts.length);
		
		for (AndroidFont font : fonts) {
			map.put(font.getName(), font);
		}
		
		return map;
	}
}
