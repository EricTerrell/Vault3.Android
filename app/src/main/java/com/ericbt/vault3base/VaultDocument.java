/*
  Vault 3
  (C) Copyright 2023, Eric Bergman-Terrell
  
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
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;

import com.ericbt.vault3base.SearchParameters.SearchFields;
import com.ericbt.vault3base.SearchParameters.SearchScope;
import com.ericbt.vault3base.async.workers.Search;
import com.ericbt.vault3base.async.IReportProgress;

import commonCode.Base64Coder;
import commonCode.VaultDocumentVersion;
import commonCode.VaultException;

import fonts.AndroidFont;
import fonts.FontList;

public class VaultDocument {
	private static final String ENCRYPTED = "Encrypted";

	private static final String CIPHERTEXT_COLUMN_NAME = "Ciphertext";
	private static final String CIPHERTEXT_SALT_COLUMN_NAME = "salt";
	private static final String CIPHERTEXT_IV_COLUMN_NAME = "iv";

	private static final String DOCUMENT_VERSION = "DocumentVersion";

	private SQLiteDatabase database;
	
	public SQLiteDatabase getDatabase() {
		return database;
	}

	private VaultDocumentVersion documentVersion;
	
	public VaultDocumentVersion getDocumentVersion() {
		return documentVersion;
	}

	public void setDocumentVersion(VaultDocumentVersion documentVersion) {
		this.documentVersion = documentVersion;
	}

	private String password;

	public boolean setPassword(String password) {
		boolean setPassword = false;

		if (isEncrypted) {
			try {
				if (password != null) {
					final String cipherText = getVaultDocumentInfo(CIPHERTEXT_COLUMN_NAME);

					if (getDocumentVersion().compareTo(VaultDocumentVersion.VERSION_1_3) == 0) {
						final byte[] salt = Base64Coder.decode(getVaultDocumentInfo(CIPHERTEXT_SALT_COLUMN_NAME));
						final byte[] iv = Base64Coder.decode(getVaultDocumentInfo(CIPHERTEXT_IV_COLUMN_NAME));

						final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password,
								getDocumentVersion(), salt, iv);

						CryptoUtils.decryptString(decryptionCipher, cipherText);
					} else if (getDocumentVersion().compareTo(VaultDocumentVersion.VERSION_1_3) < 0) {
						final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(password,
								getDocumentVersion(), null, null);

						CryptoUtils.decryptString(decryptionCipher, cipherText);
					}

					this.password = password;

					setPassword = true;
				}
				else {
					this.password = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return setPassword;
	}

	private boolean dirty = false;

	public boolean isDirty() { return dirty; }

	public void setDirty(boolean dirty) { this.dirty = dirty; }

	boolean isEncrypted;
	
	public boolean isEncrypted() {
		return isEncrypted;
	}

	/**
	 * Retrieves the specified outline item
	 * @param id outline item id
	 * @param retrieveChildren specifies if immediate children are also retrieved.
	 * @return outline item with the specified id
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws VaultException
	 */
	public OutlineItem getOutlineItem(int id, boolean retrieveChildren) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException, VaultException,
			InvalidAlgorithmParameterException, InvalidKeySpecException {
		final long startTime = System.currentTimeMillis();

		final OutlineItem outlineItem = new OutlineItem();
		outlineItem.setId(id);

		String[] columns = new String[] { /*  0 */ TableAndColumnNames.OutlineItem.ID,
				/*  1 */ TableAndColumnNames.OutlineItem.ParentID,
				/*  2 */ TableAndColumnNames.OutlineItem.Title,
				/*  3 */ TableAndColumnNames.OutlineItem.Text,
				/*  4 */ TableAndColumnNames.OutlineItem.SortOrder,
				/*  5 */ TableAndColumnNames.OutlineItem.FontList,
				/*  6 */ TableAndColumnNames.OutlineItem.Red,
				/*  7 */ TableAndColumnNames.OutlineItem.Green,
				/*  8 */ TableAndColumnNames.OutlineItem.Blue,
				/*  9 */ TableAndColumnNames.OutlineItem.TitleSalt,
				/* 10 */ TableAndColumnNames.OutlineItem.TitleIV,
				/* 11 */ TableAndColumnNames.OutlineItem.TextSalt,
				/* 12 */ TableAndColumnNames.OutlineItem.TextIV
		};

		try (final Cursor cursor = database.query(
				TableAndColumnNames.OutlineItem.TableName,
				columns,
				String.format("%s = ?", TableAndColumnNames.OutlineItem.ID),
				new String[] { String.valueOf(id) }, null, null, null)) {
			if (cursor.moveToFirst()) {
				int parentID = cursor.getInt(1);
				
				String title = cursor.getString(2);
				String text = cursor.getString(3);

				if (isEncrypted) {
					{
						final byte[] salt = Base64Coder.decode(cursor.getString(9));
						final byte[] iv = Base64Coder.decode(cursor.getString(10));

						final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
								password, getDocumentVersion(), salt, iv);

						title = CryptoUtils.decryptString(decryptionCipher, title);
					}

					{
						final byte[] salt = Base64Coder.decode(cursor.getString(11));
						final byte[] iv = Base64Coder.decode(cursor.getString(12));

						final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
								password, getDocumentVersion(), salt, iv);

						text = CryptoUtils.decryptString(decryptionCipher, text);
					}
				}
				
				if (id == 1) {
					title = new File(database.getPath()).getName();
				}
	
				final int sortOrder = cursor.getInt(4);

				final String fontListString = cursor.getString(5);
				
				if (fontListString != null && fontListString.length() > 0) {
					FontList fontList = FontList.deserialize(fontListString);
					
					outlineItem.setFontList(fontList);
				}
				
				final RGBColor rgbColor = new RGBColor(cursor.getInt(6), cursor.getInt(7), cursor.getInt(8));
				outlineItem.setColor(rgbColor);
				
				outlineItem.setTitle(title);
				outlineItem.setText(text);
				outlineItem.setSortOrder(sortOrder);
				outlineItem.setParentId(parentID);
			}
			else {
				final String errorMessage = String.format("VaultDocument.getOutlineItem: cannot retrieve item %d from %s", id, database.getPath());
				
				Log.e(StringLiterals.LogTag, errorMessage);
				
				throw new VaultException(errorMessage);
			}
		}

		if (retrieveChildren) {
			final String queryString = "SELECT ID, ParentID, Title, Text, SortOrder, EXISTS(SELECT ID FROM OutlineItem OI2 WHERE OI2.ParentID = OI1.id LIMIT 1), FontList, Red, Green, Blue, TitleSalt, TitleIV, TextSalt, TextIV FROM OutlineItem OI1 WHERE ParentID = ? ORDER BY SortOrder";

			try (final Cursor cursor = database.rawQuery(
					queryString, new String[]{String.valueOf(id)});) {
				while (cursor.moveToNext()) {
					final int itemId = cursor.getInt(0);
					final int parentID = cursor.getInt(1);

					String title = cursor.getString(2);
					String text = cursor.getString(3);

					if (isEncrypted) {
						{
							final byte[] salt = Base64Coder.decode(cursor.getString(10));
							final byte[] iv = Base64Coder.decode(cursor.getString(11));

							final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
									password, getDocumentVersion(), salt, iv);

							title = CryptoUtils.decryptString(decryptionCipher, title);
						}

						{
							final byte[] salt = Base64Coder.decode(cursor.getString(12));
							final byte[] iv = Base64Coder.decode(cursor.getString(13));

							final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
									password, getDocumentVersion(), salt, iv);

							text = CryptoUtils.decryptString(decryptionCipher, text);
						}
					}

					int sortOrder = cursor.getInt(4);

					boolean hasChildren = cursor.getInt(5) == 1;

					OutlineItem childOutlineItem = new OutlineItem();
					childOutlineItem.setId(itemId);
					childOutlineItem.setParentId(parentID);
					childOutlineItem.setSortOrder(sortOrder);
					childOutlineItem.setTitle(title);
					childOutlineItem.setText(text);
					childOutlineItem.setHasChildren(hasChildren);

					final String fontListString = cursor.getString(6);

					if (fontListString != null && fontListString.length() > 0) {
						FontList fontList = FontList.deserialize(fontListString);

						childOutlineItem.setFontList(fontList);
					}

					RGBColor rgbColor = new RGBColor(cursor.getInt(7), cursor.getInt(8), cursor.getInt(9));
					childOutlineItem.setColor(rgbColor);

					outlineItem.addChild(childOutlineItem);
				}
			}

			outlineItem.setHasChildren(outlineItem.getChildren().size() > 0);
		}

		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.getOutlineItem: id %d %d ms", id, elapsedMilliseconds));

		return outlineItem;
	}

	public OutlineItem getOutlineItem(int id) throws InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException, VaultException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		return getOutlineItem(id, true);
	}

	private String getVaultDocumentInfo(String name) {
		final long startTime = System.currentTimeMillis();

		String value = null;

		final String[] columns = new String[] { TableAndColumnNames.VaultDocumentInfo.Value };
		final String selection = String.format("%s = '%s'",
				TableAndColumnNames.VaultDocumentInfo.Name, name);

		try (final Cursor cursor =
					 database.query(
							 TableAndColumnNames.VaultDocumentInfo.TableName,
							 columns,
							 selection,
							 null, null, null, null)) {

			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
			}
		}

		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.getVaultDocumentInfo: %d ms",
				elapsedMilliseconds));

		return value;
	}

	private void setVaultDocumentInfo(String name, String value) throws VaultException {
		long startTime = System.currentTimeMillis();

		final String queryString = String.format("DELETE FROM %s WHERE %s = '%s'",
				TableAndColumnNames.VaultDocumentInfo.TableName,
				TableAndColumnNames.VaultDocumentInfo.Name,
				name);

		// Remove original row, if there is one.
		database.execSQL(queryString);

		final ContentValues columnValues = new ContentValues();
		columnValues.put(TableAndColumnNames.VaultDocumentInfo.Name, name);
		columnValues.put(TableAndColumnNames.VaultDocumentInfo.Value, value);

		final long id = database.insert(
				TableAndColumnNames.VaultDocumentInfo.TableName,
				null,
				columnValues);

		if (id == -1) {
			String errorMessage = String.format("VaultDocument.setVaultDocumentInfo: cannot set %s to \"%s\"", name, value);
			
			Log.e(StringLiterals.LogTag, errorMessage);
			
			throw new VaultException(errorMessage);
		}
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.setVaultDocumentInfo: %d ms", elapsedMilliseconds));
	}

	private void updateVaultDocumentInfoEncryptionData() throws VaultException,
			UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException {
		if (isEncrypted) {
			final byte[] salt = CryptoUtils.createSalt();
			final byte[] iv = CryptoUtils.createIV();

			final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
					password, getDocumentVersion(), salt, iv);

			final String cipherText = CryptoUtils.encryptString(encryptionCipher, getRandomPlainText());

			setVaultDocumentInfo(CIPHERTEXT_COLUMN_NAME, cipherText);
			setVaultDocumentInfo(CIPHERTEXT_SALT_COLUMN_NAME, new String(Base64Coder.encode(salt)));
			setVaultDocumentInfo(CIPHERTEXT_IV_COLUMN_NAME, new String(Base64Coder.encode(iv)));
		} else {
			deleteVaultDocumentInfo(CIPHERTEXT_COLUMN_NAME);
			deleteVaultDocumentInfo(CIPHERTEXT_SALT_COLUMN_NAME);
			deleteVaultDocumentInfo(CIPHERTEXT_IV_COLUMN_NAME);
		}
	}

	private void deleteVaultDocumentInfo(String name) {
		long startTime = System.currentTimeMillis();

		String whereClause = String.format("%s = ?", TableAndColumnNames.VaultDocumentInfo.Name);
		database.delete(TableAndColumnNames.VaultDocumentInfo.TableName, whereClause, new String[] { name });

		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.deleteVaultDocumentInfo: %d ms", elapsedMilliseconds));
	}
	
	/**
	 * Determine if the text in this database is encrypted
	 * @return true if text is encrypted, false otherwise
	 */
	private boolean databaseIsEncrypted() {
		final String value = getVaultDocumentInfo(ENCRYPTED);

		return value.equals("1");
	}

	private List<Integer> getChildren(int id) {
		final ArrayList<Integer> childrenWithChildren = new ArrayList<>();
		final ArrayList<Integer> childrenWithoutChildren = new ArrayList<>();

		final String queryString = "SELECT ID, EXISTS(SELECT ID FROM OutlineItem OI2 WHERE OI2.ParentID = OI1.id LIMIT 1) FROM OutlineItem OI1 WHERE ParentID = ?";

		try (final Cursor cursor = database.rawQuery(queryString,
				new String[] { String.valueOf(id) })) {
			while (cursor.moveToNext()) {
				int childID = cursor.getInt(0);
				final boolean hasChildren = cursor.getInt(1) == 1;
				
				if (hasChildren) {
					childrenWithChildren.add(childID);
				}
				else {
					childrenWithoutChildren.add(childID);
				}
			}
		}

		final List<Integer> allChildren = new ArrayList<>();
		allChildren.addAll(childrenWithChildren);
		allChildren.addAll(childrenWithoutChildren);
		
		for (Integer childID : childrenWithChildren) {
			final List<Integer> recursiveChildren = getChildren(childID);
			
			allChildren.addAll(recursiveChildren);
		}
		
		return allChildren;
	}
	
	public List<Integer> removeOutlineItem(int id) throws VaultException {
		long startTime = System.currentTimeMillis();

		final List<Integer> outlineItemsRemoved = remove(id);
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.removeOutlineItem: %d ms", elapsedMilliseconds));
		
		return outlineItemsRemoved;
	}
	
	/**
	 * Remove the outline item with the specified ID, and all subordinate items, recursively.
	 * @param id ID of outline item
	 * @throws VaultException
	 */
	private List<Integer> remove(int id) throws VaultException {
		final long startTime = System.currentTimeMillis();

		final List<Integer> idsToDelete = getChildren(id);
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.remove: %d ms to get child ids", elapsedMilliseconds));

		idsToDelete.add(id);
		
		database.beginTransaction();
		
		try {
			for (int idToDelete : idsToDelete) {
				final int rowsDeleted = database.delete(TableAndColumnNames.OutlineItem.TableName,
												  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
												  new String[] { String.valueOf(idToDelete) });
				
				if (rowsDeleted != 1) {
					throw new VaultException(String.format("VaultDocument.remove: cannot remove id %d", id));
				}
			}
			
			database.setTransactionSuccessful();
		}
		finally {
			database.endTransaction();
		}

		elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.remove: %d ms (%d items removed)", elapsedMilliseconds, idsToDelete.size()));
		
		return idsToDelete;
	}

	/**
	 * Determine ID of item below which to indent the specified outline item.
	 * @param parentID id of outline item's parent
	 * @param sortOrder item's sort order
	 * @return ID of item that will be the specified item's parent after indentation, or -1 if item cannot be indented.
	 */
	private int getIndentItemID(int parentID, int sortOrder) {
		final long startTime = System.currentTimeMillis();

		int indentItemID = -1;

		final String queryString = String.format("SELECT ID FROM OutlineItem WHERE ParentID = %d AND SortOrder < %d ORDER BY SortOrder DESC LIMIT 1", parentID, sortOrder);
		
		try (final Cursor cursor = database.rawQuery(queryString, null)) {
			if (cursor.moveToNext()) {
				indentItemID = cursor.getInt(0);
			}
		}

		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.getIndentItemID: %d ms",
				elapsedMilliseconds));

		return indentItemID;
	}
	
	/**
	 * Determine if the specified outline item can be indented. It can, if it has a sibling with a lower sort order.
	 * @param outlineItem outline item
	 * @return true if the outline item can be indented
	 */
	public boolean canIndent(OutlineItem outlineItem) {
		return getIndentItemID(outlineItem.getParentId(), outlineItem.getSortOrder()) != -1;
	}
	
	/**
	 * Indent the specified outline item below the sibling before it in the sort order, if such a sibling exists.
	 * @param outlineItem outline item
	 * @return id of outline item's new parent
	 * @throws VaultException 
	 */
	public int indent(OutlineItem outlineItem) throws VaultException {
		long startTime = System.currentTimeMillis();

		int newParentID = getIndentItemID(outlineItem.getParentId(), outlineItem.getSortOrder());
		int maxSortOrder = getMaxSortOrder(newParentID);
		
		if (newParentID != -1) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("ParentID", newParentID);
			contentValues.put("SortOrder", maxSortOrder + 1);
			
			database.beginTransaction();
			
			try {
				int rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName, 
												  contentValues, 
												  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
												  new String[] { String.valueOf(outlineItem.getId()) });
				
				if (rowsUpdated != 1) {
					String errorMessage = String.format("VaultDocument.indent, could not indent id: %d, parentID %d, sortOrder: %d", outlineItem.getId(), outlineItem.getParentId(), outlineItem.getSortOrder());
					Log.e(StringLiterals.LogTag, errorMessage);
					throw new VaultException(errorMessage);
				}
				
				database.setTransactionSuccessful();
			}
			finally {
				database.endTransaction();
			}
		}
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.indent: %d ms", elapsedMilliseconds));

		return newParentID;
	}
	
	/**
	 * Return the largest SortOrder value for OutlineItems with the specified parent id
	 * @param parentID specified parent id
	 * @return largest SortOrder value, or -1 if no records found
	 */
	private int getMaxSortOrder(int parentID) {
		long startTime = System.currentTimeMillis();

		int maxSortOrder = -1;

		final String queryString = String.format(
				"SELECT MAX(SortOrder) FROM OutlineItem WHERE ParentID = %d", parentID);
		
		try (final Cursor cursor = database.rawQuery(queryString, null)) {
			if (cursor.moveToNext()) {
				maxSortOrder = cursor.getInt(0);
			}
		}

		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.getMaxSortOrder: %d ms", elapsedMilliseconds));

		return maxSortOrder;
	}
	
	/**
	 * Returns the ID of the specified item's parent
	 * @param id specified outline item
	 * @return parent id
	 */
	private int getParentID(int id) {
		long startTime = System.currentTimeMillis();

		int parentID = -1;

		try (final Cursor cursor = database.query(TableAndColumnNames.OutlineItem.TableName,
				new String[]{TableAndColumnNames.OutlineItem.ParentID},
				String.format("%s = ?", TableAndColumnNames.OutlineItem.ID),
				new String[]{String.valueOf(id)}, null, null, null)) {

			if (cursor.moveToFirst()) {
				parentID = cursor.getInt(0);
			}
		}
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.getParentID: %d ms", elapsedMilliseconds));

		return parentID;
	}
	
	public boolean canUnIndent(OutlineItem outlineItem) {
		return outlineItem.getParentId() != 1;
	}
	
	public int unIndent(OutlineItem outlineItem) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, VaultException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		final long startTime = System.currentTimeMillis();

		final int newParentID = getParentID(outlineItem.getParentId());

		final OutlineItem parentOutlineItem = getOutlineItem(outlineItem.getParentId());
		
		database.beginTransaction();
		
		try {
			createGapInSortOrder(newParentID, parentOutlineItem.getSortOrder());
			
			ContentValues contentValues = new ContentValues();
			contentValues.put(TableAndColumnNames.OutlineItem.ParentID, newParentID);
			contentValues.put(TableAndColumnNames.OutlineItem.SortOrder,
					parentOutlineItem.getSortOrder() + 1);

			final int rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName,
											  contentValues, 
											  String.format("%s = ?",
													  TableAndColumnNames.OutlineItem.ID),
											  new String[] { String.valueOf(outlineItem.getId()) });
			
			if (rowsUpdated != 1) {
				final String errorMessage =
						String.format("VaultDocument.unIndent, could not unindent id: %d",
								outlineItem.getId());
				Log.e(StringLiterals.LogTag, errorMessage);
				throw new VaultException(errorMessage);
			}

			database.setTransactionSuccessful();
		}
		finally {
			database.endTransaction();
		}
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.unIndent: %d ms",
				elapsedMilliseconds));

		return newParentID;
	}
	
	private boolean canMoveUpOrDown(OutlineItem outlineItem, boolean up) {
		long startTime = System.currentTimeMillis();

		boolean canMoveUpOrDown = false;
		
		final String queryString = String.format("SELECT EXISTS(SELECT * FROM OutlineItem WHERE ParentID = ? AND SortOrder %s ? LIMIT 1)", up ? "<" : ">");

		try (final Cursor cursor = database.rawQuery(
				queryString,
				new String[] {
						String.valueOf(outlineItem.getParentId()),
						String.valueOf(outlineItem.getSortOrder()) })) {
			// Item can move up if it has a sibling with lower sort order.
			// Item can move down if it has a sibling with higher sort order.
			if (cursor.moveToNext()) {
				canMoveUpOrDown = cursor.getInt(0) == 1;
			}
		}

		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.canMoveUpOrDown: %d ms",
				elapsedMilliseconds));

		return canMoveUpOrDown;
	}
	
	public boolean canMoveUp(OutlineItem outlineItem) {
		return canMoveUpOrDown(outlineItem, true);
	}
	
	private void moveUpOrDown(OutlineItem outlineItem, boolean up) throws VaultException {
		final long startTime = System.currentTimeMillis();

		// Get ID of item above or below current item.

		Cursor cursor = null;

		try {
			// Item can move up if it has a sibling with higher sort order.
			final String queryString = String.format("SELECT ID, SortOrder FROM OutlineItem WHERE ParentID = ? AND SortOrder %s ? ORDER BY SortOrder %s LIMIT 1", up ? "<" : ">", up ? "DESC" : "ASC");
			cursor = database.rawQuery(queryString, new String[] { String.valueOf(outlineItem.getParentId()), String.valueOf(outlineItem.getSortOrder()) });

			if (cursor.moveToNext()) {
				int siblingID = cursor.getInt(0);
				int siblingSortOrder = cursor.getInt(1);
				
				cursor.close();
				cursor = null;
				
				database.beginTransaction();
				
				try {
					ContentValues contentValues = new ContentValues();
					contentValues.put("SortOrder", outlineItem.getSortOrder());
					int rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName, 
													  contentValues, 
													  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
													  new String[] { String.valueOf(siblingID) });
					
					if (rowsUpdated != 1) {
						Log.e(StringLiterals.LogTag, String.format("VaultDocument.unIndent, could not moveUp id: %d", outlineItem.getId()));
						
						throw new VaultException("VaultDocument.moveUpOrDown: could not move");
					}
					
					contentValues = new ContentValues();
					contentValues.put("SortOrder", siblingSortOrder);
					rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName, 
												  contentValues, 
												  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
												  new String[] { String.valueOf(outlineItem.getId()) });
					
					if (rowsUpdated != 1) {
						Log.e(StringLiterals.LogTag, String.format("VaultDocument.unIndent, could not moveUp id: %d", outlineItem.getId()));
						
						throw new VaultException("VaultDocument.moveUpOrDown: could not move 2");
					}
					
					database.setTransactionSuccessful();
				}
				finally {
					database.endTransaction();
				}
			}
		}
		finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.moveUpOrDown: %d ms",
				elapsedMilliseconds));
	}

	public void moveUp(OutlineItem outlineItem) throws VaultException {
		moveUpOrDown(outlineItem, true);
	}
	
	public boolean canMoveDown(OutlineItem outlineItem) {
		return canMoveUpOrDown(outlineItem, false);
	}
	
	public void moveDown(OutlineItem outlineItem) throws VaultException {
		moveUpOrDown(outlineItem, false);
	}

	private int getOutlineItemCount() throws VaultException {
		final long startTime = System.currentTimeMillis();

		int outlineItemCount = 0;
		
		final String queryString = "SELECT COUNT(*) FROM OutlineItem";

		try (final Cursor cursor = database.rawQuery(queryString, null)) {
			if (cursor.moveToNext()) {
				outlineItemCount = cursor.getInt(0);
			}
			else {
				throw new VaultException("VaultDocument.getOutlineItemCount: cannot determine count");
			}
		}

		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag,
				String.format("VaultDocument.getOutlineItemCount: %d ms (%d items)",
						elapsedMilliseconds, outlineItemCount));

		return outlineItemCount;
	}
	
	public boolean canMove() throws VaultException {
		return getOutlineItemCount() > 1;
	}
	
	/**
	 * Determine if the outline item being moved can be made a sibling of the selected outline item. The selected outline item cannot be subordinate
	 * to the item being moved.
	 * @param movingOutlineItem item being moved
	 * @param selectedOutlineItem potential sibling if item being moved
	 * @return true if the item being moved can be a sibling of the selected outline item
	 */
	public boolean canMoveTo(OutlineItem movingOutlineItem, OutlineItem selectedOutlineItem) {
		boolean canMoveTo = true;
		
		if (movingOutlineItem == null || selectedOutlineItem == null || movingOutlineItem.getId() == selectedOutlineItem.getId()) {
			canMoveTo = false;
		}
		else {
			boolean done = false;
			int id = selectedOutlineItem.getId();
			
			do {
				int parentID = getParentID(id);
				
				if (parentID == movingOutlineItem.getId()) {
					canMoveTo = false;
					done = true;
				}
				else if (parentID == 0) {
					done = true;
				}
				
				id = parentID;
			} while (!done);
		}
		
		return canMoveTo;
	}
	
	public void moveTo(OutlineItem movingOutlineItem, OutlineItem selectedOutlineItem, boolean placeAboveSelectedItem) throws VaultException {
		final long startTime = System.currentTimeMillis();

		try {
			database.beginTransaction();

			createGapInSortOrder(selectedOutlineItem.getParentId(), selectedOutlineItem.getSortOrder());

			// Move the movingOutlineItem to the slot right after the selectedOutlineItem.
			ContentValues contentValues = new ContentValues();
			contentValues.put(TableAndColumnNames.OutlineItem.ParentID, selectedOutlineItem.getParentId());
			contentValues.put(TableAndColumnNames.OutlineItem.SortOrder, selectedOutlineItem.getSortOrder() + 1);

			final int rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName,
											  contentValues, 
											  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
											  new String[] { String.valueOf(movingOutlineItem.getId()) });

			if (rowsUpdated != 1) {
				Log.e(StringLiterals.LogTag, String.format("VaultDocument.moveTo, could not move item %d to %d", movingOutlineItem.getId(), selectedOutlineItem.getId()));
				
				throw new VaultException(String.format("VaultDocument.moveTo: could not move item %d to %d", movingOutlineItem.getId(), selectedOutlineItem.getId()));
			}

			// If the movingOutlineItem should be above the selectedOutlineItem, swap the sort orders.
			if (placeAboveSelectedItem) {
				swapSortOrders(selectedOutlineItem, movingOutlineItem);
			}
			
			database.setTransactionSuccessful();
		}
		finally {
			database.endTransaction();
		}
		
		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.unIndent: %d ms",
				elapsedMilliseconds));
	}

	public boolean canAdd() {
		return true;
	}

	private int getLastInsertRowID() throws VaultException {
		int lastInsertRowID = -1;

		try (final Cursor cursor =
					 database.rawQuery("SELECT last_insert_rowid()", null)) {
			if (cursor.moveToNext()) {
				lastInsertRowID = cursor.getInt(0);
			} else {
				throw new VaultException("VaultDocument.getLastInsertRowID: cannot retrieve new item id");
			}
		}
		
		return lastInsertRowID;
	}
	
	private void createGapInSortOrder(int parentID, int sortOrder) {
		final long startTime = System.currentTimeMillis();

		database.beginTransaction();
		
		try {
			final String queryString = "UPDATE OutlineItem SET SortOrder = SortOrder + 1 WHERE ParentID = ? AND SortOrder > ?";
			database.execSQL(queryString, new String[] { String.valueOf(parentID), String.valueOf(sortOrder) } );
			
			database.setTransactionSuccessful();
		}
		finally {
			database.endTransaction();
		}
		
		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.createGapInSortOrder: %d ms", elapsedMilliseconds));
	}

	private void swapSortOrders(OutlineItem selectedOutlineItem, OutlineItem newOutlineItem) throws VaultException {
		final long startTime = System.currentTimeMillis();

		database.beginTransaction();
		
		try {
			ContentValues contentValues = new ContentValues();
			contentValues.put(TableAndColumnNames.OutlineItem.SortOrder, selectedOutlineItem.getSortOrder() + 1);
	
			int rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName, 
											  contentValues, 
											  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
											  new String[] { String.valueOf(selectedOutlineItem.getId()) });
	
			if (rowsUpdated != 1) {
				Log.e(StringLiterals.LogTag, String.format("VaultDocument.add, %d, %d", newOutlineItem.getId(), selectedOutlineItem.getId()));
				
				throw new VaultException("VaultDocument.swapSortOrders: cannot swap sort order 1");
			}
			
			contentValues = new ContentValues();
			contentValues.put(TableAndColumnNames.OutlineItem.SortOrder, selectedOutlineItem.getSortOrder());
	
			rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName, 
										  contentValues, 
										  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
										  new String[] { String.valueOf(newOutlineItem.getId()) });
			
			if (rowsUpdated != 1) {
				Log.e(StringLiterals.LogTag, String.format("VaultDocument.add, %d, %d", newOutlineItem.getId(), selectedOutlineItem.getId()));
	
				throw new VaultException("VaultDocument.swapSortOrders: cannot swap sort order 2");
			}
			
			database.setTransactionSuccessful();
		}
		finally {
			database.endTransaction();
		}

		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.swapSortOrders: %d ms", elapsedMilliseconds));
	}
	
	public void add(OutlineItem newOutlineItem, OutlineItem selectedOutlineItem, boolean placeAboveSelectedItem) throws VaultException, InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		final long startTime = System.currentTimeMillis();

		if (selectedOutlineItem != null) {
			database.beginTransaction();
			
			try {
				createGapInSortOrder(selectedOutlineItem.getParentId(), selectedOutlineItem.getSortOrder());
	
				String title = newOutlineItem.getTitle();
				String text = newOutlineItem.getText();

				String titleSalt = StringLiterals.EmptyString, titleIV = StringLiterals.EmptyString,
						textSalt = StringLiterals.EmptyString, textIV = StringLiterals.EmptyString;

				if (isEncrypted) {
					{
						final byte[] salt = CryptoUtils.createSalt();
						titleSalt = new String(Base64Coder.encode(salt));

						final byte[] iv = CryptoUtils.createIV();
						titleIV = new String(Base64Coder.encode(iv));

						final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(password,
								getDocumentVersion(), salt, iv);

						title = CryptoUtils.encryptString(encryptionCipher, title);
					}

					{
						final byte[] salt = CryptoUtils.createSalt();
						textSalt = new String(Base64Coder.encode(salt));

						final byte[] iv = CryptoUtils.createIV();
						textIV = new String(Base64Coder.encode(iv));

						final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(password,
								getDocumentVersion(), salt, iv);

						text = CryptoUtils.encryptString(encryptionCipher, text);
					}
				}

				// Insert the newOutlineItem to the slot right after the selectedOutlineItem.
				final String queryString = "INSERT INTO OutlineItem(Title, TitleSalt, TitleIV, Text, TextSalt, TextIV, SortOrder, ParentID) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

				database.execSQL(queryString,
								 new String[] {
										 title, titleSalt, titleIV,
										 text, textSalt, textIV,
										 String.valueOf(selectedOutlineItem.getSortOrder() + 1),
										 String.valueOf(selectedOutlineItem.getParentId()) } );
	
				newOutlineItem.setId(getLastInsertRowID());
				
				// If the newOutlineItem should be above the selectedOutlineItem, swap the sort orders.
				if (placeAboveSelectedItem) {
					swapSortOrders(selectedOutlineItem, newOutlineItem);
				}
				
				database.setTransactionSuccessful();
			}
			finally {
				database.endTransaction();
			}
		}
		else /* add first non-root item as child of the root */ {
			final String queryString = "INSERT INTO OutlineItem(Title, Text, SortOrder, ParentID) VALUES(?, ?, 0, 1)";
			database.execSQL(queryString, new String[] { newOutlineItem.getTitle(), newOutlineItem.getText() } );
		}
		
		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.add: %d ms",
				elapsedMilliseconds));
	}
	
	/**
	 * Update the outline item's title and text
	 * @param outlineItem outline item
	 * @param newTitle new title
	 * @param newText new text
	 * @throws VaultException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws UnsupportedEncodingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public void updateOutlineItem(OutlineItem outlineItem, String newTitle, String newText)
			throws VaultException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		long startTime = System.currentTimeMillis();

		String titleSalt = StringLiterals.EmptyString, titleIV = StringLiterals.EmptyString,
				textSalt = StringLiterals.EmptyString, textIV = StringLiterals.EmptyString;

		if (isEncrypted) {
			{
				final byte[] salt = CryptoUtils.createSalt();
				titleSalt = new String(Base64Coder.encode(salt));

				final byte[] iv = CryptoUtils.createIV();
				titleIV = new String(Base64Coder.encode(iv));

				final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
						password,
						getDocumentVersion(),
						salt,
						iv);

				newTitle = CryptoUtils.encryptString(encryptionCipher, newTitle);
			}

			{
				final byte[] salt = CryptoUtils.createSalt();
				textSalt = new String(Base64Coder.encode(salt));

				final byte[] iv = CryptoUtils.createIV();
				textIV = new String(Base64Coder.encode(iv));

				final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
						password,
						getDocumentVersion(),
						salt,
						iv);

				newText = CryptoUtils.encryptString(encryptionCipher, newText);
			}
		}
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(TableAndColumnNames.OutlineItem.Title, newTitle);
		contentValues.put(TableAndColumnNames.OutlineItem.TitleSalt, titleSalt);
		contentValues.put(TableAndColumnNames.OutlineItem.TitleIV, titleIV);
		contentValues.put(TableAndColumnNames.OutlineItem.Text, newText);
		contentValues.put(TableAndColumnNames.OutlineItem.TextSalt, textSalt);
		contentValues.put(TableAndColumnNames.OutlineItem.TextIV, textIV);

		int rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName, 
										  contentValues, 
										  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
										  new String[] { String.valueOf(outlineItem.getId()) });
		
		if (rowsUpdated != 1) {
			throw new VaultException(String.format("VaultDocument.updateOutlineText: cannot update text for %d", outlineItem.getId()));
		}

		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.updateOutlineItem: %d ms", elapsedMilliseconds));
	}

	public void search(SearchParameters searchParameters, Search search) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, VaultException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		final long startTime = System.currentTimeMillis();
		
		SparseBooleanArray searchScopeItems = null;
		
		if (searchParameters.getSearchScope() == SearchScope.SelectedOnly) {
			final List<Integer> children = getChildren(searchParameters.getSearchScopeID());
			
			searchScopeItems = new SparseBooleanArray();
			
			for (Integer child : children) {
				searchScopeItems.put(child, true);
			}
		}

		String[] columns = new String[]
				{
						/*  0 */ TableAndColumnNames.OutlineItem.ID,
						/*  1 */ TableAndColumnNames.OutlineItem.Title,
						/*  2 */ TableAndColumnNames.OutlineItem.Text,
						/*  3 */ TableAndColumnNames.OutlineItem.ParentID,
						/*  4 */ TableAndColumnNames.OutlineItem.FontList,
						/*  5 */ TableAndColumnNames.OutlineItem.Red,
						/*  6 */ TableAndColumnNames.OutlineItem.Green,
						/*  7 */ TableAndColumnNames.OutlineItem.Blue,
						/*  8 */ TableAndColumnNames.OutlineItem.TitleSalt,
						/*  9 */ TableAndColumnNames.OutlineItem.TitleIV,
						/* 10 */ TableAndColumnNames.OutlineItem.TextSalt,
						/* 11 */ TableAndColumnNames.OutlineItem.TextIV
				};

		try (final Cursor cursor = database.query(
				TableAndColumnNames.OutlineItem.TableName, columns,
				null, null, null, null, null)) {
			final int maxSearchHits = VaultPreferenceActivity.getMaxSearchHits();
			int searchHits = 0;
			
			while (cursor.moveToNext() && searchHits < maxSearchHits) {
				OutlineItem outlineItem = new OutlineItem();
				outlineItem.setId(cursor.getInt(0));
				outlineItem.setParentId(cursor.getInt(3));
				
				if ((searchParameters.getSearchScope() == SearchScope.All) || (searchParameters.getSearchScope() == SearchScope.SelectedOnly && searchScopeItems.get(outlineItem.getId(), false))) {
					outlineItem.setTitle(cursor.getString(1));
					outlineItem.setText(cursor.getString(2));

					boolean isTextDecrypted = !isEncrypted;
					
					if (isEncrypted) {
						{
							final byte[] salt = Base64Coder.decode(cursor.getString(8));
							final byte[] iv = Base64Coder.decode(cursor.getString(9));

							final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
									password, getDocumentVersion(), salt, iv);

							outlineItem.setTitle(CryptoUtils.decryptString(decryptionCipher, outlineItem.getTitle()));
						}

						if (searchParameters.getSearchFields() == SearchFields.TitlesAndText) {
							{
								final byte[] salt = Base64Coder.decode(cursor.getString(10));
								final byte[] iv = Base64Coder.decode(cursor.getString(11));

								final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
										password, getDocumentVersion(), salt, iv);

								outlineItem.setText(CryptoUtils.decryptString(decryptionCipher, outlineItem.getText()));

								isTextDecrypted = true;
							}
						}
					}
					
					final boolean isHit = searchParameters.isMatch(outlineItem);
					
					if (isHit) {
						if (!isTextDecrypted) {
							final byte[] salt = Base64Coder.decode(cursor.getString(10));
							final byte[] iv = Base64Coder.decode(cursor.getString(11));

							final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
									password, getDocumentVersion(), salt, iv);

							outlineItem.setText(CryptoUtils.decryptString(decryptionCipher, outlineItem.getText()));
						}
					
						final String fontListString = cursor.getString(4);
						
						if (fontListString != null && fontListString.length() > 0) {
							FontList fontList = FontList.deserialize(fontListString);
							
							outlineItem.setFontList(fontList);
						}
						
						final RGBColor rgbColor = new RGBColor(cursor.getInt(5), cursor.getInt(6), cursor.getInt(7));
						outlineItem.setColor(rgbColor);
						
						final OutlineItem parent = getOutlineItem(outlineItem.getParentId());
						final SearchHit searchHit = new SearchHit(outlineItem, parent);
						
						search.reportProgress(searchHit);
						
						searchHits++;
					}
					
					if (search.isCancelled()) {
						Log.i(StringLiterals.LogTag, "VaultDocument.search: cancelled");
						break;
					}
				}
			}
		}

		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.search: %d ms",
				elapsedMilliseconds));
	}

	public boolean canSort(OutlineItem outlineItem) {
		return outlineItem.getChildren().size() > 1;
	}
	
	public void sort(OutlineItem parentOutlineItem) throws VaultException {
		final long startTime = System.currentTimeMillis();

		final OutlineItem[] array = new OutlineItem[parentOutlineItem.getChildren().size()];
		
		int index = 0;
		
		for (OutlineItem child : parentOutlineItem.getChildren()) {
			array[index++] = child;
		}
		
		Arrays.sort(array, new OutlineItemComparitor());
		
		database.beginTransaction();
		
		try {
			for (index = 0; index < array.length; index++) {
				OutlineItem outlineItem = array[index];
				
				if (outlineItem.getSortOrder() != index) {
					ContentValues contentValues = new ContentValues();
					contentValues.put(TableAndColumnNames.OutlineItem.SortOrder, index);
					
					int rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName, 
													  contentValues, 
													  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
													  new String[] { String.valueOf(outlineItem.getId()) });
					
					if (rowsUpdated != 1) {
						throw new VaultException(String.format("VaultDocument.sort: cannot sort OutlineItem %d", outlineItem.getId()));
					}
				}
			}
			
			database.setTransactionSuccessful();
		}
		finally {
			database.endTransaction();
		}

		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.search: %d ms",
				elapsedMilliseconds));
	}

	public void close() {
		if (database != null) {
			database.close();
			database = null;
		}
	}

	public static void createNewVaultDocument(String dbFilePath) throws VaultException {
		final long startTime = System.currentTimeMillis();

		final File dbFile = new File(dbFilePath);
		
		if (dbFile.exists()) {
			boolean deleted = dbFile.delete();
			
			if (!deleted) {
				final String errorMessage = String.format("VaultDocument.createNewVaultDocument: cannot delete %s", dbFilePath);
				Log.e(StringLiterals.LogTag, errorMessage);
				
				throw new VaultException(errorMessage);
			}
		}

		try (final SQLiteDatabase db =
					 SQLiteDatabase.openOrCreateDatabase(dbFilePath, null)) {
			db.execSQL("INSERT INTO \"android_metadata\" VALUES ('en_US')");

			String createTable =
					"CREATE TABLE VaultDocumentInfo(" +
							"Name TEXT NOT NULL PRIMARY KEY, Value TEXT NOT NULL)";
			db.execSQL(createTable);

			final String maxVersion = VaultDocumentVersion.getLatestVaultDocumentVersion().toString();

			db.execSQL(String.format("INSERT INTO VaultDocumentInfo(Name, Value) VALUES('DocumentVersion', '%s')", maxVersion));
			db.execSQL("INSERT INTO VaultDocumentInfo(Name, Value) VALUES('Encrypted', '0')");

			createTable =
					"CREATE TABLE OutlineItem(" +
							"ID INTEGER PRIMARY KEY, ParentID INTEGER, Title TEXT, TitleSalt TEXT, TitleIV TEXT, Text TEXT, TextSalt TEXT, TextIV Text, FontList TEXT," +
							"Red INTEGER DEFAULT 0, Green INTEGER DEFAULT 0, Blue INTEGER DEFAULT 0," +
							"PhotoPath TEXT, AllowScaling INTEGER DEFAULT 1, SortOrder INTEGER" +
							")";

			db.execSQL(createTable);

			// Insert root OutlineItem.
			db.execSQL("INSERT INTO OutlineItem(Title, Text, ParentID, SortOrder) VALUES(\"\", \"\", 0, 0)");

			final String createIndex = "CREATE INDEX ParentIDIndex ON OutlineItem(ParentID)";
			db.execSQL(createIndex);
		}
		
		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.createNewVaultDocument: %d ms",
				elapsedMilliseconds));
	}

	private enum ChangePasswordAction { addPassword, removePassword, changePassword, noChange }

	private ChangePasswordAction determinePasswordAction(String newPassword) {
		final ChangePasswordAction action;

		final boolean newPasswordExists = newPassword != null && newPassword.length() > 0;

		if (!isEncrypted && newPasswordExists) {
			action = ChangePasswordAction.addPassword;
		}
		else if (isEncrypted && !newPasswordExists) {
			action = ChangePasswordAction.removePassword;
		}
		else if (isEncrypted && newPasswordExists && !newPassword.equals(password)) {
			action = ChangePasswordAction.changePassword;
		}
		else {
			action = ChangePasswordAction.noChange;
		}

		return action;
	}

	public void changePassword(String newPassword, IReportProgress reportProgress)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException,
			VaultException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		final long startTime = System.currentTimeMillis();

		final ChangePasswordAction action = determinePasswordAction(newPassword);
		
		if (action != ChangePasswordAction.noChange) {
			database.beginTransaction();

			String[] columns = new String[] {
					/* 0 */ TableAndColumnNames.OutlineItem.ID,
					/* 1 */ TableAndColumnNames.OutlineItem.Title,
					/* 2 */ TableAndColumnNames.OutlineItem.Text,
					/* 3 */ TableAndColumnNames.OutlineItem.TitleSalt,
					/* 4 */ TableAndColumnNames.OutlineItem.TitleIV,
					/* 5 */ TableAndColumnNames.OutlineItem.TextSalt,
					/* 6 */ TableAndColumnNames.OutlineItem.TextIV };

			try (final Cursor cursor = database.query(
					TableAndColumnNames.OutlineItem.TableName,
					columns,
					null, null, null, null, null)) {
				final int totalItems = getOutlineItemCount();
				int itemsProcessed = 0;

				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String title = cursor.getString(1);
					String text = cursor.getString(2);

					if (isEncrypted) {
						// Decrypt title.
						{
							final byte[] salt = Base64Coder.decode(cursor.getString(3));
							final byte[] iv = Base64Coder.decode(cursor.getString(4));

							final Cipher decryptionCipher =
									CryptoUtils.createDecryptionCipher(
											password, getDocumentVersion(), salt, iv);

							title = CryptoUtils.decryptString(decryptionCipher, title);
						}

						// Decrypt text.
						{
							final byte[] salt = Base64Coder.decode(cursor.getString(5));
							final byte[] iv = Base64Coder.decode(cursor.getString(6));

							final Cipher decryptionCipher =
									CryptoUtils.createDecryptionCipher(password,
											getDocumentVersion(), salt, iv);

							text = CryptoUtils.decryptString(decryptionCipher, text);
						}
					}

					String titleSalt = StringLiterals.EmptyString,
							titleIV = StringLiterals.EmptyString,
							textSalt = StringLiterals.EmptyString,
							textIV = StringLiterals.EmptyString;

					if (action == ChangePasswordAction.addPassword ||
							action == ChangePasswordAction.changePassword) {

						// Encrypt title.
						{
							final byte[] salt = CryptoUtils.createSalt();
							titleSalt = new String(Base64Coder.encode(salt));

							final byte[] iv = CryptoUtils.createIV();
							titleIV = new String(Base64Coder.encode(iv));

							final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
									newPassword, getDocumentVersion(), salt, iv);

							title = CryptoUtils.encryptString(encryptionCipher, title);
						}

						// Encrypt text.
						{
							final byte[] salt = CryptoUtils.createSalt();
							textSalt = new String(Base64Coder.encode(salt));

							final byte[] iv = CryptoUtils.createIV();
							textIV = new String(Base64Coder.encode(iv));

							final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
									newPassword, getDocumentVersion(), salt, iv);

							text = CryptoUtils.encryptString(encryptionCipher, text);
						}
					}
					
					ContentValues contentValues = new ContentValues();
					contentValues.put(TableAndColumnNames.OutlineItem.Title, title);
					contentValues.put(TableAndColumnNames.OutlineItem.TitleSalt, titleSalt);
					contentValues.put(TableAndColumnNames.OutlineItem.TitleIV, titleIV);
					contentValues.put(TableAndColumnNames.OutlineItem.Text, text);
					contentValues.put(TableAndColumnNames.OutlineItem.TextSalt, textSalt);
					contentValues.put(TableAndColumnNames.OutlineItem.TextIV, textIV);

					final String whereClause = String.format("%s = ?",
							TableAndColumnNames.OutlineItem.ID);
					
					final int rowsUpdated = database.update(
							TableAndColumnNames.OutlineItem.TableName,
							contentValues,
							whereClause,
							new String[] { String.valueOf(id) });
	
					if (rowsUpdated != 1) {
						final String errorMessage = String.format("VaultDocument.changePassword, could not update outline item %d", id);
						Log.e(StringLiterals.LogTag, errorMessage);

						throw new VaultException(errorMessage);
					}

					reportProgress.reportProgress(
							(++itemsProcessed / (float) totalItems) * 100.0f);
				}

				isEncrypted = (action == ChangePasswordAction.addPassword ||
						action == ChangePasswordAction.changePassword);
				setVaultDocumentInfo(ENCRYPTED, isEncrypted ? "1" : "0");
				
				this.password = newPassword;

				updateVaultDocumentInfoEncryptionData();

				database.setTransactionSuccessful();
			}
			finally {
				database.endTransaction();
			}
		}
		
		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.changePassword: %d ms",
				elapsedMilliseconds));
	}
	
	private String getRandomPlainText() {
		final SecureRandom secureRandom = new SecureRandom();

		final byte[] randomBytes = new byte[100];
		secureRandom.nextBytes(randomBytes);
		
		return new String(Base64Coder.encode(randomBytes));
	}

	public void upgradeVaultDocument(String password, IReportProgress reportProgress)
			throws Throwable {
		final long startTime = System.currentTimeMillis();

		database.beginTransaction();
		
		try {
			upgradeSchema();

			if (isEncrypted) {
				upgradeEncryption(password, reportProgress);
			} else {
				reportProgress.reportProgress(100.0f);
			}

			setDocumentVersion(VaultDocumentVersion.VERSION_1_3);
			setVaultDocumentInfo(DOCUMENT_VERSION,
					VaultDocumentVersion.getLatestVaultDocumentVersion().toString());

			updateVaultDocumentInfoEncryptionData();

			database.setTransactionSuccessful();
		}
		finally {
			database.endTransaction();
		}

		Globals.getApplication().getPasswordCache().put(database.getPath(), password);

		final long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.upgradeVaultDocument: %d ms",
				elapsedMilliseconds));
	}

	private void upgradeSchema() {
		/*
		 * Replace FontString column in OutlineItem table with FontList column.
		 *
		 * SQLite has limited ALTER TABLE support that you can use to add a column to the end of a table or to change the name of a table.
		 * If you want to make more complex changes in the structure of a table, you will have to recreate the table. You can save
		 * existing data to a temporary table, drop the old table, create the new table, then copy the data back in from the temporary table.
		 */

		if (getDocumentVersion().compareTo(VaultDocumentVersion.VERSION_1_2) < 0) {
			String command =
					"CREATE TABLE TempOutlineItem(" +
							"ID INTEGER PRIMARY KEY, ParentID INTEGER, Title TEXT, Text TEXT, " +
							"Red INTEGER DEFAULT 0, Green INTEGER DEFAULT 0, Blue INTEGER DEFAULT 0," +
							"PhotoPath TEXT, AllowScaling INTEGER DEFAULT 1, SortOrder INTEGER" +
							")";
			database.execSQL(command);

			command = "INSERT INTO TempOutlineItem SELECT ID, ParentID, Title, Text, Red, Green, Blue, PhotoPath, AllowScaling, SortOrder FROM OutlineItem;";
			database.execSQL(command);

			command = "DROP TABLE OutlineItem;";
			database.execSQL(command);

			command =
					"CREATE TABLE OutlineItem(" +
							"ID INTEGER PRIMARY KEY, ParentID INTEGER, Title TEXT, Text TEXT, FontList TEXT," +
							"Red INTEGER DEFAULT 0, Green INTEGER DEFAULT 0, Blue INTEGER DEFAULT 0," +
							"PhotoPath TEXT, AllowScaling INTEGER DEFAULT 1, SortOrder INTEGER" +
							")";
			database.execSQL(command);

			command = "INSERT INTO OutlineItem SELECT ID, ParentID, Title, Text, NULL, Red, Green, Blue, PhotoPath, AllowScaling, SortOrder FROM TempOutlineItem;";
			database.execSQL(command);

			command = "CREATE INDEX ParentIDIndex ON OutlineItem(ParentID)";
			database.execSQL(command);

			command = "DROP TABLE TempOutlineItem;";
			database.execSQL(command);
		}

		if (getDocumentVersion().compareTo(VaultDocumentVersion.VERSION_1_3) < 0) {
			// Add version 1.3 cryptography columns.
			String[] newColumns = new String[]{"TitleSalt", "TitleIV", "TextSalt", "TextIV"};

			// https://www.sqlite.org/lang_altertable.html
			Arrays.stream(newColumns)
					.forEach(columnName -> {
						final String addColumnCommand =
								String.format("ALTER TABLE OutlineItem ADD COLUMN %s TEXT",
										columnName);

						database.execSQL(addColumnCommand);
					});
		}
	}

	private void upgradeEncryption(String password, IReportProgress reportProgress)
			throws InvalidAlgorithmParameterException, NoSuchPaddingException,
			UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, VaultException {
		String[] columns = new String[] {
				/* 0 */ TableAndColumnNames.OutlineItem.ID,
				/* 1 */ TableAndColumnNames.OutlineItem.Title,
				/* 2 */ TableAndColumnNames.OutlineItem.Text
		};

		try (final Cursor cursor = database.query(
				TableAndColumnNames.OutlineItem.TableName,
				columns,
				null, null, null, null, null)) {

			final int totalItems = getOutlineItemCount();
			int itemsProcessed = 0;

			while (cursor.moveToNext()) {
				final int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String text = cursor.getString(2);

				// decrypt title.
				{
					final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
							password, getDocumentVersion(), null, null);

					title = CryptoUtils.decryptString(decryptionCipher, title);
				}

				// decrypt text
				{
					final Cipher decryptionCipher = CryptoUtils.createDecryptionCipher(
							password, getDocumentVersion(), null, null);

					text = CryptoUtils.decryptString(decryptionCipher, text);
				}

				String titleSalt, titleIV;

				// encrypt title
				{
					final byte[] salt = CryptoUtils.createSalt();
					titleSalt = new String(Base64Coder.encode(salt));

					final byte[] iv = CryptoUtils.createIV();
					titleIV = new String(Base64Coder.encode(iv));

					final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
							password, VaultDocumentVersion.getLatestVaultDocumentVersion(),
							salt, iv);

					title = CryptoUtils.encryptString(encryptionCipher, title);
				}

				String textSalt, textIV;

				// encrypt text
				{
					final byte[] salt = CryptoUtils.createSalt();
					textSalt = new String(Base64Coder.encode(salt));

					final byte[] iv = CryptoUtils.createIV();
					textIV = new String(Base64Coder.encode(iv));

					final Cipher encryptionCipher = CryptoUtils.createEncryptionCipher(
							password, VaultDocumentVersion.getLatestVaultDocumentVersion(),
							salt, iv);

					text = CryptoUtils.encryptString(encryptionCipher, text);
				}

				// Update text, title, and associated data
				ContentValues contentValues = new ContentValues();
				contentValues.put(TableAndColumnNames.OutlineItem.Title, title);
				contentValues.put(TableAndColumnNames.OutlineItem.TitleSalt, titleSalt);
				contentValues.put(TableAndColumnNames.OutlineItem.TitleIV, titleIV);
				contentValues.put(TableAndColumnNames.OutlineItem.Text, text);
				contentValues.put(TableAndColumnNames.OutlineItem.TextSalt, textSalt);
				contentValues.put(TableAndColumnNames.OutlineItem.TextIV, textIV);

				final String whereClause = String.format("%s = ?",
						TableAndColumnNames.OutlineItem.ID);

				final int rowsUpdated = database.update(
						TableAndColumnNames.OutlineItem.TableName,
						contentValues,
						whereClause,
						new String[]{String.valueOf(id)});

				if (rowsUpdated != 1) {
					final String errorMessage = String.format(
							"VaultDocument.changePassword, could not update outline item %d", id);
					Log.e(StringLiterals.LogTag, errorMessage);

					throw new VaultException(errorMessage);
				}

				reportProgress.reportProgress(
						(++itemsProcessed / (float) totalItems) * 100.0f);
			}
		}

		this.password = password;

		setVaultDocumentInfo(ENCRYPTED, "1");
	}

	public void updateFont(OutlineItem outlineItem, AndroidFont font, int color) throws Throwable {
		long startTime = System.currentTimeMillis();
		
		String oldFontListText = StringLiterals.EmptyString;
		
		if (outlineItem.getFontList() != null) {
			oldFontListText = FontList.serialize(outlineItem.getFontList());
		}
		else {
			outlineItem.setFontList(new FontList());
		}
		
		outlineItem.getFontList().add(font);
		
		final String newFontListText = FontList.serialize(outlineItem.getFontList());
		
		final RGBColor rgbColor =
				new RGBColor(Color.red(color), Color.green(color), Color.blue(color));

		final boolean colorChanged =
				outlineItem.getColor() == null || !outlineItem.getColor().equals(rgbColor);
				
		// Only update the database if the font list really changed.
		if (!oldFontListText.equals(newFontListText) || colorChanged) {
			database.beginTransaction();
			
			try {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TableAndColumnNames.OutlineItem.FontList, newFontListText);
				contentValues.put(TableAndColumnNames.OutlineItem.Red, rgbColor.getRed());
				contentValues.put(TableAndColumnNames.OutlineItem.Green, rgbColor.getGreen());
				contentValues.put(TableAndColumnNames.OutlineItem.Blue, rgbColor.getBlue());
	
				final int rowsUpdated = database.update(TableAndColumnNames.OutlineItem.TableName,
												  contentValues, 
												  String.format("%s = ?", TableAndColumnNames.OutlineItem.ID), 
												  new String[] { String.valueOf(outlineItem.getId()) });
				
				if (rowsUpdated != 1) {
					throw new VaultException(String.format("VaultDocument.updateFont: cannot update FontList for %d", outlineItem.getId()));
				}
				
				if (outlineItem.getFontList() == null)
				{
					outlineItem.setFontList(new FontList());
				}
				
				outlineItem.getFontList().add(font);
				outlineItem.setColor(rgbColor);
				
				database.setTransactionSuccessful();
			}
			finally {
				database.endTransaction();
			}
		}
		else {
			Log.w(StringLiterals.LogTag, String.format("VaultDocument.updateFont: font list did not change for %d", outlineItem.getId()));
		}
		
		long elapsedMilliseconds = System.currentTimeMillis() - startTime;
		Log.i(StringLiterals.LogTag, String.format("VaultDocument.updateFont: %d ms", elapsedMilliseconds));
	}
	
	public static void closeDocument() {
		final VaultDocument vaultDocument = Globals.getApplication().getVaultDocument();

		if (vaultDocument != null) {
			vaultDocument.close();
			Globals.getApplication().setVaultDocument(null);
		}
	}

	public static boolean closeCurrentDocumentWhenDeletedOrRenamed(String filePath) {
		boolean closed = false;
		
		if (Globals.getApplication().getVaultDocument() != null && Globals.getApplication().getVaultDocument().getDatabase().getPath().equals(filePath)) {
			closeDocument();
			
			closed = true;
		}
		
		return closed;
	}
	
	/**
	 * Throw an exception if the database version is higher than the code can cope with.
	 * @param dbVaultDocumentVersion vault document version in database
	 * @throws Exception
	 */
	public static void verifyVaultDocumentVersion(VaultDocumentVersion dbVaultDocumentVersion) throws Exception {
		final VaultDocumentVersion codeVaultDocumentVersion = VaultDocumentVersion.getLatestVaultDocumentVersion();
		
		if (dbVaultDocumentVersion.compareTo(codeVaultDocumentVersion) > 0) {
			throw new VaultException("Database version is too high", VaultException.ExceptionCode.DatabaseVersionTooHigh);
		}
	}
	
	public VaultDocument(SQLiteDatabase database) {
		this.database = database;
		
		isEncrypted = databaseIsEncrypted();
		
		final String documentVersionString = getVaultDocumentInfo(DOCUMENT_VERSION);
		documentVersion = new VaultDocumentVersion(documentVersionString);
	}
}
