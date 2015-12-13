package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.ui.Image;

public class ItemsTable {

	public static final String TABLE_NAME = "Items";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "item_name";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_ID_UNIT = "id_unit";
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_COMMENT = "comment";
	public static final String COLUMN_DEFAULT_IMAGE_PATH = "item_default_image_path";
	public static final String COLUMN_IMAGE_PATH = "image_image_path";

	private static final int INIT_DATA_NAME = 0;
	private static final int INIT_DATA_UNIT = 1;
	private static final int INIT_DATA_IMAGE = 2;

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL, "
			+ COLUMN_AMOUNT + " REAL, "
			+ COLUMN_ID_UNIT + " INTEGER, "
			+ COLUMN_PRICE + " REAL, "
			+ COLUMN_COMMENT + " TEXT, "
			+ COLUMN_DEFAULT_IMAGE_PATH + " TEXT, "
			+ COLUMN_IMAGE_PATH + " TEXT, "
			+ "FOREIGN KEY (" + COLUMN_ID_UNIT + ") REFERENCES " + UnitsTable.TABLE_NAME + " (" + UnitsTable.COLUMN_ID + ") ON DELETE SET NULL"
			+ ");";

	public static void onCreate(SQLiteDatabase database, Context ctx) {
		database.execSQL(TABLE_CREATE);
		String[] units = ctx.getResources().getStringArray(R.array.units);
		String[][] initData = ShoppingListSQLiteHelper.parseInitData(ctx.getResources().getStringArray(R.array.items));

		initialData(database, initData, java.util.Arrays.asList(units));
	}


	private static void initialData(SQLiteDatabase database, String[][] initData, List<String> units) {
		for (String[] itemData : initData) {
			ContentValues contentValue = new ContentValues();

			contentValue.put(COLUMN_NAME, itemData[INIT_DATA_NAME]);
			contentValue.put(COLUMN_DEFAULT_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE]);
			contentValue.put(COLUMN_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE]);
			contentValue.put(COLUMN_ID_UNIT, units.indexOf(itemData[INIT_DATA_UNIT]) + 1);

			database.insert(TABLE_NAME, null, contentValue);
		}
	}
}
