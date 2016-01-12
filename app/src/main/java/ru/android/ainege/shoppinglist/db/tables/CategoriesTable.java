package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDataSource;
import ru.android.ainege.shoppinglist.db.entities.Category;

public class CategoriesTable {

	public static final String TABLE_NAME = "Categories";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "category_name";

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL"
			+ ");";

	public static void onCreate(SQLiteDatabase db, Context ctx) {
		db.execSQL(TABLE_CREATE);

		initialData(db, ctx.getResources().getStringArray(R.array.categories));
	}

	public static HashMap<String, Category> getCategories (SQLiteDatabase db){
		ArrayList<Category> categoriesDB = CategoriesDataSource.getAll(db).getEntities();
		HashMap<String, Category> unit = new HashMap<>();

		for (Category c : categoriesDB) {
			unit.put(c.getName(), c);
		}

		return unit;
	}

	private static void initialData(SQLiteDatabase db, String[] initData) {
		for (String category : initData) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(COLUMN_NAME, category);
			db.insert(TABLE_NAME, null, contentValue);
		}
	}

}
