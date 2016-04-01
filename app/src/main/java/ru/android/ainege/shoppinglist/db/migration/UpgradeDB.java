package ru.android.ainege.shoppinglist.db.migration;

import android.database.sqlite.SQLiteDatabase;

public class UpgradeDB {
	protected static final String TMP_TABLE_SUFFIX = "_tmp";
	private SQLiteDatabase mDb;

	protected UpgradeDB(SQLiteDatabase db) {
		mDb = db;
	}

	protected void createTemporaryTable(String query) {
		mDb.execSQL(query);
	}

	protected void fillingTemporaryTable(String query) {
		mDb.execSQL(query);
	}

	protected void fillingTemporaryTable(String tempTableName, String oldTableName) {
		mDb.execSQL("INSERT INTO " + tempTableName + TMP_TABLE_SUFFIX +
				" SELECT * FROM " + oldTableName);
	}

	protected void deleteOldTable(String tableName) {
		mDb.execSQL("DROP TABLE " + tableName);
	}

	protected void createNewTable(String query) {
		mDb.execSQL(query);
	}

	protected void fillingNewTableQuery(String query) {
		mDb.execSQL(query);
	}

	protected void fillingNewTable(String tableName) {
		mDb.execSQL("INSERT INTO " + tableName + " SELECT * FROM " + tableName + TMP_TABLE_SUFFIX);
	}

	protected void deleteTemporaryTable(String tableName) {
		mDb.execSQL("DROP TABLE " + tableName + TMP_TABLE_SUFFIX);
	}
}
