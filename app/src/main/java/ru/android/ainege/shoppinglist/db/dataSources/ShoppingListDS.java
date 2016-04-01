package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import ru.android.ainege.shoppinglist.db.ITable.*;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.entities.Unit;

public class ShoppingListDS extends GenericDS<ShoppingList> implements IShoppingLists{
	private static final String DEFAULT_ITEM_DATA = "id_default_data";

	public ShoppingListDS(Context context) {
		super(context);
	}

	@Override
	public EntityCursor<ItemData> getAll() {
		return null;
	}

	public ShoppingListCursor getItemsInList(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + TABLE_NAME + ".*, " +
				IItems.COLUMN_NAME + ", " + IItems.COLUMN_DEFAULT_IMAGE_PATH + ", " +
				IItems.COLUMN_IMAGE_PATH + ", " +
				IItems.TABLE_NAME + "." + IItems.COLUMN_ID_DATA + " AS " + DEFAULT_ITEM_DATA + ", " +
				IItemData.TABLE_NAME + ".*, " +
				IUnits.COLUMN_NAME + ", " + ICategories.COLUMN_NAME + ", " +
				ICategories.COLUMN_COLOR +
				" FROM " + TABLE_NAME +
				" INNER JOIN " + IItems.TABLE_NAME + " ON " +
				TABLE_NAME + "." + COLUMN_ID_ITEM + " = " + IItems.TABLE_NAME + "." + IItems.COLUMN_ID +
				" INNER JOIN " + IItemData.TABLE_NAME + " ON " +
				TABLE_NAME + "." + COLUMN_ID_DATA + " = " + IItemData.TABLE_NAME + "." + IItemData.COLUMN_ID +
				" INNER JOIN " + IUnits.TABLE_NAME + " ON " +
				IItemData.TABLE_NAME + "." + IItemData.COLUMN_ID_UNIT + " = " + IUnits.TABLE_NAME + "." + IUnits.COLUMN_ID +
				" INNER JOIN " + ICategories.TABLE_NAME + " ON " +
				IItemData.TABLE_NAME + "." + IItemData.COLUMN_ID_CATEGORY + " = " + ICategories.TABLE_NAME + "." + ICategories.COLUMN_ID +
				" WHERE " + TABLE_NAME + "." + COLUMN_ID_LIST + " = ? ";
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
		return new ShoppingListCursor(cursor);
	}

