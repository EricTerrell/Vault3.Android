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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import android.util.Log;

/**
 * @author Eric Bergman-Terrell
 *
 * Log uncaught exceptions to file on sdcard if enabled.
 */
public class CustomUncaughtExceptionHandler implements UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread thread, Throwable tr) {
		String logMessage = String.format(
				"CustomUncaughtExceptionHandler.uncaughtException: Thread %d Message %s",
				thread.getId(),
				tr.getMessage());

		Log.e(StringLiterals.LogTag, logMessage);

		logMessage = String.format(
				"%s\r\n\r\nThread: %d\r\n\r\nMessage:\r\n\r\n%s\r\n\r\nStack Trace:\r\n\r\n%s",
				new Date(),
				thread.getId(),
				tr.getMessage(),
				Log.getStackTraceString(tr));

		Log.e(StringLiterals.LogTag, logMessage);

		tr.printStackTrace();

		try (PrintWriter printWriter = new PrintWriter(
				new FileWriter(VaultPreferenceActivity.getExceptionLogFilePath(), true))) {

			printWriter.print(logMessage);
			printWriter.print("\n\n---------------------------------------------------------------------------\n\n");
		} catch (Throwable tr2) {
			tr2.printStackTrace();
		}
	}
}
