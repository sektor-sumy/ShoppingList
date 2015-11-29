package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class CurrencyTable {

	public static final String TABLE_NAME = "Currencies";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "currency_name";
	public static final String COLUMN_SYMBOL = "symbol";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL,"
			+ COLUMN_SYMBOL + " TEXT"
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
		String[] name = {
				"рубль",
				"гривна",
				"доллар",
				"евро",
		};
		String[] symbol = {
				"\u20BD",
				"\u20B4",
				"\u0024",
				"\u20AC",
		};

		for (int i = 0; i < name.length; i++) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(COLUMN_NAME, name[i]);
			contentValue.put(COLUMN_SYMBOL, symbol[i]);
			database.insert(TABLE_NAME, null, contentValue);
		}
	}

}
