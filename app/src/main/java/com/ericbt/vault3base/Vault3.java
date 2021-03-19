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

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import commonCode.VaultDocumentVersion;
import commonCode.VaultException;
import fonts.FontList;

public class Vault3 extends AsyncTaskActivity {
	private Menu optionsMenu;
	private Button addItem;
	private ListView navigateListView;
	private NavigateArrayAdapter navigateArrayAdapter;
	private TextView parentTextView;
	private OutlineItem childOutlineItem, movingOutlineItem; 
	private ImageView goUpImage;
	private View parentLayout;
	private final SparseIntArray scrollPositions = new SparseIntArray();
	private TextFragment textFragment;
	private CustomBroadcastReceiver customBroadcastReceiver;

	private static final int FILE_ACTIVITY_RESULT = 1;
	private static final int ADD_ITEM_RESULT = 2;
	private static final int PASSWORD_PROMPT = 3;
	private static final int MOVE_ITEM_HERE = 4;
	private static final int REMOVE_OUTLINE_ITEM = 5;
	private static final int UPGRADE_VAULT_DOCUMENT = 6;
	private static final int SEARCH = 7;
	public static final int TEXT = 8;
	public static final int SETTINGS = 9;

	private DocumentAction documentAction;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(StringLiterals.LogTag, getPackageName());

