package ru.android.ainege.shoppinglist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.android.ainege.shoppinglist.db.tables.CurrencyTable;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class ShoppingListSQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "shoppingList.db";
	private static final int DATABASE_VERSION = 2;
	private static ShoppingListSQLiteHelper instance;
	private final Context mCtx;

	private ShoppingListSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mCtx = context;
	}

	public static ShoppingListSQLiteHelper getInstance(Context context) {
		if (instance == null) {
			instance = new ShoppingListSQLiteHelper(context.getApplicationContext());
		}

		return instance;
	}

	public static String[][] parseInitData(String[] data) {
		String[][] response = new String[data.length][];
		for (int i = 0; i < data.length; i++) {
			response[i] = data[i].split("—");
		}

		return response;
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys = ON;");
		}
	}

	@Override
	public synchronized void close() {
		super.close();

		instance = null;
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		CurrencyTable.onCreate(sqLiteDatabase, mCtx);
		UnitsTable.onCreate(sqLiteDatabase, mCtx);
		ItemsTable.onCreate(sqLiteDatabase, mCtx);
		ListsTable.onCreate(sqLiteDatabase);
		ShoppingListTable.onCreate(sqLiteDatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(oldVersion) {
			case 1:
				UnitsTable.onUpgrade(db, mCtx, oldVersion, newVersion);
				ItemsTable.onUpgrade(db, mCtx, oldVersion, newVersion);

		}
	}

}
