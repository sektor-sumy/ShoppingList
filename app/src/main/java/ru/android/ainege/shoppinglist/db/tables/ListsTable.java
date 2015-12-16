package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.ui.Image;

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
			+ COLUMN_ID_CURRENCY + " INTEGER, "
			+ COLUMN_IMAGE_PATH + " TEXT, "
			+ "FOREIGN KEY (" + COLUMN_ID_CURRENCY + ") REFERENCES " + CurrencyTable.TABLE_NAME + " (" + CurrencyTable.COLUMN_ID + ") ON DELETE SET NULL"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(TABLE_CREATE);
		initialData(database);
	}

	private static void initialData(SQLiteDatabase database) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(COLUMN_NAME, "Ваш список");
			contentValue.put(COLUMN_ID_CURRENCY, 1);
			contentValue.put(COLUMN_IMAGE_PATH, Image.LIST_IMAGE_PATH + "random_list_0.png");
			database.insert(TABLE_NAME, null, contentValue);
	}
}
