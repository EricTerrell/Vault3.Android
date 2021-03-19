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

public class RGBColor {
	private int red;
	
	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	private int green;

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	private int blue;

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}
	
	public RGBColor(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public boolean isDefaulted() {
		return red == 0 && green == 0 && blue == 0;
	}

	@Override
	public boolean equals(Object obj) {
		RGBColor rgbColor = (RGBColor) obj;
		
		return red == rgbColor.red && green == rgbColor.green && blue == rgbColor.blue;
	}
	
}
