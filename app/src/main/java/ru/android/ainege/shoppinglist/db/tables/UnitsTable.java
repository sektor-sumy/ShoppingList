package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Unit;

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

	public static void onUpgrade(SQLiteDatabase db, Context ctx, int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 1:
				HashMap<String, Unit> dictionary = getUnit(db);

				String[] units = ctx.getResources().getStringArray(R.array.units);
				for (String unit : units) {
					if (!dictionary.containsKey(unit.toLowerCase())) {
						add(db, unit);
					}
				}
		}
	}

	public static HashMap<String, Unit> getUnit (SQLiteDatabase db){
		ArrayList<Unit> unitsDB = UnitsDS.getAll(db).getEntities();
		HashMap<String, Unit> unit = new HashMap<>();

		for (Unit u : unitsDB) {
			unit.put(u.getName(), u);
		}

		return unit;
	}

	private static void initialData(SQLiteDatabase database, String[] units) {
		for (String unit : units) {
			add(database, unit);
		}
	}

	private static void add(SQLiteDatabase database, String unit) {
		ContentValues contentValue = new ContentValues();
		contentValue.put(COLUMN_NAME, unit);
		database.insert(TABLE_NAME, null, contentValue);
	}
}
