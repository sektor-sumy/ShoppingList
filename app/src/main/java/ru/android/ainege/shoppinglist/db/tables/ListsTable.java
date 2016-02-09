package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.util.Image;

public class ListsTable {
	public static final String TABLE_NAME = "Lists";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "list_name";
	public static final String COLUMN_ID_CURRENCY = "id_currency";
	public static final String COLUMN_IMAGE_PATH = "list_image_path";

	public static final String AMOUNT_BOUGHT_ITEMS = "amount_bought";
	public static final String AMOUNT_ITEMS = "amount_items";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL, "
			+ COLUMN_ID_CURRENCY + " INTEGER NOT NULL, "
			+ COLUMN_IMAGE_PATH + " TEXT NOT NULL, "
			+ "FOREIGN KEY (" + COLUMN_ID_CURRENCY + ") REFERENCES " + CurrenciesTable.TABLE_NAME + " (" + CurrenciesTable.COLUMN_ID + ")"
			+ ");";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
		initialData(db);
	}

	private static void initialData(SQLiteDatabase db) {
		ContentValues contentValue = new ContentValues();

		contentValue.put(COLUMN_NAME, "Ваш список");
		contentValue.put(COLUMN_ID_CURRENCY, 1);
		contentValue.put(COLUMN_IMAGE_PATH, Image.LIST_IMAGE_PATH + "random_list_0.png");

		db.insert(TABLE_NAME, null, contentValue);
	}
}
