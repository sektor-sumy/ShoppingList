package ru.android.ainege.shoppinglist.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class ShoppingListTable {
	public static final String TABLE_NAME = "ShoppingList";

	public static final String COLUMN_ID_ITEM = "id_item";
	public static final String COLUMN_ID_LIST = "id_list";
	public static final String COLUMN_ID_DATA = "id_data";
	public static final String COLUMN_IS_BOUGHT = "is_bought";
	public static final String COLUMN_DATE = "date";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID_ITEM + " INTEGER NOT NULL, "
			+ COLUMN_ID_LIST + " INTEGER NOT NULL, "
			+ COLUMN_ID_DATA + " INTEGER NOT NULL, "
			+ COLUMN_IS_BOUGHT + " INTEGER NOT NULL, "
			+ COLUMN_DATE + " INTEGER NOT NULL, "
			+ "FOREIGN KEY (" + COLUMN_ID_DATA + ") REFERENCES " + ItemDataTable.TABLE_NAME + " (" + ItemDataTable.COLUMN_ID + ") ON DELETE CASCADE, "
			+ "FOREIGN KEY (" + COLUMN_ID_ITEM + ") REFERENCES " + ItemsTable.TABLE_NAME + " (" + ItemsTable.COLUMN_ID + ") ON DELETE CASCADE, "
			+ "FOREIGN KEY (" + COLUMN_ID_LIST + ") REFERENCES " + ListsTable.TABLE_NAME + " (" + ListsTable.COLUMN_ID + ") ON DELETE CASCADE, "
			+ "PRIMARY KEY (" + COLUMN_ID_ITEM + ", " + COLUMN_ID_LIST + ")"
			+ ");";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}
}
