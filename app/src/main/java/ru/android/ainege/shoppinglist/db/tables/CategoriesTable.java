package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.entities.Category;

public class CategoriesTable {
	public static final String TABLE_NAME = "Categories";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "category_name";
	public static final String COLUMN_COLOR = "color";

	private static final int INIT_DATA_NAME = 0;
	private static final int INIT_DATA_COLOR = 1;

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL, "
			+ COLUMN_COLOR + " INTEGER NOT NULL"
			+ ");";

	public static void onCreate(SQLiteDatabase db, Context ctx) {
		db.execSQL(TABLE_CREATE);

		initialData(db, ShoppingListSQLiteHelper.parseInitData(ctx.getResources().getStringArray(R.array.categories)));
	}

	public static HashMap<String, Category> getCategories(SQLiteDatabase db) {
		ArrayList<Category> categoriesDB = CategoriesDS.getAll(db).getEntities();
		HashMap<String, Category> unit = new HashMap<>();

		for (Category c : categoriesDB) {
			unit.put(c.getName(), c);
		}

		return unit;
	}

	private static void initialData(SQLiteDatabase db, String[][] initData) {
		for (String[] category : initData) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(COLUMN_NAME, category[INIT_DATA_NAME]);
			contentValue.put(COLUMN_COLOR, category[INIT_DATA_COLOR]);
			db.insert(TABLE_NAME, null, contentValue);
		}
	}
}
