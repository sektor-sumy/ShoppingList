package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.R;

public class UnitsTable {

	public static final String TABLE_NAME = "Units";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "unit_name";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL"
			+ ");";

	public static void onCreate(SQLiteDatabase database, Context ctx) {
		database.execSQL(TABLE_CREATE);
		initialData(database, ctx.getResources().getStringArray(R.array.units));
	}

	private static void initialData(SQLiteDatabase database, String[] units) {
		for (String unit : units) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(COLUMN_NAME, unit);
			database.insert(TABLE_NAME, null, contentValue);
		}
	}
}