		if (getIntent().getBooleanExtra("EXIT", false)) {
	         finish();
	         
	         ExitApplication.exit();
	    }
		else {
			Log.i(StringLiterals.LogTag, "Vault3.onCreate begin");

            PreferenceManager.setDefaultValues(this, R.xml.settings, false);
			
			super.onCreate(savedInstanceState);

			movingOutlineItem = new OutlineItem();
			
			ApplicationState applicationState = VaultPreferenceActivity.getApplicationState();
			
			// If we know which document to load, prepare to do it in onResume.
			if (applicationState.getDbPath() != null && applicationState.getDbPath().length() > 0) {
				int parentOutlineItemId = applicationState.getParentOutlineItemId() != 0 ? applicationState.getParentOutlineItemId() : 1;

				documentAction = new DocumentAction(applicationState.getDbPath(), DocumentAction.Action.Load, parentOutlineItemId, null);
			}
			
			VaultPreferenceActivity.init();

	        setContentView(R.layout.main);

			initActionBar();

			parentTextView = (TextView) findViewById(R.id.tvParent);
			
			navigateListView = (ListView) findViewById(R.id.list_view);
			navigateListView.setEmptyView(findViewById(R.id.EmptyNavigateListView));
			registerForContextMenu(navigateListView);

			goUpImage = (ImageView) findViewById(R.id.GoUpImage);
			
			parentLayout = findViewById(R.id.ParentLayout);
			
			goUpImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveScrollPosition();

					goToParent();
				}
			});
			
			goUpImage.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					saveScrollPosition();
					goToRoot();
					
					return false;
				}
			});

			addItem = (Button) findViewById(R.id.AddItem);
			
			addItem.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Vault3.this, AddItemActivity.class);
					intent.putExtra(StringLiterals.DisplayHint, true); // tell the user how to add subsequent items.
					intent.putExtra(StringLiterals.AboveOrBelowPrompt, false);
	                startActivityForResult(intent, ADD_ITEM_RESULT);     
				}
			});
			
			navigateArrayAdapter = new NavigateArrayAdapter(this, 0, 0, new OutlineItem());
			navigateListView.setAdapter(navigateArrayAdapter);
			
			enableDisableParentLayout(false);
		    
		    addAds();
		}

		customBroadcastReceiver = createCustomBroadcastReceiver();

        Log.i(StringLiterals.LogTag, "Vault3.onCreate end");
	}

	private CustomBroadcastReceiver createCustomBroadcastReceiver() {
		Log.i(StringLiterals.LogTag, "Vault3.createCustomBroadcastReceiver");

		final CustomBroadcastReceiver customBroadcastReceiver = new CustomBroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(StringLiterals.LogTag, String.format("onReceive action: %s", intent.getAction()));

				switch(intent.getAction()) {
					case CustomBroadcastReceiver.UPDATE_TEXT: {
						final int id = intent.getIntExtra(CustomBroadcastReceiver.ID, -1);

						if (id >= 0) {
							final OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem(id);

							if (outlineItem != null) {
								outlineItem.setTitle(intent.getStringExtra(CustomBroadcastReceiver.NEW_TITLE));
								outlineItem.setText(intent.getStringExtra(CustomBroadcastReceiver.NEW_TEXT));

								navigateArrayAdapter.notifyDataSetChanged();
							}
						}
					}
					break;

					case CustomBroadcastReceiver.UPDATE_FONT: {
						final int id = intent.getIntExtra(CustomBroadcastReceiver.ID, -1);

						if (id >= 0) {
							final OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem(id);

							if (outlineItem != null) {
								final String serializedFontList = intent.getStringExtra(CustomBroadcastReceiver.FONT_LIST);

								final RGBColor rgbColor = new RGBColor(
										intent.getIntExtra(CustomBroadcastReceiver.RED, 0),
										intent.getIntExtra(CustomBroadcastReceiver.GREEN, 0),
										intent.getIntExtra(CustomBroadcastReceiver.BLUE, 0)
								);

								final FontList fontList = FontList.deserialize(serializedFontList);

								outlineItem.setFontList(fontList);
								outlineItem.setColor(rgbColor);

								// No need to call notifyDataSetChanged as the UI will not change
							}
						}
					}
					break;
				}
			}
		};

		final IntentFilter intentFilter = new IntentFilter(CustomBroadcastReceiver.UPDATE_TEXT);
		intentFilter.addAction(CustomBroadcastReceiver.UPDATE_FONT);

		registerReceiver(customBroadcastReceiver, intentFilter);

		return customBroadcastReceiver;
	}

	public TextFragment getTextFragment() {
		return textFragment;
	}

	private void addAds() {
		if (Globals.isFreeVersion()) {
			View adView = findViewById(R.id.adLayout);
			adView.setVisibility(View.VISIBLE);

			Button upgradeButton = (Button) findViewById(R.id.UpgradeButton);

			upgradeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Vault3.this, UpgradeActivity.class);
					startActivity(intent);
				}
			});
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		ApplicationState applicationState = null;
		
		VaultDocument vaultDocument = Globals.getApplication().getVaultDocument();
		
		if (vaultDocument != null) {
			applicationState = new ApplicationState(vaultDocument.getDatabase().getPath(), navigateArrayAdapter.getOutlineItem().getId());
		}
		
		VaultPreferenceActivity.putApplicationState(applicationState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(customBroadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.i(StringLiterals.LogTag, "Vault3.onResume begin");

        // Prompt user to accept license terms if they have not been previously accepted.
        if (!VaultPreferenceActivity.getUserAcceptedTerms()) {
			Intent licenseTermsIntent = new Intent(Vault3.this, LicenseTermsActivity.class);
			licenseTermsIntent.putExtra(StringLiterals.AllowCancel, false);
            startActivity(licenseTermsIntent);     
        }

		Permissions.requestReadWriteExternalStoragePermission(this);

        boolean loadedFromFileManager = false;

        if (getIntent() != null && getIntent().getData() != null) {
            String fileManagerPath = getIntent().getData().getPath();

            getIntent().setData(null); // we don't want to do this a second time after the password UI is mismissed.
            documentAction = null;

            if (new File(fileManagerPath).exists()) {
                loadVaultFile(fileManagerPath, 1, null);
                loadedFromFileManager = true;
            }
        }

        if (!loadedFromFileManager && documentAction != null) {
			if (documentAction.getAction() == DocumentAction.Action.Load) {
				loadVaultFile(documentAction.getDbFilePath(), documentAction.getOutlineId(), documentAction.getPassword());
			}
			else if (documentAction.getAction() == DocumentAction.Action.Close) {
				Log.i(StringLiterals.LogTag, "Vault3.onResume closing document");
				
				updateGUIWhenCurrentDocumentOpenedOrClosed(false);
				Globals.getApplication().setVaultDocument(null);
			}
			
			documentAction = null;
		}

		Log.i(StringLiterals.LogTag, "Vault3.onResume end");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.options_menu, menu);
		
		optionsMenu = menu;
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = false;
		
		if (enabled) {
			MenuItem sortMenuItem = menu.findItem(R.id.SortMenuItem);
			sortMenuItem.setEnabled(Globals.getApplication().getVaultDocument() != null && Globals.getApplication().getVaultDocument().canSort(getParentOutlineItem()));
			
			result = true;
		}

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == R.id.FileMenuItem) {
			Intent intent = new Intent(Vault3.this, FileActivity.class);
			startActivityForResult(intent, FILE_ACTIVITY_RESULT);
			result = true;
		}
		else if (item.getItemId() == R.id.SearchMenuItem) {
			Intent intent = new Intent(Vault3.this, SearchActivity.class);
			intent.putExtra(StringLiterals.SelectedOutlineItemId, getParentOutlineItem().getId());
			startActivityForResult(intent, SEARCH);
			result = true;
		}
		else if (item.getItemId() == R.id.settings_menu_item) {
			startActivityForResult(new Intent(this, VaultPreferenceActivity.class), SETTINGS);
			result = true;
		} else if (item.getItemId() == R.id.about_menu_item) {
			Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);     
			result = true;
		} else if (item.getItemId() == R.id.SortMenuItem) {
			setEnabled(false);
			new SortTask().execute(new SortTaskParameters(getParentOutlineItem(), this));
        } else if (item.getItemId() == R.id.HelpMenuItem) {
            String url = getString(R.string.online_help_url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else if (item.getItemId() == R.id.CloudSyncMenuItem) {
            String url = getString(R.string.cloud_sync_url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }

		return result;
	}

	public void updateUIs(OutlineItem outlineItem) {
		NavigateArrayAdapter navigateArrayAdapter = (NavigateArrayAdapter) getNavigateListView().getAdapter();
		navigateArrayAdapter.setEnabled(true);

		if (outlineItem != null) {
			navigateArrayAdapter.setOutlineItem(outlineItem);
			
			setCurrentOutlineItem(outlineItem);
			enableDisableParentLayout(true);
		}
		
		enableDisableButtons();
		
		enable(true);
		
		restoreScrollPosition();
	}
	
	private void loadVaultFile(String dbFilePath, int selectedOutlineItemID, String password) {
		Log.i(StringLiterals.LogTag, String.format("Vault3.loadVaultFile begin %s %d", dbFilePath, selectedOutlineItemID));
		
		try {
			// If we have a document open, and it's the right one, use it.
			if (Globals.getApplication().getVaultDocument() != null && Globals.getApplication().getVaultDocument().getDatabase().getPath().equals(dbFilePath)) {
				displayVaultDocument(selectedOutlineItemID);
			}
			else {
				// Otherwise, create a new one.
				SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(dbFilePath, null);
				final VaultDocument vaultDocument = new VaultDocument(database);
					
				VaultDocument.verifyVaultDocumentVersion(vaultDocument.getDocumentVersion());
					
				if (vaultDocument.getDocumentVersion().compareTo(VaultDocumentVersion.getLatestVaultDocumentVersion()) < 0) {
					vaultDocument.close();
					
					upgradeVaultDocument(dbFilePath);
					return;
				}
					
				if (vaultDocument.isEncrypted) {
					password = password != null ? password : Globals.getApplication().getPasswordCache().get(dbFilePath);
					
					if (vaultDocument.setPassword(password)) {
						Globals.getApplication().setVaultDocument(vaultDocument, this);
						
						Globals.getApplication().getPasswordCache().put(dbFilePath, password);

						displayVaultDocument(selectedOutlineItemID);
					}
					else {
						Intent intent = new Intent(Vault3.this, PasswordPromptActivity.class);
						intent.putExtra(StringLiterals.DBPath, vaultDocument.getDatabase().getPath());
			            startActivityForResult(intent, PASSWORD_PROMPT);     
					}
				}
				else {
					Globals.getApplication().setVaultDocument(vaultDocument, this);
					
					displayVaultDocument(selectedOutlineItemID);
				}
			}
		} catch (Throwable ex) {
			processException(ex, dbFilePath);
		}

		Log.i(StringLiterals.LogTag, "Vault3.loadVaultFile end");
	}
	
	private void displayVaultDocument(int selectedOutlineItemID) {
		Log.i(StringLiterals.LogTag, String.format("Vault3.displayVaultDocument %d", selectedOutlineItemID));
		
		clearScrollPositions();
		
		try {
			update(selectedOutlineItemID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void displayVaultDocument() {
		displayVaultDocument(1);
	}
	
	private void processException(Throwable ex, String dbFilePath) {
		Log.e(StringLiterals.LogTag, String.format("Vault3.loadVaultFile exception %s", ex.getMessage()));
		
		ex.printStackTrace();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(String.format("Cannot Open %s Document", StringLiterals.ProgramName));
        alertDialogBuilder.setPositiveButton("OK", null);

        boolean setMessage = false;
        
		if (ex instanceof VaultException) {
			VaultException vaultException = (VaultException) ex;
			
			if (vaultException.getExceptionCode() == VaultException.ExceptionCode.DatabaseVersionTooHigh) {
		        alertDialogBuilder.setMessage(String.format("You must upgrade to the latest version of Vault 3 in order to open %s.", dbFilePath));
				
				setMessage = true;
			}
		}
		
		if (!setMessage) {
	        alertDialogBuilder.setMessage(String.format("Cannot open %s.\r\n\r\nTurn off USB storage if it is on.\r\n\r\n%s", dbFilePath, ex.getMessage()));
		}
                
		alertDialogBuilder.create().show();
	}

	private void upgradeVaultDocument(String dbFilePath) {
		Intent intent = new Intent(this, UpgradeVaultDocumentActivity.class);
		intent.putExtra(StringLiterals.DBPath, dbFilePath);
		startActivityForResult(intent, UPGRADE_VAULT_DOCUMENT);
	}

	private void goToParent() {
		if (Globals.getApplication().getVaultDocument() != null && Globals.getApplication().getVaultDocument().getDatabase() != null && navigateArrayAdapter.getOutlineItem() != null) {
			setEnabled(false);
			parentLayout.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.list_selector_background));
			
			OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem();
			
			UpdateNavigateListItemTaskParameters updateNavigateListItemTaskParameters = new UpdateNavigateListItemTaskParameters(outlineItem.getParentId(), this);
			new UpdateNavigateListItemTask().execute(updateNavigateListItemTaskParameters);
		}
	}
	
	private void goToRoot() {
		if (Globals.getApplication().getVaultDocument() != null && Globals.getApplication().getVaultDocument().getDatabase() != null) {
			setEnabled(false);
			parentLayout.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.list_selector_background));
			
			UpdateNavigateListItemTaskParameters updateNavigateListItemTaskParameters = new UpdateNavigateListItemTaskParameters(1, this);
			new UpdateNavigateListItemTask().execute(updateNavigateListItemTaskParameters);
		}
	}
	
	private void enableDisableParentLayout(boolean enabled) {
		parentLayout.setEnabled(enabled);
		parentLayout.setClickable(enabled);
		parentLayout.setLongClickable(enabled);

		goUpImage.setEnabled(enabled);
	}
	
	public void enableDisableButtons() {
		OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem();
		boolean enabled = outlineItem != null && !outlineItem.isRoot();

		enableDisableParentLayout(enabled);
	}
	
	public ListView getNavigateListView() {
		return navigateListView;
	}
	
	public void setParentTitle(String title) {
		parentTextView.setText(title);
	}

	public View getParentLayout() {
		return parentLayout;
	}

	public TextView getParentTextView() {
		return parentTextView;
	}

	private void setCurrentOutlineItem(final OutlineItem currentOutlineItem) {
		parentTextView.setText(currentOutlineItem.getTitle());

		if (textFragment != null) {
			parentLayout.setBackgroundColor(!currentOutlineItem.isRoot() ? Color.BLUE : Color.BLACK);
			TextActivity.addTextData(currentOutlineItem, textFragment.getActivity().getIntent(), true);
			textFragment.update(true, currentOutlineItem);
		}

		parentTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (textFragment != null) {
					parentLayout.setBackgroundColor(Color.BLUE);

					TextActivity.addTextData(currentOutlineItem, textFragment.getActivity().getIntent(), false);
					textFragment.update(true, currentOutlineItem);

					((NavigateArrayAdapter) getNavigateListView().getAdapter()).selectOutlineItem(currentOutlineItem);
				}
				else {
					Intent intent = new Intent(Vault3.this, TextActivity.class);
					TextActivity.addTextData(Vault3.this.navigateArrayAdapter.getOutlineItem(), intent, true);
					startActivityForResult(intent, TEXT);
				}
			}
		});
	}
	
	public void update(int outlineItemID) {
		enable(false);
		
		UpdateNavigateListItemTaskParameters updateNavigateListItemTaskParameters = new UpdateNavigateListItemTaskParameters(outlineItemID, this);
		new UpdateNavigateListItemTask().execute(updateNavigateListItemTaskParameters);
	}

	public void update() {
		OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem();

		update(outlineItem.getId());
	}

	private void saveScrollPosition() {
		OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem();
		saveScrollPosition(outlineItem.getId(), navigateListView.getFirstVisiblePosition());
	}
	
	public void saveScrollPosition(int outlineItemID, int firstVisibleItemPosition) {
		scrollPositions.put(outlineItemID, firstVisibleItemPosition);
	}
	
	public Integer getScrollPosition(int outlineItemID) {
		return scrollPositions.get(outlineItemID);
	}
	
	private void restoreScrollPosition() {
		OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem();

		if (outlineItem != null) {
			Integer firstVisibleItemPosition = scrollPositions.get(outlineItem.getId());
			
			if (firstVisibleItemPosition != null) {
				navigateListView.setSelection(firstVisibleItemPosition);
			}
		}
	}

	private void clearScrollPositions() {
		scrollPositions.clear();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem();

		if (view.getId() == R.id.list_view) {
			AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
			
			childOutlineItem = null;

			if (outlineItem != null && outlineItem.getChildren().size() > adapterContextMenuInfo.position) {
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.navigate_context_menu, menu);
				
				childOutlineItem = outlineItem.getChildren().get(adapterContextMenuInfo.position);
				
				menu.setHeaderTitle(childOutlineItem.getTitle());
				
				MenuItem addMenuItem = menu.findItem(R.id.AddMenuItem);
				addMenuItem.setEnabled(Globals.getApplication().getVaultDocument().canAdd());
				
				MenuItem indentMenuItem = menu.findItem(R.id.IndentMenuItem);
				indentMenuItem.setEnabled(Globals.getApplication().getVaultDocument().canIndent(childOutlineItem));
				
				MenuItem unIndentMenuItem = menu.findItem(R.id.UnIndentMenuItem);
				unIndentMenuItem.setEnabled(Globals.getApplication().getVaultDocument().canUnIndent(childOutlineItem));
				
				MenuItem moveUpMenuItem = menu.findItem(R.id.MoveUpMenuItem);
				moveUpMenuItem.setEnabled(Globals.getApplication().getVaultDocument().canMoveUp(childOutlineItem));
				
				MenuItem moveDownMenuItem = menu.findItem(R.id.MoveDownMenuItem);
				moveDownMenuItem.setEnabled(Globals.getApplication().getVaultDocument().canMoveDown(childOutlineItem));
				
				MenuItem moveMenuItem = menu.findItem(R.id.MoveItemMenuItem);

				try {
					moveMenuItem.setEnabled(Globals.getApplication().getVaultDocument().canMove());
				} catch (VaultException e) {
					e.printStackTrace();
				}
				
				MenuItem moveItemHereMenuItem = menu.findItem(R.id.MoveItemHereMenuItem);
				
				boolean enabled = Globals.getApplication().getVaultDocument().canMoveTo(movingOutlineItem, childOutlineItem);
				
				moveItemHereMenuItem.setEnabled(enabled);
				moveItemHereMenuItem.setVisible(enabled);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = true;

		if (item.getItemId() == R.id.AddMenuItem) {
			Intent intent = new Intent(Vault3.this, AddItemActivity.class);
			intent.putExtra(StringLiterals.DisplayHint, false);
			intent.putExtra(StringLiterals.AboveOrBelowPrompt, true);
			intent.putExtra(StringLiterals.SelectedOutlineItemId, childOutlineItem.getId());
			intent.putExtra(StringLiterals.SelectedOutlineItemSortOrder, childOutlineItem.getSortOrder());
			intent.putExtra(StringLiterals.SelectedOutlineItemParentId, childOutlineItem.getParentId());
            startActivityForResult(intent, ADD_ITEM_RESULT);     
		} else if (item.getItemId() == R.id.RemoveMenuItem) {
			Intent intent = new Intent(this, RemoveOutlineItemActivity.class);
			intent.putExtra(StringLiterals.SelectedOutlineItemId, childOutlineItem.getId());
			intent.putExtra(StringLiterals.Title, childOutlineItem.getTitle());
			intent.putExtra(StringLiterals.SelectedOutlineItemParentId, childOutlineItem.getParentId());
			startActivityForResult(intent, REMOVE_OUTLINE_ITEM);
		} else if (item.getItemId() == R.id.IndentMenuItem) {
			setEnabled(false);
			
			new IndentItemTask().execute(new IndentItemTaskParameters(childOutlineItem, this));
		} else if (item.getItemId() == R.id.UnIndentMenuItem) {
			setEnabled(false);
			
			new UnindentItemTask().execute(new UnindentItemTaskParameters(childOutlineItem, this));
		} else if (item.getItemId() == R.id.MoveItemMenuItem) {
			movingOutlineItem = childOutlineItem;
		} else if (item.getItemId() == R.id.MoveItemHereMenuItem) {
			Intent intent = new Intent(this, MoveItemHereActivity.class);
			intent.putExtra(StringLiterals.SelectedOutlineItemId, childOutlineItem.getId());
			intent.putExtra(StringLiterals.SelectedOutlineItemSortOrder, childOutlineItem.getSortOrder());
			intent.putExtra(StringLiterals.SelectedOutlineItemParentId, childOutlineItem.getParentId());
			startActivityForResult(intent, MOVE_ITEM_HERE);
		} else if (item.getItemId() == R.id.MoveUpMenuItem) {
			setEnabled(false);
			
			new MoveItemUpTask().execute(new MoveItemUpTaskParameters(childOutlineItem, this));
		} else if (item.getItemId() == R.id.MoveDownMenuItem) {
			setEnabled(false);
			
			new MoveItemDownTask().execute(new MoveItemDownTaskParameters(childOutlineItem, this));
		} else {
			result = super.onContextItemSelected(item);
		}

		return result;
	}

	private void enableOptionsMenuItems(boolean enabled) {
		if (optionsMenu != null) {
			for (int i = 0; i < optionsMenu.size(); i++) {
				optionsMenu.getItem(i).setEnabled(enabled);
			}
		}
	}
	
	public void enable(boolean enabled) {
		if (enabled) {
			enableDisableButtons();
		}
		else {
			enableDisableParentLayout(false);
		}
		
		enableOptionsMenuItems(enabled);

		navigateArrayAdapter.setEnabled(enabled);
		navigateListView.setEnabled(enabled);
		navigateListView.setFocusable(enabled);
		navigateListView.setClickable(enabled);
		navigateListView.setItemsCanFocus(enabled);

		OutlineItem outlineItem = navigateArrayAdapter.getOutlineItem();

		final boolean enableRoot = enabled && !outlineItem.isRoot();

		int color = enableRoot ? Color.WHITE : Color.DKGRAY;
		parentTextView.setTextColor(color);

		goUpImage.setEnabled(enableRoot);
		parentTextView.setEnabled(enableRoot);

		if (textFragment != null) {
			textFragment.enable(enabled);
		}
	}
	
	public void enableAddItem(boolean enabled) {
		addItem.setVisibility(enabled ? View.VISIBLE : View.GONE);
		addItem.setEnabled(enabled);
	}
	
	public void updateGUIWhenCurrentDocumentOpenedOrClosed(boolean opened) {
		enable(opened);
		
		if (!opened) {
			parentTextView.setText("");
			enableAddItem(false);
			navigateArrayAdapter.clear();
		}
		
		enableDisableParentLayout(false);

		enableOptionsMenuItems(true);
	}
	
	private OutlineItem getParentOutlineItem() {
		return navigateArrayAdapter.getOutlineItem();
	}
	
	public OutlineItem getMovingOutlineItem() {
		return movingOutlineItem;
	}

	public void setMovingOutlineItem(OutlineItem movingOutlineItem) {
		this.movingOutlineItem = movingOutlineItem;
	}
	
	public void updateStateWhenDocumentChanged() {
		setMovingOutlineItem(null);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);

		if (fragment instanceof TextFragment) {
			textFragment = (TextFragment) fragment;
		}
	}

	@Override
	public void onBackPressed() {
		new ExitPrompt(this).onBackPressed();
	}
	
	public void exit() {
		super.onBackPressed();
	}

	public void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.i(StringLiterals.LogTag, String.format("Vault3.onActivityResult begin %d %d", requestCode, resultCode));

		switch (requestCode) {
			case FILE_ACTIVITY_RESULT: {
				if (resultCode == RESULT_OK) {
					String action = data.getExtras().getString(StringLiterals.Action);

					Log.i(StringLiterals.LogTag, String.format("Vault3.onActivityResult: FILE_ACTIVITY_RESULT %s", action));

					if (StringLiterals.Load.equals(action)) {
						documentAction = new DocumentAction(data.getExtras().getString(StringLiterals.DBPath), DocumentAction.Action.Load, 1, null);
					} else if (StringLiterals.Close.equals(action) || StringLiterals.RemoveCurrentDocument.equals(action)) {
						documentAction = new DocumentAction(null, DocumentAction.Action.Close, 0, null);
					}
				}
			}
			break;

			case ADD_ITEM_RESULT: {
				if (resultCode == RESULT_OK) {
					OutlineItem newOutlineItem = new OutlineItem();
					newOutlineItem.setTitle(data.getExtras().getString(StringLiterals.Title));
					newOutlineItem.setText(data.getExtras().getString(StringLiterals.Text));

					setEnabled(false);

					OutlineItem selectedOutlineItem = new OutlineItem();
					selectedOutlineItem.setId(data.getExtras().getInt(StringLiterals.SelectedOutlineItemId, 0));
					selectedOutlineItem.setSortOrder(data.getExtras().getInt(StringLiterals.SelectedOutlineItemSortOrder, 0));
					selectedOutlineItem.setParentId(data.getExtras().getInt(StringLiterals.SelectedOutlineItemParentId, 1));

					AddItemTaskParameters addItemTaskParameters = new AddItemTaskParameters(newOutlineItem,
							selectedOutlineItem,
							data.getExtras().getBoolean(StringLiterals.AddAbove),
							data.getExtras().getBoolean(StringLiterals.DisplayHint),
							this);
					new AddItemTask().execute(addItemTaskParameters);
				}
			}
			break;

			case PASSWORD_PROMPT: {
				if (resultCode == RESULT_OK) {
					final String dbPath = data.getExtras().getString(StringLiterals.DBPath);
					final String password = data.getExtras().getString(StringLiterals.Password);

					documentAction = new DocumentAction(dbPath, DocumentAction.Action.Load, 1, password);
				}
			}
			break;

			case MOVE_ITEM_HERE: {
				if (resultCode == RESULT_OK) {
					int id = data.getExtras().getInt(StringLiterals.SelectedOutlineItemId);
					int sortOrder = data.getExtras().getInt(StringLiterals.SelectedOutlineItemSortOrder);
					int parentId = data.getExtras().getInt(StringLiterals.SelectedOutlineItemParentId);

					OutlineItem selectedOutlineItem = new OutlineItem();
					selectedOutlineItem.setId(id);
					selectedOutlineItem.setSortOrder(sortOrder);
					selectedOutlineItem.setParentId(parentId);

					setEnabled(false);

					MoveItemTaskParameters moveItemTaskParameters = new MoveItemTaskParameters(movingOutlineItem, selectedOutlineItem, data.getExtras().getBoolean(StringLiterals.Above), this);
					new MoveItemTask().execute(moveItemTaskParameters);
				}
			}
			break;

			case REMOVE_OUTLINE_ITEM: {
				if (resultCode == RESULT_OK) {
					setEnabled(false);

					OutlineItem outlineItem = new OutlineItem();
					outlineItem.setId(data.getExtras().getInt(StringLiterals.SelectedOutlineItemId));
					outlineItem.setParentId(data.getExtras().getInt(StringLiterals.SelectedOutlineItemParentId));

					new RemoveItemAndChildrenTask().execute(new RemoveItemAndChildrenTaskParameters(outlineItem, this));
				}
			}
			break;

			case UPGRADE_VAULT_DOCUMENT: {
				if (resultCode == RESULT_OK) {
					documentAction = new DocumentAction(data.getExtras().getString(StringLiterals.DBPath), DocumentAction.Action.Load, 1, null);
				} else if (resultCode == UpgradeVaultDocumentActivity.RESULT_EXCEPTION) {
					AlertDialog.Builder alertDialogBuilder = new Builder(this);
					alertDialogBuilder.setTitle("Cannot Upgrade");

					String message = String.format("Cannot upgrade %s: %s",
							data.getExtras().getString(StringLiterals.DBPath),
							data.getExtras().getString(StringLiterals.ExceptionMessage));

					alertDialogBuilder.setMessage(message);
					alertDialogBuilder.setPositiveButton("OK", null);

					alertDialogBuilder.create().show();
				}
			}
			break;

			case SEARCH: {
				if (resultCode == RESULT_OK) {
					int searchHitParentOutlineId = data.getExtras().getInt(StringLiterals.SelectedOutlineItemParentId);

					if (Globals.getApplication().getVaultDocument() != null && Globals.getApplication().getVaultDocument().getDatabase() != null) {
						documentAction = new DocumentAction(Globals.getApplication().getVaultDocument().getDatabase().getPath(), DocumentAction.Action.Load, searchHitParentOutlineId, null);
					}

				}
			}
			break;

			case SETTINGS: {
				((BaseAdapter) navigateListView.getAdapter()).notifyDataSetChanged();
			}
			break;
		}

		if (documentAction == null) {
			Log.i(StringLiterals.LogTag, "Vault3.onActivityResult: documentAction is null");
		} else {
			Log.i(StringLiterals.LogTag, String.format("Vault3.onActivityResult: documentAction path: %s action: %s id: %d",
					documentAction.getDbFilePath(), documentAction.getAction(), documentAction.getOutlineId()));
		}

		Log.i(StringLiterals.LogTag, "Vault3.onActivityResult end");
	}

}