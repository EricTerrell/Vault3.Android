/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell
  
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

import fonts.AndroidFont;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class VaultPreferenceActivity extends AppCompatActivity {
	private static final String DBPath                     = "DBPath";
	private static final String defaultDBPath              = StringLiterals.EmptyString;
	private static final String ParentOutlineItemID        = "ParentOutlineItemID";
	private static final String SearchText                 = "SearchText";
	private static final String defaultSearchText          = StringLiterals.EmptyString;
	private static final String userAcceptedTermsKey       = "UserAcceptedTerms";
	private static final String forceUppercasePasswordsKey = "ForceUppercasePasswords";
	private static final String showPasswordsKey 		   = "ShowPasswords";
	private static final String SearchScope                = "SearchScope";
	private static final String SearchFields               = "SearchFields";
	private static final String MustFind                   = "MustFind";
	private static final String MatchWholeWords            = "MatchWholeWords";
	private static final String MatchCase                  = "MatchCase";
	private static final String MaxSearchHits              = "MaxSearchHits";
	private static final String SortSearchResultsKey       = "SortSearchResults";
	private static final String FolderUriKey               = "FolderUri";
	private static final String SelectedFileUriKey         = "SelectedFileUriKey";

	private static final int defaultParentOutlineItemID   = -1;
	private static final int defaultMaxSearchHits         = 25;
	
	/**
	 * Generate programmatically generated default values, such as the value for RootFolderPath.
	 */
	public static void init() {
		String rootFolderPath = getRootFolderPath(false);
		
		if (rootFolderPath.trim().length() == 0) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
			
			SharedPreferences.Editor editor = sharedPreferences.edit();

			editor.putString("RootFolderPath", getDefaultRootFolderPath());
			editor.apply();
		}
	}

	// http://stackoverflow.com/questions/6822319/what-to-use-instead-of-addpreferencesfromresource-in-a-preferenceactivity
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment())
				.commit();

		init();
	}

    public static class MyPreferenceFragment extends PreferenceFragmentCompat
    {
		@Override
		public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
			setPreferencesFromResource(R.xml.settings, rootKey);
		}
	}
    
	private static String getDefaultRootFolderPath() {
		final String override = Globals.getApplication().getApplicationContext()
				.getString(R.string.default_root_folder_path).trim();
		
		return override.length() > 0 ? override : String.format("%s/Vault 3 Documents", Environment.getExternalStorageDirectory().getPath());
	}

	private static String getRootFolderPath(boolean useDefaultIfNecessary) {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString("RootFolderPath", useDefaultIfNecessary ? getDefaultRootFolderPath() : StringLiterals.EmptyString);
	}
	
	public static String getRootFolderPath() {
		return getRootFolderPath(true);
	}
	
	public static String getExceptionLogFilePath() {
		return String.format("%s/ExceptionLog.txt", getRootFolderPath());
	}
	
	public static boolean getCachePasswords() {
		return PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext())
				.getBoolean("CachePasswords", true);
	}

	public static boolean getDisplayWrenchIcon() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext())
				.getBoolean("DisplayWrenchIcon", true);
	}

	public static SearchParameters.SearchScope getSearchScope() {
		final String value = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext())
				.getString(SearchScope, "1");
		
		return value.equals("0") ? SearchParameters.SearchScope.SelectedOnly : SearchParameters.SearchScope.All;
	}
	
	public static void setSearchScope(SearchParameters.SearchScope searchScope) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(SearchScope, searchScope == SearchParameters.SearchScope.SelectedOnly ? "0" : "1");
		editor.apply();
	}

	public static SearchParameters.SearchFields getSearchFields() {
		final String value = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext())
				.getString(SearchFields, "1");
		
		return value.equals("0") ? SearchParameters.SearchFields.Titles : SearchParameters.SearchFields.TitlesAndText;
	}
	
	public static void setSearchFields(SearchParameters.SearchFields searchFields) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(SearchFields, searchFields == SearchParameters.SearchFields.Titles ? "0" : "1");
		editor.apply();
	}
	
	public static SearchParameters.MustFind getSearchMustFind() {
		final String value = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext()).getString(MustFind, "0");
		
		return value.equals("0") ?
				SearchParameters.MustFind.AtLeastOneWordOrPhrase : SearchParameters.MustFind.AllWordsAndPhrases;
	}

	public static void setSearchMustFind(SearchParameters.MustFind mustFind) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(MustFind, mustFind == SearchParameters.MustFind.AtLeastOneWordOrPhrase ? "0" : "1");
		editor.apply();
	}
	
	public static boolean getSearchMatchWholeWords() {
		return PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext())
				.getBoolean(MatchWholeWords, false);
	}
	
	public static void setSearchMatchWholeWorlds(boolean matchWholeWords) {
		final SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(MatchWholeWords, matchWholeWords);
		editor.apply();
	}
	
	public static boolean getSearchMatchCase() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getBoolean(MatchCase, false);
	}
	
	public static void setSearchMatchCase(boolean matchCase) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(MatchCase, matchCase);
		editor.apply();
	}
	
	public static ApplicationState getApplicationState() {
		final String dbPath = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext()).getString(DBPath, defaultDBPath);
		final int parentOutlineItemId = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext())
				.getInt(ParentOutlineItemID, defaultParentOutlineItemID);
		
		Log.i(StringLiterals.LogTag, String.format(
				"VaultPreferenceActivity.getApplicationState: %s %d", dbPath, parentOutlineItemId));
		
		return new ApplicationState(dbPath, parentOutlineItemId);
	}
	
	public static void setSearchText(String searchText) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(SearchText, searchText);
		editor.apply();
	}

	public static String getSearchText() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getString(SearchText, defaultSearchText);
	}
	
	public static void putApplicationState(ApplicationState applicationState) {
		if (applicationState != null) {
			Log.i(StringLiterals.LogTag, String.format(
					"VaultPreferenceActivity.putApplicationState: %s %d",
					applicationState.getDbPath(), applicationState.getParentOutlineItemId()));
		}
		else {
			Log.i(StringLiterals.LogTag, "VaultPreferenceActivity.putApplicationState: null");
		}

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		
		if (applicationState != null) {
			editor.putString(DBPath, applicationState.getDbPath());
			editor.putInt(ParentOutlineItemID, applicationState.getParentOutlineItemId());
		}
		else {
			editor.putString(DBPath, defaultDBPath);
			editor.putInt(ParentOutlineItemID, defaultParentOutlineItemID);
		}
		
		editor.apply();
	}

	public static boolean getUserAcceptedTerms() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getBoolean(userAcceptedTermsKey, false);
	}
	
	public static void putUserAcceptedTerms(boolean userAcceptedTerms) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(userAcceptedTermsKey, userAcceptedTerms);
		editor.apply();
	}

	public static boolean getForceUppercasePasswords() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getBoolean(forceUppercasePasswordsKey, true);
	}
	
	public static void putUppercasePasswords(boolean forceUppercasePasswords) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(forceUppercasePasswordsKey, forceUppercasePasswords);
		editor.apply();
	}

	public static boolean getShowPasswords() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().
				getApplicationContext()).getBoolean(showPasswordsKey, true);
	}

	public static void putShowPasswords(boolean showPasswords) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(showPasswordsKey, showPasswords);
		editor.apply();
	}

	public static int getMaxSearchHits() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getInt(MaxSearchHits, defaultMaxSearchHits);
	}
	
	public static void setMaxSearchHits(int maxSearchHits) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());
		
		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt(MaxSearchHits, maxSearchHits);
		editor.apply();
	}
	
	public static boolean useDefaultTextFontAndColor() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getBoolean("UseDefaultTextFont", false);
	}
	
	private static String getDefaultTextFontTypeface() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getString("DefaultTextFontTypeface",
				FontUtils.getDefaultFont().getName());
	}
	
	private static int getDefaultTextFontSize() {
		final String fontSizeString = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext()).getString(
						"DefaultTextFontSize", StringLiterals.EmptyString);

		int fontSize = (int) FontUtils.getDefaultFont().getSizeInPoints();
	
		try {
			fontSize = Integer.valueOf(fontSizeString.split(" ")[0]);
		}
		catch (Throwable ex) {
			// do nothing.
		}
		
		return fontSize;
	}
	
	public static RGBColor getDefaultTextFontColor() {
		RGBColor color = null;
		
		final String colorName = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext()).getString(
						"DefaultTextFontColor", StringLiterals.EmptyString);
		final Integer intColor = FontUtils.getColor(colorName);
		
		if (intColor != null) {
			color = new RGBColor(Color.red(intColor), Color.green(intColor), Color.blue(intColor));
		}
		
		return color;
	}
	
	private static int getDefaultTextFontStyle() {
		int fontStyle = FontUtils.getDefaultFont().getStyle();
		
		final String styleString = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext()).getString(
						"DefaultTextFontStyle", StringLiterals.EmptyString);

		if (styleString.length() > 0) {
			fontStyle = FontUtils.getTextStyle(styleString);
		}
		
		return fontStyle;
	}
	
	public static AndroidFont getDefaultTextFont() {
		AndroidFont defaultTextFont = null;
		
		if (useDefaultTextFontAndColor()) {
			defaultTextFont = new AndroidFont(
					getDefaultTextFontTypeface(),
					getDefaultTextFontSize(), getDefaultTextFontStyle());
		}
		
		return defaultTextFont;
	}

	public static RGBColor getTextBackgroundColor() {
		RGBColor color = null;
		
		try {
			final String colorName = PreferenceManager.getDefaultSharedPreferences(
					Globals.getApplication().getApplicationContext()).getString(
							"TextBackgroundColor", StringLiterals.EmptyString);
			final Integer intColor = FontUtils.getColor(colorName);
			
			if (intColor != null) {
				color = new RGBColor(
						Color.red(intColor), Color.green(intColor), Color.blue(intColor));
			}
		}
		catch (Throwable ex) {
			// do nothing
		}
		
		return color;
	}

    public static String getEditMode() {
        return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getString("EditMode", "PAN");
    }

	public static void setSortSearchResults(boolean sortSearchResults) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(SortSearchResultsKey, sortSearchResults);
		editor.apply();
	}

	public static void setFolderUri(Uri folderUri) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(FolderUriKey, folderUri.toString());
		editor.apply();
	}

	public static Uri getSelectedFileUri() {
		final String uriString = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext())
				.getString(SelectedFileUriKey, null);

		return Uri.parse(uriString);
	}

	public static void setSelectedFileUri(Uri selectedFileUri) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext());

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(SelectedFileUriKey, selectedFileUri.toString());
		editor.apply();
	}

	public static Uri getFolderUri() {
		final String uriString = PreferenceManager.getDefaultSharedPreferences(
				Globals.getApplication().getApplicationContext())
				.getString(FolderUriKey, null);

		try {
			return Uri.parse(uriString);
		} catch (Throwable ex) {
			return null;
		}
	}

	public static boolean getSortSearchResults() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication()
				.getApplicationContext()).getBoolean(SortSearchResultsKey, false);
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
