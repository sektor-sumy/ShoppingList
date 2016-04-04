package ru.android.ainege.shoppinglist.db.migration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.util.Image;

public class Migration2 {
	private SQLiteDatabase mDb;
	private Context mCtx;

	public Migration2(SQLiteDatabase db, Context ctx) {
		mDb = db;
		mCtx = ctx;
	}

	public void run() {
		upgradeUnit();
		upgradeItem();
	}

	private void upgradeUnit() {
		HashMap<String, Unit> dictionary = UnitsDS.getUnit(mDb);
		String[] units = mCtx.getResources().getStringArray(R.array.units);

		for (String unit : units) {
			if (!dictionary.containsKey(unit.toLowerCase())) {
				ContentValues contentValue = new ContentValues();
				contentValue.put(UnitT.COLUMN_NAME, unit);
				mDb.insert(UnitT.TABLE_NAME, null, contentValue);
			}
		}
	}

	private void upgradeItem() {
		HashMap<String, Unit> unitHM = UnitsDS.getUnit(mDb);

		Cursor itemDB = getAll(mDb);
		HashMap<String, Integer> itemHM = new HashMap<>();

		if (itemDB.moveToFirst()) {
			do {
				itemHM.put(itemDB.getString(itemDB.getColumnIndex(Migration2.ItemT.COLUMN_NAME)).toLowerCase(), itemDB.getPosition());
			} while (itemDB.moveToNext());

			String[][] initData = ShoppingListSQLiteHelper.parseInitData(mCtx.getResources().getStringArray(R.array.items));

			for (String[] itemData : initData) {
				String name = itemData[ItemT.INIT_DATA_NAME].toLowerCase();
				String image = Image.ITEM_IMAGE_PATH + itemData[ItemT.INIT_DATA_IMAGE];

				if (itemHM.containsKey(name)) {
					itemDB.moveToPosition(itemHM.get(name));

					if (!itemDB.getString(itemDB.getColumnIndex(ItemT.COLUMN_DEFAULT_IMAGE_PATH)).equals(image)) {
						ContentValues contentValue = new ContentValues();
						contentValue.put(ItemT.COLUMN_DEFAULT_IMAGE_PATH, image);

						if (itemDB.getString(itemDB.getColumnIndex(ItemT.COLUMN_IMAGE_PATH)).startsWith(Image.CHARACTER_IMAGE_PATH)) {
							contentValue.put(ItemT.COLUMN_IMAGE_PATH, image);
						}

						mDb.update(ItemT.TABLE_NAME, contentValue, ItemT.COLUMN_ID + " = ?",
								new String[]{String.valueOf(itemDB.getLong(itemDB.getColumnIndex(ItemT.COLUMN_ID)))});
					}
				} else {
					ContentValues contentValue = new ContentValues();

					contentValue.put(ItemT.COLUMN_NAME, itemData[ItemT.INIT_DATA_NAME]);
					contentValue.put(ItemT.COLUMN_DEFAULT_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[ItemT.INIT_DATA_IMAGE]);
					contentValue.put(ItemT.COLUMN_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[ItemT.INIT_DATA_IMAGE]);
					contentValue.put(ItemT.COLUMN_ID_UNIT, unitHM.get(itemData[ItemT.INIT_DATA_UNIT]).getId());

					mDb.insert(ItemT.TABLE_NAME, null, contentValue);
				}
			}
		}
	}

	private Cursor getAll(SQLiteDatabase db) {
		return new ItemDS.ItemCursor(db.query(ItemT.TABLE_NAME, null, null,
				null, null, null, null));
	}


	class UnitT {
		static final String TABLE_NAME = "Units";

		static final String COLUMN_ID = "_id";
		static final String COLUMN_NAME = "unit_name";
	}

	class CurrencyT {
		static final String TABLE_NAME = "Currencies";

		static final String COLUMN_ID = "_id";
		static final String COLUMN_NAME = "currency_name";
		static final String COLUMN_SYMBOL = "symbol";
	}

	class ListT {
		static final String TABLE_NAME = "Lists";

		static final String COLUMN_ID = "_id";
		static final String COLUMN_NAME = "list_name";
		static final String COLUMN_ID_CURRENCY = "id_currency";
		static final String COLUMN_IMAGE_PATH = "list_image_path";
	}

	class ItemT {
		static final String TABLE_NAME = "Items";

		static final String COLUMN_ID = "_id";
		static final String COLUMN_NAME = "item_name";
		static final String COLUMN_AMOUNT = "amount";
		static final String COLUMN_ID_UNIT = "id_unit";
		static final String COLUMN_PRICE = "price";
		static final String COLUMN_COMMENT = "comment";
		static final String COLUMN_DEFAULT_IMAGE_PATH = "item_default_image_path";
		static final String COLUMN_IMAGE_PATH = "image_image_path";

		static final int INIT_DATA_NAME = 0;
		static final int INIT_DATA_UNIT = 1;
		static final int INIT_DATA_IMAGE = 2;
	}

	class ShoppingListT {
		static final String TABLE_NAME = "ShoppingList";

		static final String COLUMN_ID_ITEM = "id_item";
		static final String COLUMN_ID_LIST = "id_list";
		static final String COLUMN_IS_BOUGHT = "is_bought";
		static final String COLUMN_AMOUNT = "amount";
		static final String COLUMN_ID_UNIT = "id_unit";
		static final String COLUMN_PRICE = "price";
		static final String COLUMN_COMMENT = "comment";
		static final String COLUMN_DATE = "date";
	}
}
