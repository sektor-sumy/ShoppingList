package ru.android.ainege.shoppinglist.db.migration;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.PrefsManager;

public class Migration3 extends UpgradeDB {
	private SQLiteDatabase mDb;
	private Context mCtx;

	public Migration3(SQLiteDatabase db, Context ctx) {
		super(db);
		mDb = db;
		mCtx = ctx;
	}

	public void run() {
		upgradeDB();
		updateShowcase();
	}

	//<editor-fold desc="UpgradeDB">
	private void upgradeDB() {
		createTemporaryTables();
		fillingTemporaryTables();
		deleteOldTables();
		createNewTables();
		fillingNewTables();
		deleteTemporaryTables();
	}

	private void createTemporaryTables() {
		createTemporaryTable(CurrencyT.TEMP_TABLE_CREATE);
		createTemporaryTable(ListT.TEMP_TABLE_CREATE);
		createTemporaryTable(ItemT.TEMP_TABLE_CREATE);
		createTemporaryTable(ShoppingListT.TEMP_TABLE_CREATE);
	}

	private void fillingTemporaryTables() {
		fillingTemporaryTable(CurrencyT.TABLE_NAME, Migration2.CurrencyT.TABLE_NAME);
		fillingTemporaryTable(ListT.TABLE_NAME, Migration2.ListT.TABLE_NAME);
		fillingTemporaryTable(ItemT.TABLE_NAME, Migration2.ItemT.TABLE_NAME);
		fillingTemporaryTable(ShoppingListT.TABLE_NAME, Migration2.ShoppingListT.TABLE_NAME);
	}

	private void deleteOldTables() {
		deleteOldTable(Migration2.CurrencyT.TABLE_NAME);
		deleteOldTable(Migration2.ListT.TABLE_NAME);
		deleteOldTable(Migration2.ItemT.TABLE_NAME);
		deleteOldTable(Migration2.ShoppingListT.TABLE_NAME);
	}

	private void createNewTables() {
		createNewTable(CurrencyT.TABLE_CREATE);
		createNewTable(CategoryT.TABLE_CREATE);
		createNewTable(ListT.TABLE_CREATE);
		createNewTable(ItemDataT.TABLE_CREATE);
		createNewTable(ItemT.TABLE_CREATE);
		createNewTable(ShoppingListT.TABLE_CREATE);
	}

	private void fillingNewTables() {
		fillingNewTable(CurrencyT.TABLE_NAME);
		fillingNewCategoryTable();
		fillingNewTable(ListT.TABLE_NAME);
		fillingNewItemTable();
		fillingNewShoppingListTable();
	}

