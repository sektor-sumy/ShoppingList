package ru.android.ainege.shoppinglist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.android.ainege.shoppinglist.db.migration.Create;
import ru.android.ainege.shoppinglist.db.migration.Migration2;
import ru.android.ainege.shoppinglist.db.migration.Migration3;

public class ShoppingListSQLiteHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "shoppingList.db";
	private static final int DATABASE_VERSION = 3;
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
		new Create(sqLiteDatabase, mCtx).run();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 1:
				new Migration2(db, mCtx).run();
			case 2:
				new Migration3(db, mCtx).run();
		}
	}
}
