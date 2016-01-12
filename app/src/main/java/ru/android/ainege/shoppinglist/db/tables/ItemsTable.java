package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.ui.Image;

public class ItemsTable {

	public static final String TABLE_NAME = "Items";

	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_ID_UNIT = "id_unit";
	public static final String COLUMN_PRICE = "price";
	public static final String COLUMN_COMMENT = "comment";


	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "item_name";
	public static final String COLUMN_IMAGE_PATH = "item_image_path";
	public static final String COLUMN_DEFAULT_IMAGE_PATH = "default_image_path";
	public static final String COLUMN_ID_DATA = "id_data";

	private static final int INIT_DATA_NAME = 0;
	private static final int INIT_DATA_UNIT = 1;
	private static final int INIT_DATA_IMAGE = 2;
	private static final int INIT_DATA_CATEGORY = 3;

	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_NAME + " TEXT NOT NULL, "
			+ COLUMN_DEFAULT_IMAGE_PATH + " TEXT NOT NULL, "
			+ COLUMN_IMAGE_PATH + " TEXT NOT NULL, "
			+ COLUMN_ID_DATA + " INTEGER NOT NULL, "
			+ "FOREIGN KEY (" + COLUMN_ID_DATA + ") REFERENCES " + ItemDataTable.TABLE_NAME + " (" + ItemDataTable.COLUMN_ID + ")"
			+ ");";

	public static void onCreate(SQLiteDatabase db, Context ctx) {
		db.execSQL(TABLE_CREATE);

		String[][] initData = ShoppingListSQLiteHelper.parseInitData(ctx.getResources().getStringArray(R.array.items));
		initialData(db, initData);
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
						ContentValues contentValue = new ContentValues();

						contentValue.put(COLUMN_NAME, itemData[INIT_DATA_NAME]);
						contentValue.put(COLUMN_DEFAULT_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE]);
						contentValue.put(COLUMN_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE]);
						contentValue.put(COLUMN_ID_UNIT, unitHM.get(itemData[INIT_DATA_UNIT]).getId());

						db.insert(TABLE_NAME, null, contentValue);
					}
				}
		}
	}

	private static void initialData(SQLiteDatabase db, String[][] initData) {
		HashMap<String, Unit> unit = UnitsTable.getUnit(db);
		HashMap<String, Category> category = CategoriesTable.getCategories(db);

		for (String[] itemData : initData) {
			long idData = addData(db, unit.get(itemData[INIT_DATA_UNIT]), category.get(itemData[INIT_DATA_CATEGORY]));
			addItem(db, itemData, idData);
		}
	}

	private static long addData(SQLiteDatabase db, Unit unit, Category category) {
		ContentValues data = new ContentValues();

		data.put(ItemDataTable.COLUMN_ID_UNIT, unit.getId());
		data.put(ItemDataTable.COLUMN_ID_CATEGORY, category.getId());

		return db.insert(ItemDataTable.TABLE_NAME, null, data);
	}

	private static void addItem(SQLiteDatabase db, String[] itemData, long idData) {
		ContentValues contentValue = new ContentValues();

		contentValue.put(COLUMN_NAME, itemData[INIT_DATA_NAME]);
		contentValue.put(COLUMN_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE]);
		contentValue.put(COLUMN_DEFAULT_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[INIT_DATA_IMAGE]);
		contentValue.put(COLUMN_ID_DATA, idData);

		db.insert(TABLE_NAME, null, contentValue);
	}
}
