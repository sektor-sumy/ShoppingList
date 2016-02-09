package ru.android.ainege.shoppinglist.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.android.ainege.shoppinglist.db.tables.CategoriesTable;
import ru.android.ainege.shoppinglist.db.tables.CurrenciesTable;
import ru.android.ainege.shoppinglist.db.tables.ItemDataTable;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.util.Showcase;

public class ShoppingListSQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "shoppingList.db";
	private static final int DATABASE_VERSION = 2;
	private static ShoppingListSQLiteHelper instance;
	private final Context mCtx;

	private ShoppingListSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mCtx = context;
	}

	public static ShoppingListSQLiteHelper getInstance(Context context) {
		if (instance == null) {
			instance = new ShoppingListSQLiteHelper(context.getApplicationContext());
		}

		return instance;
	}

	public static String[][] parseInitData(String[] data) {
		String[][] response = new String[data.length][];
		for (int i = 0; i < data.length; i++) {
			response[i] = data[i].split("—");
		}

		return response;
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys = ON;");
		}
	}

	@Override
	public synchronized void close() {
		super.close();

		instance = null;
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		CurrenciesTable.onCreate(sqLiteDatabase, mCtx);
		UnitsTable.onCreate(sqLiteDatabase, mCtx);
		CategoriesTable.onCreate(sqLiteDatabase, mCtx);
		ItemDataTable.onCreate(sqLiteDatabase);
		ItemsTable.onCreate(sqLiteDatabase, mCtx);
		ListsTable.onCreate(sqLiteDatabase);
		ShoppingListTable.onCreate(sqLiteDatabase);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 1:
				SharedPreferences sp = mCtx.getSharedPreferences(Showcase.PREFS_SHOWCASE_INTERNAL, Context.MODE_PRIVATE);

				SharedPreferences.Editor e = sp.edit();
				e.putBoolean("hasShot" + Showcase.SHOT_LIST, true);
				e.putBoolean("hasShot" + Showcase.SHOT_ADD_ITEM, true);
				e.putBoolean("hasShot" + Showcase.SHOT_ITEM_IN_LIST, true);
				e.putBoolean("hasShot" + Showcase.SHOT_ITEM, true);
				e.putBoolean("hasShot" + Showcase.SHOT_CURRENCY, true);
				e.apply();

				UnitsTable.onUpgrade(db, mCtx, oldVersion, newVersion);
				ItemsTable.onUpgrade(db, mCtx, oldVersion, newVersion);

		}
	}
}
