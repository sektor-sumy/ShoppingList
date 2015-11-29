package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class ListsTable {

	public static final String TABLE_NAME = "Lists";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "list_name";
	public static final String COLUMN_ID_CURRENCY = "id_currency";

	public static final String AMOUNT_BOUGHT_ITEMS = "amount_bought";
	public static final String AMOUNT_ITEMS = "amount_items";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL, "
			+ COLUMN_ID_CURRENCY + " INTEGER, "
			+ "FOREIGN KEY (" + COLUMN_ID_CURRENCY + ") REFERENCES " + CurrencyTable.TABLE_NAME + " (" + CurrencyTable.COLUMN_ID + ") ON DELETE SET NULL"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(TABLE_CREATE);
		initialData(database);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

	private static void initialData(SQLiteDatabase database) {
		String[] lists = {"Мой список", "Купить на ДР"};
		int[] currency = {1, 3};

		for (int i = 0; i < lists.length; i++) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(COLUMN_NAME, lists[i]);
			contentValue.put(COLUMN_ID_CURRENCY, currency[i]);
			database.insert(TABLE_NAME, null, contentValue);
		}
	}
}
