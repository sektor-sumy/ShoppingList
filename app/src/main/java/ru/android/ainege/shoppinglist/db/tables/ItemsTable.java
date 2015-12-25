package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.Unit;
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
			+ COLUMN_ID_UNIT + " INTEGER NOT NULL, "
			+ COLUMN_PRICE + " REAL, "
			+ COLUMN_COMMENT + " TEXT, "
			+ COLUMN_DEFAULT_IMAGE_PATH + " TEXT NOT NULL, "
			+ COLUMN_IMAGE_PATH + " TEXT NOT NULL, "
			+ "FOREIGN KEY (" + COLUMN_ID_UNIT + ") REFERENCES " + UnitsTable.TABLE_NAME + " (" + UnitsTable.COLUMN_ID + ") ON DELETE SET NULL"
			+ ");";

	public static void onCreate(SQLiteDatabase database, Context ctx) {
		database.execSQL(TABLE_CREATE);
		String[][] initData = ShoppingListSQLiteHelper.parseInitData(ctx.getResources().getStringArray(R.array.items));

		initialData(database, initData);
	}

	public static void onUpgrade(SQLiteDatabase db, Context ctx, int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 1:
				HashMap<String, Unit> unitHM = UnitsTable.getUnit(db);

				ArrayList<Item> itemsDB = ItemDataSource.getAll(db).getEntities();
				HashMap<String, Item> itemHM = new HashMap<>();
				for (Item i : itemsDB) {
					itemHM.put(i.getName().toLowerCase(), i);
				}

				String[][] initData = ShoppingListSQLiteHelper.parseInitData(ctx.getResources().getStringArray(R.array.items));
				for (String[] itemData : initData) {
					String name = itemData[INIT_DATA_NAME].toLowerCase();
					String image = Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE];

					if (itemHM.containsKey(name)) {
						Item item = itemHM.get(name);

						if (!item.getDefaultImagePath().equals(image)) {
							ContentValues contentValue = new ContentValues();
							contentValue.put(COLUMN_DEFAULT_IMAGE_PATH, image);

							if (item.getImagePath().startsWith(Image.CHARACTER_IMAGE_PATH)) {
								contentValue.put(COLUMN_IMAGE_PATH, image);
							}

							db.update(ItemsTable.TABLE_NAME, contentValue, ItemsTable.COLUMN_ID + " = ?",
									new String[]{String.valueOf(item.getId())});
						}
					} else {
						add(db, itemData, unitHM.get(itemData[INIT_DATA_UNIT]));
					}
				}
		}
	}


	private static void initialData(SQLiteDatabase db, String[][] initData) {
		HashMap<String, Unit> unit = UnitsTable.getUnit(db);

		for (String[] itemData : initData) {
			add(db, itemData, unit.get(itemData[INIT_DATA_UNIT]));
		}
	}

	private static void add(SQLiteDatabase db, String[] itemData, Unit unit) {
		ContentValues contentValue = new ContentValues();

		contentValue.put(COLUMN_NAME, itemData[INIT_DATA_NAME]);
		contentValue.put(COLUMN_DEFAULT_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE]);
		contentValue.put(COLUMN_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE]);
		contentValue.put(COLUMN_ID_UNIT, unit.getId());

		db.insert(TABLE_NAME, null, contentValue);
	}
}
