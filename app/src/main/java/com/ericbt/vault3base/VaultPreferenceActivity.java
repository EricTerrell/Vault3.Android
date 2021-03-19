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

import fonts.AndroidFont;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

public class VaultPreferenceActivity extends Activity {
	private static final String DBPath                     = "DBPath";
	private static final String defaultDBPath              = "";
	private static final String ParentOutlineItemID        = "ParentOutlineItemID";
	private static final String SearchText                 = "SearchText";
	private static final String defaultSearchText          = "";
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
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
		init();
	}

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }
    }
    
	private static String getDefaultRootFolderPath() {
		String override = Globals.getApplication().getApplicationContext().getString(R.string.default_root_folder_path).trim();
		
		return override.length() > 0 ? override : String.format("%s/Vault 3 Documents", Environment.getExternalStorageDirectory().getPath());
	}

	private static String getRootFolderPath(boolean useDefaultIfNecessary) {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString("RootFolderPath", useDefaultIfNecessary ? getDefaultRootFolderPath() : "");
	}
	
	public static String getRootFolderPath() {
		return getRootFolderPath(true);
	}
	
	public static String getExceptionLogFilePath() {
		return String.format("%s/ExceptionLog.txt", getRootFolderPath());
	}
	
	public static boolean getCachePasswords() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean("CachePasswords", true);
	}

	public static boolean getDisplayWrenchIcon() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean("DisplayWrenchIcon", true);
	}

	public static Search.SearchScope getSearchScope() {
		String value = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString(SearchScope, "1");
		
		return value.equals("0") ? Search.SearchScope.SelectedOnly : Search.SearchScope.All;
	}
	
	public static void setSearchScope(Search.SearchScope searchScope) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(SearchScope, searchScope == Search.SearchScope.SelectedOnly ? "0" : "1");
		editor.apply();
	}

	public static Search.SearchFields getSearchFields() {
		String value = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString(SearchFields, "1");
		
		return value.equals("0") ? Search.SearchFields.Titles : Search.SearchFields.TitlesAndText;
	}
	
	public static void setSearchFields(Search.SearchFields searchFields) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(SearchFields, searchFields == Search.SearchFields.Titles ? "0" : "1");
		editor.apply();
	}
	
	public static Search.MustFind getSearchMustFind() {
		String value = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString(MustFind, "0");
		
		return value.equals("0") ? Search.MustFind.AtLeastOneWordOrPhrase : Search.MustFind.AllWordsAndPhrases; 
	}

	public static void setSearchMustFind(Search.MustFind mustFind) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(MustFind, mustFind == Search.MustFind.AtLeastOneWordOrPhrase ? "0" : "1");
		editor.apply();
	}
	
	public static boolean getSearchMatchWholeWords() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean(MatchWholeWords, false);
	}
	
	public static void setSearchMatchWholeWorlds(boolean matchWholeWords) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(MatchWholeWords, matchWholeWords);
		editor.apply();
	}
	
	public static boolean getSearchMatchCase() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean(MatchCase, false);
	}
	
	public static void setSearchMatchCase(boolean matchCase) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(MatchCase, matchCase);
		editor.apply();
	}
	
	public static ApplicationState getApplicationState() {
		String dbPath = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString(DBPath, defaultDBPath);
		int parentOutlineItemId = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getInt(ParentOutlineItemID, defaultParentOutlineItemID);
		
		Log.i(StringLiterals.LogTag, String.format("VaultPreferenceActivity.getApplicationState: %s %d", dbPath, parentOutlineItemId));
		
		return new ApplicationState(dbPath, parentOutlineItemId);
	}
	
	public static void setSearchText(String searchText) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putString(SearchText, searchText);
		editor.apply();
	}

	public static String getSearchText() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString(SearchText, defaultSearchText);
	}
	
	public static int getDefaultParentOutlineItemId() {
		return defaultParentOutlineItemID;
	}
	
	public static void putApplicationState(ApplicationState applicationState) {
		if (applicationState != null) {
			Log.i(StringLiterals.LogTag, String.format("VaultPreferenceActivity.putApplicationState: %s %d", applicationState.getDbPath(), applicationState.getParentOutlineItemId()));
		}
		else {
			Log.i(StringLiterals.LogTag, "VaultPreferenceActivity.putApplicationState: null");
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
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
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean(userAcceptedTermsKey, false);
	}
	
	public static void putUserAcceptedTerms(boolean userAcceptedTerms) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(userAcceptedTermsKey, userAcceptedTerms);
		editor.apply();
	}

	public static boolean getForceUppercasePasswords() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean(forceUppercasePasswordsKey, true);
	}
	
	public static void putUppercasePasswords(boolean forceUppercasePasswords) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(forceUppercasePasswordsKey, forceUppercasePasswords);
		editor.apply();
	}

	public static boolean getShowPasswords() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean(showPasswordsKey, true);
	}

	public static void putShowPasswords(boolean showPasswords) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());

		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(showPasswordsKey, showPasswords);
		editor.apply();
	}

	public static int getMaxSearchHits() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getInt(MaxSearchHits, defaultMaxSearchHits);
	}
	
	public static void setMaxSearchHits(int maxSearchHits) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());
		
		SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putInt(MaxSearchHits, maxSearchHits);
		editor.apply();
	}
	
	public static boolean useDefaultTextFontAndColor() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean("UseDefaultTextFont", false);
	}
	
	private static String getDefaultTextFontTypeface() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString("DefaultTextFontTypeface", FontUtils.getDefaultFont().getName());
	}
	
	private static int getDefaultTextFontSize() {
		String fontSizeString = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString("DefaultTextFontSize", "");

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
		
		String colorName = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString("DefaultTextFontColor", "");
		Integer intColor = FontUtils.getColor(colorName);
		
		if (intColor != null) {
			color = new RGBColor(Color.red(intColor), Color.green(intColor), Color.blue(intColor));
		}
		
		return color;
	}
	
	private static int getDefaultTextFontStyle() {
		int fontStyle = FontUtils.getDefaultFont().getStyle();
		
		String styleString = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString("DefaultTextFontStyle", "");

		if (styleString.length() > 0) {
			fontStyle = FontUtils.getTextStyle(styleString);
		}
		
		return fontStyle;
	}
	
	public static AndroidFont getDefaultTextFont() {
		AndroidFont defaultTextFont = null;
		
		if (useDefaultTextFontAndColor()) {
			defaultTextFont = new AndroidFont(getDefaultTextFontTypeface(), getDefaultTextFontSize(), getDefaultTextFontStyle());
		}
		
		return defaultTextFont;
	}

	public static RGBColor getTextBackgroundColor() {
		RGBColor color = null;
		
		try {
			String colorName = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString("TextBackgroundColor", "");
			Integer intColor = FontUtils.getColor(colorName);
			
			if (intColor != null) {
				color = new RGBColor(Color.red(intColor), Color.green(intColor), Color.blue(intColor));
			}
		}
		catch (Throwable ex) {
			// do nothing
		}
		
		return color;
	}

    public static String getEditMode() {
        return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getString("EditMode", "PAN");
    }

	public static void setSortSearchResults(boolean sortSearchResults) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext());

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		editor.putBoolean(SortSearchResultsKey, sortSearchResults);
		editor.apply();
	}

	public static boolean getSortSearchResults() {
		return PreferenceManager.getDefaultSharedPreferences(Globals.getApplication().getApplicationContext()).getBoolean(SortSearchResultsKey, false);
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
