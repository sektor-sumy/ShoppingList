package ru.android.ainege.shoppinglist.db.migration;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.util.Image;

public class Create {
	private SQLiteDatabase mDb;
	private Context mCtx;

	public Create(SQLiteDatabase db, Context ctx) {
		mDb = db;
		mCtx = ctx;
	}

	public void run() {
		mDb.execSQL(Migration3.UnitT.TABLE_CREATE);
		mDb.execSQL(Migration3.CurrencyT.TABLE_CREATE);
		mDb.execSQL(Migration3.CategoryT.TABLE_CREATE);
		mDb.execSQL(Migration3.ListT.TABLE_CREATE);
		mDb.execSQL(Migration3.ItemDataT.TABLE_CREATE);
		mDb.execSQL(Migration3.ItemT.TABLE_CREATE);
		mDb.execSQL(Migration3.ShoppingListT.TABLE_CREATE);

		initialUnit();
		initialCurrency();
		initialCategory();
		initialList();
		initialItem();
	}

	private void initialUnit() {
		String[] units = mCtx.getResources().getStringArray(R.array.units);

		for (String unit : units) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(Migration3.UnitT.COLUMN_NAME, unit);
			mDb.insert(Migration3.UnitT.TABLE_NAME, null, contentValue);
		}
	}

	private void initialCurrency() {
		String[][] initData = ShoppingListSQLiteHelper.parseInitData(mCtx.getResources().getStringArray(R.array.currency));

		for (String[] currencyData : initData) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(Migration3.CurrencyT.COLUMN_NAME, currencyData[Migration3.CurrencyT.INIT_DATA_NAME]);
			contentValue.put(Migration3.CurrencyT.COLUMN_SYMBOL, currencyData[Migration3.CurrencyT.INIT_DATA_SYMBOL]);
			mDb.insert(Migration3.CurrencyT.TABLE_NAME, null, contentValue);
		}
	}

	private void initialCategory() {
		String[][] initData = ShoppingListSQLiteHelper.parseInitData(mCtx.getResources().getStringArray(R.array.categories));

		for (String[] category : initData) {
			ContentValues contentValue = new ContentValues();
			contentValue.put(Migration3.CategoryT.COLUMN_NAME, category[Migration3.CategoryT.INIT_DATA_NAME]);
			contentValue.put(Migration3.CategoryT.COLUMN_COLOR, category[Migration3.CategoryT.INIT_DATA_COLOR]);
			mDb.insert(Migration3.CategoryT.TABLE_NAME, null, contentValue);
		}
	}

	private void initialList() {
		ContentValues contentValue = new ContentValues();

		contentValue.put(Migration3.ListT.COLUMN_NAME, "Ваш список");
		contentValue.put(Migration3.ListT.COLUMN_ID_CURRENCY, 1);
		contentValue.put(Migration3.ListT.COLUMN_IMAGE_PATH, Image.LIST_IMAGE_PATH + "random_list_0.png");

		mDb.insert(Migration3.ListT.TABLE_NAME, null, contentValue);
	}

	private void initialItem() {
		String[][] initData = ShoppingListSQLiteHelper.parseInitData(mCtx.getResources().getStringArray(R.array.items));
		HashMap<String, Unit> unit = UnitsDS.getUnit(mDb);
		HashMap<String, Category> category = CategoriesDS.getCategories(mDb);

		for (String[] itemData : initData) {
			long idData = addDataItem(unit.get(itemData[Migration3.ItemT.INIT_DATA_UNIT]), category.get(itemData[Migration3.ItemT.INIT_DATA_CATEGORY]));
			addItem(itemData, idData);
		}
	}

	private long addDataItem(Unit unit, Category category) {
		ContentValues data = new ContentValues();

		data.put(Migration3.ItemDataT.COLUMN_ID_UNIT, unit.getId());
		data.put(Migration3.ItemDataT.COLUMN_ID_CATEGORY, category.getId());

		return mDb.insert(Migration3.ItemDataT.TABLE_NAME, null, data);
	}

	private void addItem(String[] itemData, long idData) {
		ContentValues contentValue = new ContentValues();

		contentValue.put(Migration3.ItemT.COLUMN_NAME, itemData[Migration3.ItemT.INIT_DATA_NAME]);
		contentValue.put(Migration3.ItemT.COLUMN_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[Migration3.ItemT.INIT_DATA_IMAGE]);
		contentValue.put(Migration3.ItemT.COLUMN_DEFAULT_IMAGE_PATH, Image.ITEM_IMAGE_PATH + itemData[Migration3.ItemT.INIT_DATA_IMAGE]);
		contentValue.put(Migration3.ItemT.COLUMN_ID_DATA, idData);

		mDb.insert(Migration3.ItemT.TABLE_NAME, null, contentValue);
	}
}
