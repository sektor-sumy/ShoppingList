package ru.android.ainege.shoppinglist.db.migration;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class UpgradeDB {
	protected static final String TMP_TABLE_SUFFIX = "_tmp";

	protected SQLiteDatabase mDb;
	protected Context mCtx;

	public abstract void run();

	public UpgradeDB(SQLiteDatabase db, Context ctx) {
		mDb = db;
		mCtx = ctx;
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