	private void fillingNewCategoryTable() {
		String[][] initData = ShoppingListSQLiteHelper.parseInitData(mCtx.getResources().getStringArray(R.array.categories));

		for (String[] category : initData) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(CategoryT.COLUMN_NAME, category[CategoryT.INIT_DATA_NAME]);
			contentValue.put(CategoryT.COLUMN_COLOR, category[CategoryT.INIT_DATA_COLOR]);
			mDb.insert(CategoryT.TABLE_NAME, null, contentValue);
		}
	}

	private void fillingNewItemTable() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + ItemT.TABLE_NAME + TMP_TABLE_SUFFIX, null);
		if (cursor.moveToFirst()) {
			HashMap<String, String> item = getItems();

			do {
				String itemName = cursor.getString(cursor.getColumnIndex(Migration2.ItemT.COLUMN_NAME));
				long idData = saveData(cursor, getIdCategory(item, itemName));

				addItem(mDb,
						cursor.getLong(cursor.getColumnIndex(Migration2.ItemT.COLUMN_ID)),
						itemName,
						cursor.getString(cursor.getColumnIndex(Migration2.ItemT.COLUMN_DEFAULT_IMAGE_PATH)),
						cursor.getString(cursor.getColumnIndex(Migration2.ItemT.COLUMN_IMAGE_PATH)),
						idData);

			} while (cursor.moveToNext());
		}

	}

	private void fillingNewShoppingListTable() {
		Cursor cursor = mDb.rawQuery("SELECT * FROM " + ShoppingListT.TABLE_NAME + TMP_TABLE_SUFFIX, null);
		if (cursor.moveToFirst()) {
			HashMap<String, String> item = getItems();

			do {
				String itemName = getItemName(mDb, cursor.getLong(cursor.getColumnIndex(Migration2.ShoppingListT.COLUMN_ID_ITEM)));
				long idData = saveData(cursor, getIdCategory(item, itemName));

				addShoppingList(mDb,
						cursor.getLong(cursor.getColumnIndex(Migration2.ShoppingListT.COLUMN_ID_ITEM)),
						cursor.getLong(cursor.getColumnIndex(Migration2.ShoppingListT.COLUMN_ID_LIST)),
						idData,
						cursor.getInt(cursor.getColumnIndex(Migration2.ShoppingListT.COLUMN_IS_BOUGHT)) == 1,
						cursor.getLong(cursor.getColumnIndex(Migration2.ShoppingListT.COLUMN_DATE)));

			} while (cursor.moveToNext());
		}
	}

	private void deleteTemporaryTables() {
		deleteTemporaryTable(CurrencyT.TABLE_NAME);
		deleteTemporaryTable(ListT.TABLE_NAME);
		deleteTemporaryTable(ItemT.TABLE_NAME);
		deleteTemporaryTable(ShoppingListT.TABLE_NAME);
	}

	private HashMap<String, String> getItems() {
		String[][] initData1 = ShoppingListSQLiteHelper.parseInitData(mCtx.getResources().getStringArray(R.array.items));
		HashMap<String, String> item = new HashMap<>();

		for (int i = 0; i < initData1.length; i++) {
			item.put(initData1[i][ShoppingListT.INIT_DATA_NAME].toLowerCase(), initData1[i][ShoppingListT.INIT_DATA_CATEGORY].toLowerCase());
		}

		return item;
	}

	private String getItemName(SQLiteDatabase db, long id) {
		Cursor cursor = db.query(ItemT.TABLE_NAME, null,
				ItemT.COLUMN_ID + " = ?",
				new String[]{String.valueOf(id)}, null, null, null);
		String name = null;

		if (cursor.moveToFirst()) {
			name = cursor.getString(cursor.getColumnIndex(Migration2.ItemT.COLUMN_NAME));
		}

		return name;
	}

	private long getIdCategory(HashMap<String, String> item, String itemName) {
		String defaultCategory = mCtx.getResources().getStringArray(R.array.categories)[0].split("—")[0];
		String category = item.containsKey(itemName.toLowerCase()) ? item.get(itemName.toLowerCase()) : defaultCategory;
		return getCategory(mDb, category);
	}

	private long getCategory(SQLiteDatabase db, String name) {
		Cursor cursor = db.query(CategoryT.TABLE_NAME, null,
				CategoryT.COLUMN_NAME + " like '" + name + "'",
				null, null, null, null);
		long id = -1;

		if (cursor.moveToFirst()) {
			id = cursor.getLong(cursor.getColumnIndex(CategoryT.COLUMN_ID));
		}

		return id;
	}

	private long saveData(Cursor cursor, long idCategory) {
		return addItemData(mDb,
				cursor.getDouble(cursor.getColumnIndex(Migration2.ItemT.COLUMN_AMOUNT)),
				cursor.getLong(cursor.getColumnIndex(Migration2.ItemT.COLUMN_ID_UNIT)),
				cursor.getDouble(cursor.getColumnIndex(Migration2.ItemT.COLUMN_PRICE)),
				idCategory,
				cursor.getString(cursor.getColumnIndex(Migration2.ItemT.COLUMN_COMMENT)));
	}

	private long addItemData(SQLiteDatabase db, double amount, long idUnit, double price, long idCategory, String commit) {
		ContentValues values = new ContentValues();
		values.put(ItemDataT.COLUMN_AMOUNT, amount);
		values.put(ItemDataT.COLUMN_ID_UNIT, idUnit);
		values.put(ItemDataT.COLUMN_PRICE, price);
		values.put(ItemDataT.COLUMN_ID_CATEGORY, idCategory);
		values.put(ItemDataT.COLUMN_COMMENT, commit);
		return db.insert(ItemDataT.TABLE_NAME, null, values);
	}

	private void addItem(SQLiteDatabase db, long id, String name, String default_image, String image, long id_data) {
		ContentValues values = new ContentValues();
		values.put(ItemT.COLUMN_ID, id);
		values.put(ItemT.COLUMN_NAME, name);
		values.put(ItemT.COLUMN_DEFAULT_IMAGE_PATH, default_image);
		values.put(ItemT.COLUMN_IMAGE_PATH, image);
		values.put(ItemT.COLUMN_ID_DATA, id_data);
		db.insert(ItemT.TABLE_NAME, null, values);
	}

	private void addShoppingList(SQLiteDatabase db, long idItem, long idList, long idData, boolean isBought, long date) {
		ContentValues values = new ContentValues();
		values.put(ShoppingListT.COLUMN_ID_ITEM, idItem);
		values.put(ShoppingListT.COLUMN_ID_LIST, idList);
		values.put(ShoppingListT.COLUMN_ID_DATA, idData);
		values.put(ShoppingListT.COLUMN_IS_BOUGHT, isBought);
		values.put(ShoppingListT.COLUMN_DATE, date);
		db.insert(ShoppingListT.TABLE_NAME, null, values);
	}
	//</editor-fold>

	private void updateShowcase() {
		boolean list = mCtx.getSharedPreferences("showcase_internal", Context.MODE_PRIVATE)
				.getBoolean("hasShot0", false);
		boolean add_item = mCtx.getSharedPreferences("showcase_internal", Context.MODE_PRIVATE)
				.getBoolean("hasShot1", false);
		boolean item_in_list = mCtx.getSharedPreferences("showcase_internal", Context.MODE_PRIVATE)
				.getBoolean("hasShot2", false);
		boolean item = mCtx.getSharedPreferences("showcase_internal", Context.MODE_PRIVATE)
				.getBoolean("hasShot3", false);
		boolean currency = mCtx.getSharedPreferences("showcase_internal", Context.MODE_PRIVATE)
				.getBoolean("hasShot4", false);

		SharedPreferences sp = mCtx.getSharedPreferences(Showcase.PREFS_SHOWCASE, Context.MODE_PRIVATE);

		SharedPreferences.Editor e = sp.edit();
		e.putInt("status_" + Showcase.SHOT_ADD_LIST, list ? PrefsManager.SEQUENCE_FINISHED : PrefsManager.SEQUENCE_NEVER_STARTED);
		e.putInt("status_" + Showcase.SHOT_LIST, list ? PrefsManager.SEQUENCE_FINISHED : PrefsManager.SEQUENCE_NEVER_STARTED);
		e.putInt("status_" + Showcase.SHOT_ADD_ITEM, add_item ? PrefsManager.SEQUENCE_FINISHED : PrefsManager.SEQUENCE_NEVER_STARTED);
		e.putInt("status_" + Showcase.SHOT_ITEM_IN_LIST, item_in_list ? PrefsManager.SEQUENCE_FINISHED : PrefsManager.SEQUENCE_NEVER_STARTED);
		e.putInt("status_" + Showcase.SHOT_ITEM, item ? PrefsManager.SEQUENCE_FINISHED : PrefsManager.SEQUENCE_NEVER_STARTED);
		e.putInt("status_" + Showcase.SHOT_CURRENCY, currency ? PrefsManager.SEQUENCE_FINISHED : PrefsManager.SEQUENCE_NEVER_STARTED);
		e.apply();

		File file = new File(mCtx.getFilesDir().getParent() + "/shared_prefs/showcase_internal.xml");
		file.delete();
	}

	public class UnitT {
		public static final String TABLE_NAME = "Units";

		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_NAME = "unit_name";

		static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
				+ "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NAME + " TEXT NOT NULL"
				+ ");";

	}

	public class CurrencyT {
		public static final String TABLE_NAME = "Currencies";

		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_NAME = "currency_name";
		public static final String COLUMN_SYMBOL = "symbol";

		static final int INIT_DATA_NAME = 0;
		static final int INIT_DATA_SYMBOL = 1;

		static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
				+ "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NAME + " TEXT NOT NULL,"
				+ COLUMN_SYMBOL + " TEXT NOT NULL"
				+ ");";

		private static final String TEMP_TABLE_CREATE = "CREATE TEMPORARY TABLE " + TABLE_NAME + TMP_TABLE_SUFFIX
				+ "("
				+ Migration2.CurrencyT.COLUMN_ID + " INTEGER, "
				+ Migration2.CurrencyT.COLUMN_NAME + " TEXT,"
				+ Migration2.CurrencyT.COLUMN_SYMBOL + " TEXT"
				+ ");";
	}

	public class ListT {
		public static final String TABLE_NAME = "Lists";

		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_NAME = "list_name";
		public static final String COLUMN_ID_CURRENCY = "id_currency";
		public static final String COLUMN_IMAGE_PATH = "list_image_path";

		static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
				+ "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NAME + " TEXT NOT NULL, "
				+ COLUMN_ID_CURRENCY + " INTEGER NOT NULL, "
				+ COLUMN_IMAGE_PATH + " TEXT NOT NULL, "
				+ "FOREIGN KEY (" + COLUMN_ID_CURRENCY + ") REFERENCES " + CurrencyT.TABLE_NAME + " (" + CurrencyT.COLUMN_ID + ")"
				+ ");";

		private static final String TEMP_TABLE_CREATE = "CREATE TEMPORARY TABLE " + TABLE_NAME + TMP_TABLE_SUFFIX
				+ "("
				+ Migration2.ListT.COLUMN_ID + " INTEGER, "
				+ Migration2.ListT.COLUMN_NAME + " TEXT, "
				+ Migration2.ListT.COLUMN_ID_CURRENCY + " INTEGER, "
				+ Migration2.ListT.COLUMN_IMAGE_PATH + " TEXT"
				+ ");";
	}

	public class ItemT {
		public static final String TABLE_NAME = "Items";

		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_NAME = "item_name";
		public static final String COLUMN_IMAGE_PATH = "image_path";
		public static final String COLUMN_DEFAULT_IMAGE_PATH = "default_image_path";
		public static final String COLUMN_ID_DATA = "id_data";

		static final int INIT_DATA_NAME = 0;
		static final int INIT_DATA_UNIT = 1;
		static final int INIT_DATA_IMAGE = 2;
		static final int INIT_DATA_CATEGORY = 3;

		static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
				+ "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NAME + " TEXT NOT NULL, "
				+ COLUMN_DEFAULT_IMAGE_PATH + " TEXT NOT NULL, "
				+ COLUMN_IMAGE_PATH + " TEXT NOT NULL, "
				+ COLUMN_ID_DATA + " INTEGER NOT NULL, "
				+ "FOREIGN KEY (" + COLUMN_ID_DATA + ") REFERENCES " + ItemDataT.TABLE_NAME + " (" + ItemDataT.COLUMN_ID + ")"
				+ ");";

		private static final String TEMP_TABLE_CREATE = "CREATE TEMPORARY TABLE " + TABLE_NAME + TMP_TABLE_SUFFIX
				+ "("
				+ Migration2.ItemT.COLUMN_ID + " INTEGER, "
				+ Migration2.ItemT.COLUMN_NAME + " TEXT, "
				+ Migration2.ItemT.COLUMN_AMOUNT + " REAL, "
				+ Migration2.ItemT.COLUMN_ID_UNIT + " INTEGER, "
				+ Migration2.ItemT.COLUMN_PRICE + " REAL, "
				+ Migration2.ItemT.COLUMN_COMMENT + " TEXT, "
				+ Migration2.ItemT.COLUMN_DEFAULT_IMAGE_PATH + " TEXT, "
				+ Migration2.ItemT.COLUMN_IMAGE_PATH + " TEXT"
				+ ");";
	}

	public class ShoppingListT {
		public static final String TABLE_NAME = "ShoppingLists";

		public static final String COLUMN_ID_ITEM = "id_item";
		public static final String COLUMN_ID_LIST = "id_list";
		public static final String COLUMN_ID_DATA = "id_data";
		public static final String COLUMN_IS_BOUGHT = "is_bought";
		public static final String COLUMN_DATE = "date";

		static final int INIT_DATA_NAME = 0;
		static final int INIT_DATA_CATEGORY = 3;

		static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
				+ "("
				+ COLUMN_ID_ITEM + " INTEGER NOT NULL, "
				+ COLUMN_ID_LIST + " INTEGER NOT NULL, "
				+ COLUMN_ID_DATA + " INTEGER NOT NULL, "
				+ COLUMN_IS_BOUGHT + " INTEGER NOT NULL, "
				+ COLUMN_DATE + " INTEGER NOT NULL, "
				+ "FOREIGN KEY (" + COLUMN_ID_DATA + ") REFERENCES " + ItemDataT.TABLE_NAME + " (" + ItemDataT.COLUMN_ID + ") ON DELETE CASCADE, "
				+ "FOREIGN KEY (" + COLUMN_ID_ITEM + ") REFERENCES " + ItemT.TABLE_NAME + " (" + ItemT.COLUMN_ID + ") ON DELETE CASCADE, "
				+ "FOREIGN KEY (" + COLUMN_ID_LIST + ") REFERENCES " + ListT.TABLE_NAME + " (" + ListT.COLUMN_ID + ") ON DELETE CASCADE, "
				+ "PRIMARY KEY (" + COLUMN_ID_ITEM + ", " + COLUMN_ID_LIST + ")"
				+ ");";

		private static final String TEMP_TABLE_CREATE = "CREATE TEMPORARY TABLE " + TABLE_NAME + TMP_TABLE_SUFFIX
				+ "("
				+ Migration2.ShoppingListT.COLUMN_ID_ITEM + " INTEGER, "
				+ Migration2.ShoppingListT.COLUMN_ID_LIST + " INTEGER, "
				+ Migration2.ShoppingListT.COLUMN_IS_BOUGHT + " INTEGER, "
				+ Migration2.ShoppingListT.COLUMN_AMOUNT + " REAL, "
				+ Migration2.ShoppingListT.COLUMN_ID_UNIT + " INTEGER, "
				+ Migration2.ShoppingListT.COLUMN_PRICE + " REAL, "
				+ Migration2.ShoppingListT.COLUMN_COMMENT + " TEXT, "
				+ Migration2.ShoppingListT.COLUMN_DATE + " INTEGER"
				+ ");";
	}

	public class CategoryT {
		public static final String TABLE_NAME = "Categories";

		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_NAME = "category_name";
		public static final String COLUMN_COLOR = "color";

		static final int INIT_DATA_NAME = 0;
		static final int INIT_DATA_COLOR = 1;

		static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
				+ "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NAME + " TEXT NOT NULL, "
				+ COLUMN_COLOR + " INTEGER NOT NULL"
				+ ");";
	}

	public class ItemDataT {
		public static final String TABLE_NAME = "ItemData";

		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_AMOUNT = "amount";
		public static final String COLUMN_ID_UNIT = "id_unit";
		public static final String COLUMN_PRICE = "price";
		public static final String COLUMN_ID_CATEGORY = "id_category";
		public static final String COLUMN_COMMENT = "comment";

		static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
				+ "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_AMOUNT + " REAL, "
				+ COLUMN_ID_UNIT + " INTEGER NOT NULL, "
				+ COLUMN_PRICE + " REAL, "
				+ COLUMN_ID_CATEGORY + " INTEGER NOT NULL, "
				+ COLUMN_COMMENT + " TEXT, "
				+ "FOREIGN KEY (" + COLUMN_ID_UNIT + ") REFERENCES " + UnitT.TABLE_NAME + " (" + UnitT.COLUMN_ID + "), "
				+ "FOREIGN KEY (" + COLUMN_ID_CATEGORY + ") REFERENCES " + CategoryT.TABLE_NAME + " (" + CategoryT.COLUMN_ID + ")"
				+ ");";
	}
}
