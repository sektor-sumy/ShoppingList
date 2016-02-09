package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;

public class CurrenciesTable {
	public static final String TABLE_NAME = "Currencies";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "currency_name";
	public static final String COLUMN_SYMBOL = "symbol";

	private static final int INIT_DATA_NAME = 0;
	private static final int INIT_DATA_SYMBOL = 1;

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL,"
			+ COLUMN_SYMBOL + " TEXT NOT NULL"
			+ ");";

	public static void onCreate(SQLiteDatabase db, Context ctx) {
		db.execSQL(TABLE_CREATE);

		initialData(db, ShoppingListSQLiteHelper.parseInitData(ctx.getResources().getStringArray(R.array.currency)));
	}

	private static void initialData(SQLiteDatabase db, String[][] initData) {
		for (String[] currencyData : initData) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(COLUMN_NAME, currencyData[INIT_DATA_NAME]);
			contentValue.put(COLUMN_SYMBOL, currencyData[INIT_DATA_SYMBOL]);
			db.insert(TABLE_NAME, null, contentValue);
		}
	}
}
