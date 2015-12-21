package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class ShoppingListDataSource extends GenericDataSource<ShoppingList> {

	public ShoppingListDataSource(Context context) {
		super(context);
	}

	public ShoppingListCursor getItemsInList(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + ShoppingListTable.TABLE_NAME + ".*, " +
				ItemsTable.COLUMN_NAME + ", " +
				ItemsTable.COLUMN_DEFAULT_IMAGE_PATH + ", " +
				ItemsTable.COLUMN_IMAGE_PATH + ", " +
				UnitsTable.COLUMN_NAME +
				" FROM " + ShoppingListTable.TABLE_NAME + " INNER JOIN " + ItemsTable.TABLE_NAME + " ON " +
				ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM + " = " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID +
				" LEFT JOIN " + UnitsTable.TABLE_NAME + " ON " +
				ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_UNIT + " = " + UnitsTable.TABLE_NAME + "." + UnitsTable.COLUMN_ID +
				" WHERE " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_LIST + " = ? ";
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
		return new ShoppingListCursor(cursor);
	}

	public ShoppingListCursor existItemInList(String name, long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select " + ShoppingListTable.TABLE_NAME + ".*, " +
				ItemsTable.COLUMN_NAME + ", " +
				ItemsTable.COLUMN_DEFAULT_IMAGE_PATH + ", " +
				ItemsTable.COLUMN_IMAGE_PATH +
				" from " + ShoppingListTable.TABLE_NAME + " INNER JOIN " + ItemsTable.TABLE_NAME +
				" ON " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM + " = " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID +
				" where " + ItemsTable.COLUMN_NAME + " like '" + name +
				"' AND " + ShoppingListTable.COLUMN_ID_LIST + " = " + idList, null);
		return new ShoppingListCursor(cursor);
	}

	public boolean isUnitUsed(long idUnit) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ShoppingListTable.TABLE_NAME, null, ShoppingListTable.COLUMN_ID_UNIT + " = " + idUnit,
				null, null, null, null);
		ShoppingListCursor itemInListCursor = new ShoppingListCursor(cursor);
		boolean result = itemInListCursor.getCount() > 0;
		cursor.close();
		return result;
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
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(shoppingList);
		return db.update(ShoppingListTable.TABLE_NAME, values,
				ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
				new String[]{String.valueOf(idOldItem), String.valueOf(shoppingList.getIdList())});
	}

	public void updateUnit(long oldId, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ShoppingListTable.COLUMN_ID_UNIT, newId);
		db.update(ShoppingListTable.TABLE_NAME, values,
				ShoppingListTable.COLUMN_ID_UNIT + " = ?",
				new String[]{String.valueOf(oldId)});
	}

	@Override
	public long add(ShoppingList shoppingList) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(shoppingList);
		values.put(ShoppingListTable.COLUMN_ID_LIST, shoppingList.getIdList());
		values.put(ShoppingListTable.COLUMN_DATE, System.currentTimeMillis() / 1000);
		return db.insert(ShoppingListTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long idItem) {

	}

	public void delete(long idItem, long idList) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(ShoppingListTable.TABLE_NAME,
				ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
				new String[]{String.valueOf(idItem), String.valueOf(idList)});
	}

	private ContentValues createContentValues(boolean isBought) {
		ContentValues values = new ContentValues();
		values.put(ShoppingListTable.COLUMN_IS_BOUGHT, isBought);
		return values;
	}

	private ContentValues createContentValues(ShoppingList shoppingList) {
		ContentValues values = createContentValues(shoppingList.isBought());
		values.put(ShoppingListTable.COLUMN_AMOUNT, shoppingList.getAmount());
		values.put(ShoppingListTable.COLUMN_ID_UNIT, shoppingList.getIdUnit());
		values.put(ShoppingListTable.COLUMN_PRICE, shoppingList.getPrice());
		values.put(ShoppingListTable.COLUMN_COMMENT, shoppingList.getComment());
		values.put(ShoppingListTable.COLUMN_ID_ITEM, shoppingList.getIdItem());
		return values;
	}

	public static class ShoppingListCursor extends EntityCursor<ShoppingList> {
		public ShoppingListCursor(Cursor cursor) {
			super(cursor);
		}

		public ShoppingList getEntity() {
			long idItem = getLong(getColumnIndex(ShoppingListTable.COLUMN_ID_ITEM));
			long idList = getLong(getColumnIndex(ShoppingListTable.COLUMN_ID_LIST));
			boolean isBought = getInt(getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) == 1;
			double amount = getDouble(getColumnIndex(ShoppingListTable.COLUMN_AMOUNT));
			long idUnit = getLong(getColumnIndex(ShoppingListTable.COLUMN_ID_UNIT));
			double price = getDouble(getColumnIndex(ShoppingListTable.COLUMN_PRICE));
			String comment = getString(getColumnIndex(ItemsTable.COLUMN_COMMENT));
			long date = getLong(getColumnIndex(ShoppingListTable.COLUMN_DATE));

			ShoppingList shoppingList = new ShoppingList(idItem, idList, isBought, amount, idUnit, price, comment, new Date(date));

			if (getColumnIndex(UnitsTable.COLUMN_NAME) != -1) {
				String unitName = getString(getColumnIndex(UnitsTable.COLUMN_NAME));
				shoppingList.setUnit(new Unit(idUnit, unitName));
			}

			if (getColumnIndex(ItemsTable.COLUMN_NAME) != -1) {
				String nameItem = getString(getColumnIndex(ItemsTable.COLUMN_NAME));
				Item item = new Item(idItem, nameItem, null, null);
				if (getColumnIndex(ItemsTable.COLUMN_IMAGE_PATH) != -1) {
					item.setImagePath(getString(getColumnIndex(ItemsTable.COLUMN_IMAGE_PATH)));
				}
				if (getColumnIndex(ItemsTable.COLUMN_DEFAULT_IMAGE_PATH) != -1) {
					item.setDefaultImagePath(getString(getColumnIndex(ItemsTable.COLUMN_DEFAULT_IMAGE_PATH)));
				}

				shoppingList.setItem(item);
			}

			return shoppingList;
		}
	}
}
