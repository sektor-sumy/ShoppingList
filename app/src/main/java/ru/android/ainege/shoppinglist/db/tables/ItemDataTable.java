package ru.android.ainege.shoppinglist.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class ItemDataTable {
	public static final String TABLE_NAME = "ItemData";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_ID_UNIT = "id_unit";
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_ID_CATEGORY = "id_category";
	public static final String COLUMN_COMMENT = "comment";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_AMOUNT + " REAL, "
			+ COLUMN_ID_UNIT + " INTEGER NOT NULL, "
			+ COLUMN_PRICE + " REAL, "
			+ COLUMN_ID_CATEGORY + " INTEGER NOT NULL, "
			+ COLUMN_COMMENT + " TEXT, "
			+ "FOREIGN KEY (" + COLUMN_ID_UNIT + ") REFERENCES " + UnitsTable.TABLE_NAME + " (" + UnitsTable.COLUMN_ID + "), "
			+ "FOREIGN KEY (" + COLUMN_ID_CATEGORY + ") REFERENCES " + CategoriesTable.TABLE_NAME + " (" + CategoriesTable.COLUMN_ID + ")"
			+ ");";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}
}
