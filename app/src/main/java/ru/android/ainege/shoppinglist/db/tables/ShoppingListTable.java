package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class ShoppingListTable {
	public static final String TABLE_NAME = "ShoppingList";

	public static final String COLUMN_ID_ITEM = "id_item";
	public static final String COLUMN_ID_LIST = "id_list";
	public static final String COLUMN_IS_BOUGHT = "is_bought";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_ID_UNIT = "id_unit";
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_COMMENT = "comment";
	public static final String COLUMN_DATE = "date";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID_ITEM + " INTEGER NOT NULL, "
			+ COLUMN_ID_LIST + " INTEGER NOT NULL, "
			+ COLUMN_IS_BOUGHT + " INTEGER NOT NULL, "
			+ COLUMN_AMOUNT + " REAL, "
			+ COLUMN_ID_UNIT + " INTEGER, "
			+ COLUMN_PRICE + " REAL, "
			+ COLUMN_COMMENT + " TEXT, "
			+ COLUMN_DATE + " INTEGER, "
			+ "FOREIGN KEY (" + COLUMN_ID_UNIT + ") REFERENCES " + UnitsTable.TABLE_NAME + " (" + UnitsTable.COLUMN_ID + ") ON DELETE SET NULL, "
			+ "FOREIGN KEY (" + COLUMN_ID_ITEM + ") REFERENCES " + ItemsTable.TABLE_NAME + " (" + ItemsTable.COLUMN_ID + ") ON DELETE CASCADE, "
			+ "FOREIGN KEY (" + COLUMN_ID_LIST + ") REFERENCES " + ListsTable.TABLE_NAME + " (" + ListsTable.COLUMN_ID + ") ON DELETE CASCADE, "
			+ "PRIMARY KEY (" + COLUMN_ID_ITEM + ", " + COLUMN_ID_LIST + ")"
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
		int list = 1;
		int[] item = {1, 2, 3, 4, 5, 6};
		boolean[] isBought = {false, false, true, false, false, true};
		double[] amount = {0, 1, 2, 0, 1, 1};
		int[] amountUnit = {0, 1, 1, 0, 2, 2};
		double[] price = {55.00, 19.5, 66, 87, 70, 105};
		String[] comment = {
				"",
				"",
				"проверь дату изготовления",
				"в гранулах",
				"круглый",
				"",
		};

		for (int i = 0; i < item.length; i++) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(COLUMN_ID_ITEM, item[i]);
			contentValue.put(COLUMN_ID_LIST, list);
			contentValue.put(COLUMN_IS_BOUGHT, isBought[i]);
			contentValue.put(COLUMN_AMOUNT, amount[i]);
			contentValue.put(COLUMN_ID_UNIT, amountUnit[i]);
			contentValue.put(COLUMN_PRICE, price[i]);
			contentValue.put(COLUMN_COMMENT, comment[i]);
			contentValue.put(COLUMN_DATE, i);
			database.insert(TABLE_NAME, null, contentValue);
		}
	}
}
