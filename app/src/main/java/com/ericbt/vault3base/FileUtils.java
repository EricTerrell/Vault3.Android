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
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class FileUtils {
	public static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		long bytes = 0;

		byte[] buffer = new byte[1024];
		int length;

		while ((length = inputStream.read(buffer)) > 0) {
			bytes += length;

			outputStream.write(buffer, 0, length);
		}

		return bytes;
	}

	public static boolean deleteDatabaseFile(String databasePath) {
		boolean deleted = false;

		try {
			final File databaseFile = new File(databasePath);

			// Delete database file
			deleted = databaseFile.delete();

			final String journalFilePath =
					String.format("%s%s", databasePath, StringLiterals.JournalSuffix);

			final File journalFile = new File(journalFilePath);

			if (journalFile.exists()) {
				// Delete journal file
				final boolean journalFileDeleted = journalFile.delete();

				if (!journalFileDeleted) {
					Log.w(StringLiterals.LogTag,
							String.format("Could not delete %s", journalFile.getPath()));
				}
			}
		} catch (Throwable ex) {
			Log.w(StringLiterals.LogTag,
					String.format("Could not delete %s", databasePath));
		}

		return deleted;
	}

	public static void deleteAllTempFiles(Context context) {
		final File folder = DocumentFileUtils.getTempFolderPath(context);

		Log.i(StringLiterals.LogTag, "FileUtils.deleteAllTempFiles");

		for (final File file : folder.listFiles(new TempFileFilter())) {
			try {
				final boolean deleted = file.delete();

				Log.i(StringLiterals.LogTag,
						String.format(
								"FileUtils.deleteAllTempFiles: Deleting %s",
								file.getAbsolutePath()));

				if (!deleted) {
					Log.e(StringLiterals.LogTag, String.format("FileUtils.deleteAllTempFiles: cannot delete %s", file.getPath()));
				}
			} catch (Throwable ex) {
				Log.e(StringLiterals.LogTag, String.format("FileUtils.deleteAllTempFiles: cannot delete %s ex: %s", file.getPath(), ex));
			}
		}
	}

	private static class TempFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			final String lowerCasePathname = pathname.getPath().toLowerCase(Locale.ROOT);

			return lowerCasePathname.endsWith(StringLiterals.FileType) ||
					lowerCasePathname.endsWith(StringLiterals.JournalSuffix);
		}
	}
}
