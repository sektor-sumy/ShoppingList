package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.db.tables.CategoriesTable;
import ru.android.ainege.shoppinglist.db.tables.ItemDataTable;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class ShoppingListDS extends GenericDS<ShoppingList> {
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
		String selectQuery = "SELECT " + ShoppingListTable.TABLE_NAME + ".*, " +
				ItemsTable.COLUMN_NAME + ", " + ItemsTable.COLUMN_DEFAULT_IMAGE_PATH + ", " +
				ItemsTable.COLUMN_IMAGE_PATH + ", " +
				ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID_DATA + " AS " + DEFAULT_ITEM_DATA + ", " +
				ItemDataTable.TABLE_NAME + ".*, " +
				UnitsTable.COLUMN_NAME + ", " + CategoriesTable.COLUMN_NAME + ", " +
				CategoriesTable.COLUMN_COLOR +
				" FROM " + ShoppingListTable.TABLE_NAME +
				" INNER JOIN " + ItemsTable.TABLE_NAME + " ON " +
				ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM + " = " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID +
				" INNER JOIN " + ItemDataTable.TABLE_NAME + " ON " +
				ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_DATA + " = " + ItemDataTable.TABLE_NAME + "." + ItemDataTable.COLUMN_ID +
				" INNER JOIN " + UnitsTable.TABLE_NAME + " ON " +
				ItemDataTable.TABLE_NAME + "." + ItemDataTable.COLUMN_ID_UNIT + " = " + UnitsTable.TABLE_NAME + "." + UnitsTable.COLUMN_ID +
				" INNER JOIN " + CategoriesTable.TABLE_NAME + " ON " +
				ItemDataTable.TABLE_NAME + "." + ItemDataTable.COLUMN_ID_CATEGORY + " = " + CategoriesTable.TABLE_NAME + "." + CategoriesTable.COLUMN_ID +
				" WHERE " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_LIST + " = ? ";
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
		return new ShoppingListCursor(cursor);
	}

	public ShoppingListCursor getByName(String name, long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + ShoppingListTable.TABLE_NAME + ".*, " +
				ItemsTable.COLUMN_NAME + ", " +
				ItemsTable.COLUMN_DEFAULT_IMAGE_PATH + ", " +
				ItemsTable.COLUMN_IMAGE_PATH + ", " +
				ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID_DATA + " AS " + DEFAULT_ITEM_DATA +
				" FROM " + ShoppingListTable.TABLE_NAME + " INNER JOIN " + ItemsTable.TABLE_NAME +
				" ON " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM + " = " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID +
				" WHERE " + ItemsTable.COLUMN_NAME + " LIKE '" + name +
				"' AND " + ShoppingListTable.COLUMN_ID_LIST + " = " + idList, null);
		return new ShoppingListCursor(cursor);
	}

	public void setIsBought(boolean isBought, long idItem, long idList) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(isBought);
		db.update(ShoppingListTable.TABLE_NAME,
				values,
				ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
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
		values.put(ShoppingListTable.COLUMN_ID_ITEM, shoppingList.getIdItem());
		return db.update(ShoppingListTable.TABLE_NAME, values,
				ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
				new String[]{String.valueOf(idOldItem), String.valueOf(shoppingList.getIdList())});
	}

	@Override
	public long add(ShoppingList shoppingList) {
		long idData = new ItemDataDS(mContext).add(new ItemData(shoppingList));
		shoppingList.setIdItemData(idData);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(shoppingList);
		return db.insert(ShoppingListTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long idItemData) {
		new ItemDataDS(mContext).delete(idItemData);
	}

	private ContentValues createContentValues(boolean isBought) {
		ContentValues values = new ContentValues();
		values.put(ShoppingListTable.COLUMN_IS_BOUGHT, isBought);
		return values;
	}

	private ContentValues createContentValues(ShoppingList shoppingList) {
		ContentValues values = createContentValues(shoppingList.isBought());
		values.put(ShoppingListTable.COLUMN_ID_ITEM, shoppingList.getIdItem());
		values.put(ShoppingListTable.COLUMN_ID_LIST, shoppingList.getIdList());
		values.put(ShoppingListTable.COLUMN_ID_DATA, shoppingList.getIdItemData());
		values.put(ShoppingListTable.COLUMN_DATE, System.currentTimeMillis() / 1000);
		return values;
	}

	public static class ShoppingListCursor extends EntityCursor<ShoppingList> {
		public ShoppingListCursor(Cursor cursor) {
			super(cursor);
		}

		public ShoppingList getEntity() {
			long idItem = getLong(getColumnIndex(ShoppingListTable.COLUMN_ID_ITEM));
			long idList = getLong(getColumnIndex(ShoppingListTable.COLUMN_ID_LIST));
			long idData = getLong(getColumnIndex(ShoppingListTable.COLUMN_ID_DATA));
			boolean isBought = getInt(getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) == 1;
			long date = getLong(getColumnIndex(ShoppingListTable.COLUMN_DATE));

			ShoppingList shoppingList = new ShoppingList(idItem, idList, isBought, idData, new Date(date));

			if (getColumnIndex(ItemsTable.COLUMN_NAME) != -1) {
				String nameItem = getString(getColumnIndex(ItemsTable.COLUMN_NAME));
				String defaultImage = getString(getColumnIndex(ItemsTable.COLUMN_DEFAULT_IMAGE_PATH));
				String image = getString(getColumnIndex(ItemsTable.COLUMN_IMAGE_PATH));
				long idItemData = getLong(getColumnIndex(DEFAULT_ITEM_DATA));

				shoppingList.setItem(new Item(idItem, nameItem, defaultImage, image, idItemData));
			}

			if (getColumnIndex(ItemDataTable.COLUMN_AMOUNT) != -1) {
				shoppingList.setAmount(getDouble(getColumnIndex(ItemDataTable.COLUMN_AMOUNT)));
				shoppingList.setPrice(getDouble(getColumnIndex(ItemDataTable.COLUMN_PRICE)));
				shoppingList.setComment(getString(getColumnIndex(ItemDataTable.COLUMN_COMMENT)));

				long idUnit = getLong(getColumnIndex(ItemDataTable.COLUMN_ID_UNIT));
				if (getColumnIndex(UnitsTable.COLUMN_NAME) != -1) {
					String unitName = getString(getColumnIndex(UnitsTable.COLUMN_NAME));
					shoppingList.setUnit(new Unit(idUnit, unitName));
				} else {
					shoppingList.setIdUnit(idUnit);
				}

				long idCategory = getLong(getColumnIndex(ItemDataTable.COLUMN_ID_CATEGORY));
				if (getColumnIndex(CategoriesTable.COLUMN_NAME) != -1) {
					String categoryName = getString(getColumnIndex(CategoriesTable.COLUMN_NAME));
					int color = getInt(getColumnIndex(CategoriesTable.COLUMN_COLOR));
					shoppingList.setCategory(new Category(idCategory, categoryName, color));
				} else {
					shoppingList.setIdCategory(idCategory);
				}
			}

			return shoppingList;
		}
	}
}