	public ShoppingListCursor getByName(String name, long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + TABLE_NAME + ".*, " +
				IItems.COLUMN_NAME + ", " +
				IItems.COLUMN_DEFAULT_IMAGE_PATH + ", " +
				IItems.COLUMN_IMAGE_PATH + ", " +
				IItems.TABLE_NAME + "." + IItems.COLUMN_ID_DATA + " AS " + DEFAULT_ITEM_DATA +
				" FROM " + TABLE_NAME + " INNER JOIN " + IItems.TABLE_NAME +
				" ON " + TABLE_NAME + "." + COLUMN_ID_ITEM + " = " + IItems.TABLE_NAME + "." + IItems.COLUMN_ID +
				" WHERE " + IItems.COLUMN_NAME + " LIKE '" + name +
				"' AND " + COLUMN_ID_LIST + " = " + idList, null);
		return new ShoppingListCursor(cursor);
	}

	public void setIsBought(boolean isBought, long idItem, long idList) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(isBought);
		db.update(TABLE_NAME,
				values,
				COLUMN_ID_ITEM + " = ? AND " + COLUMN_ID_LIST + " = ?",
				new String[]{String.valueOf(idItem), String.valueOf(idList)});
	}

	@Override
	public int update(ShoppingList shoppingList) {
		return update(shoppingList, shoppingList.getIdItem());
	}

	public int update(ShoppingList shoppingList, long idOldItem) {
		new ItemDataDS(mContext).update(new ItemData(shoppingList));

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(shoppingList.isBought());
		values.put(COLUMN_ID_ITEM, shoppingList.getIdItem());
		return db.update(TABLE_NAME, values,
				COLUMN_ID_ITEM + " = ? AND " + COLUMN_ID_LIST + " = ?",
				new String[]{String.valueOf(idOldItem), String.valueOf(shoppingList.getIdList())});
	}

	@Override
	public long add(ShoppingList shoppingList) {
		long idData = new ItemDataDS(mContext).add(new ItemData(shoppingList));
		shoppingList.setIdItemData(idData);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(shoppingList);
		return db.insert(TABLE_NAME, null, values);
	}

	@Override
	public void delete(long idItemData) {
		new ItemDataDS(mContext).delete(idItemData);
	}

	private ContentValues createContentValues(boolean isBought) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_IS_BOUGHT, isBought);
		return values;
	}

	private ContentValues createContentValues(ShoppingList shoppingList) {
		ContentValues values = createContentValues(shoppingList.isBought());
		values.put(COLUMN_ID_ITEM, shoppingList.getIdItem());
		values.put(COLUMN_ID_LIST, shoppingList.getIdList());
		values.put(COLUMN_ID_DATA, shoppingList.getIdItemData());
		values.put(COLUMN_DATE, System.currentTimeMillis() / 1000);
		return values;
	}

	public static class ShoppingListCursor extends EntityCursor<ShoppingList> {
		public ShoppingListCursor(Cursor cursor) {
			super(cursor);
		}

		public ShoppingList getEntity() {
			long idItem = getLong(getColumnIndex(COLUMN_ID_ITEM));
			long idList = getLong(getColumnIndex(COLUMN_ID_LIST));
			long idData = getLong(getColumnIndex(COLUMN_ID_DATA));
			boolean isBought = getInt(getColumnIndex(COLUMN_IS_BOUGHT)) == 1;
			long date = getLong(getColumnIndex(COLUMN_DATE));

			ShoppingList shoppingList = new ShoppingList(idItem, idList, isBought, idData, new Date(date));

			if (getColumnIndex(IItems.COLUMN_NAME) != -1) {
				String nameItem = getString(getColumnIndex(IItems.COLUMN_NAME));
				String defaultImage = getString(getColumnIndex(IItems.COLUMN_DEFAULT_IMAGE_PATH));
				String image = getString(getColumnIndex(IItems.COLUMN_IMAGE_PATH));
				long idItemData = getLong(getColumnIndex(DEFAULT_ITEM_DATA));

				shoppingList.setItem(new Item(idItem, nameItem, defaultImage, image, idItemData));
			}

			if (getColumnIndex(IItemData.COLUMN_AMOUNT) != -1) {
				shoppingList.setAmount(getDouble(getColumnIndex(IItemData.COLUMN_AMOUNT)));
				shoppingList.setPrice(getDouble(getColumnIndex(IItemData.COLUMN_PRICE)));
				shoppingList.setComment(getString(getColumnIndex(IItemData.COLUMN_COMMENT)));

				long idUnit = getLong(getColumnIndex(IItemData.COLUMN_ID_UNIT));
				if (getColumnIndex(IUnits.COLUMN_NAME) != -1) {
					String unitName = getString(getColumnIndex(IUnits.COLUMN_NAME));
					shoppingList.setUnit(new Unit(idUnit, unitName));
				} else {
					shoppingList.setIdUnit(idUnit);
				}

				long idCategory = getLong(getColumnIndex(IItemData.COLUMN_ID_CATEGORY));
				if (getColumnIndex(ICategories.COLUMN_NAME) != -1) {
					String categoryName = getString(getColumnIndex(ICategories.COLUMN_NAME));
					int color = getInt(getColumnIndex(ICategories.COLUMN_COLOR));
					shoppingList.setCategory(new Category(idCategory, categoryName, color));
				} else {
					shoppingList.setIdCategory(idCategory);
				}
			}

			return shoppingList;
		}
	}
}
