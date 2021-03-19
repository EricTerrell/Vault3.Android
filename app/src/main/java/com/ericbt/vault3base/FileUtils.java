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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
	public static String getFileType(File file) {
		String fileName = file.getName();
		
		return getFileType(fileName);
	}
	
	private static String getFileType(String fileName) {
		String fileType = null;
		
		int index = fileName.lastIndexOf('.');
		
		if (index >= 0) {
			fileType = fileName.substring(index + 1);
		}
		
		return fileType;
	}
	
	/**
	 * Copies the specified file
	 * @param srcPath path of file to be copied
	 * @param destPath path to where file will be copied
	 * @throws IOException
	 */
	public static void copyFile(String srcPath, String destPath) throws IOException {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		
		try {
			inputStream = new FileInputStream(srcPath);
			outputStream = new FileOutputStream(destPath);
	    
	        byte[] buffer = new byte[1024];
	        int length;
	        
	        while ((length = inputStream.read(buffer)) > 0) {
	            outputStream.write(buffer, 0, length);
	        }

	        outputStream.flush();
		}
		finally {
			if (inputStream != null) {
				inputStream.close();
			}
			
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}
}
