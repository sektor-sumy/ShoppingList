package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.tables.CurrenciesTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;

public class ListsDS extends GenericDS<List> {

	public ListsDS(Context context) {
		super(context);
		mDbHelper.getReadableDatabase();
	}

	public ListCursor get(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + ListsTable.TABLE_NAME + ".*, " +
				CurrenciesTable.TABLE_NAME + "." + CurrenciesTable.COLUMN_NAME + ", " +
				CurrenciesTable.TABLE_NAME + "." + CurrenciesTable.COLUMN_SYMBOL + " " +
				"FROM " + ListsTable.TABLE_NAME + " INNER JOIN " + CurrenciesTable.TABLE_NAME + " " +
				"ON " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID_CURRENCY + " = " +
				CurrenciesTable.TABLE_NAME + "." + CurrenciesTable.COLUMN_ID + " " +
				"WHERE " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID + " = ?";
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
		return new ListCursor(cursor);
	}

	public ListCursor getAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + ListsTable.TABLE_NAME + ".*, " +
				"SUM(" + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_IS_BOUGHT +
				") AS " + ListsTable.AMOUNT_BOUGHT_ITEMS + ", " +
				"COUNT(" + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM +
				") AS " + ListsTable.AMOUNT_ITEMS + " " +
				"FROM " + ListsTable.TABLE_NAME + " left join " + ShoppingListTable.TABLE_NAME + " " +
				"ON " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID + " = " +
				ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_LIST + " " +
				"GROUP BY " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID +
				" ORDER BY " + ListsTable.COLUMN_ID + " DESC";
		Cursor cursor = db.rawQuery(selectQuery, null);
		return new ListCursor(cursor);
	}

	public boolean isCurrencyUsed(long idCurrency) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ListsTable.TABLE_NAME, null, ListsTable.COLUMN_ID_CURRENCY + " = " + idCurrency,
				null, null, null, null);
		ListCursor listCursor = new ListCursor(cursor);
		boolean result = listCursor.getCount() > 0;
		cursor.close();
		return result;
	}

	@Override
	public int update(List list) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(list);
		return db.update(ListsTable.TABLE_NAME, values, ListsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(list.getId())});
	}

	public void updateCurrency(long oldId, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ListsTable.COLUMN_ID_CURRENCY, newId);
		db.update(ListsTable.TABLE_NAME, values,
				ListsTable.COLUMN_ID_CURRENCY + " = ?",
				new String[]{String.valueOf(oldId)});
	}

	@Override
	public long add(List list) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(list);
		return db.insert(ListsTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(ListsTable.TABLE_NAME, ListsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(List list) {
		ContentValues values = new ContentValues();
		values.put(ListsTable.COLUMN_NAME, list.getName());
		values.put(ListsTable.COLUMN_ID_CURRENCY, list.getIdCurrency());
		values.put(ListsTable.COLUMN_IMAGE_PATH, list.getImagePath());
		return values;
	}

	public static class ListCursor extends EntityCursor<List> {
		public ListCursor(Cursor cursor) {
			super(cursor);
		}

		public List getEntity() {
			long id = getLong(getColumnIndex(ListsTable.COLUMN_ID));
			String name = getString(getColumnIndex(ListsTable.COLUMN_NAME));
			long idCurrency = getLong(getColumnIndex(ListsTable.COLUMN_ID_CURRENCY));
			String imagePath = getString(getColumnIndex(ListsTable.COLUMN_IMAGE_PATH));

			List list = new List(id, name, idCurrency, imagePath);

			if (getColumnIndex(CurrenciesTable.COLUMN_NAME) != -1) {
				String currencyName = getString(getColumnIndex(CurrenciesTable.COLUMN_NAME));
				String currencySymbol = getString(getColumnIndex(CurrenciesTable.COLUMN_SYMBOL));
				list.setCurrency(new Currency(idCurrency, currencyName, currencySymbol));
			}

			if (getColumnIndex(ListsTable.AMOUNT_BOUGHT_ITEMS) != -1) {
				int amount_bought_items = getInt(getColumnIndex(ListsTable.AMOUNT_BOUGHT_ITEMS));
				list.setAmountBoughtItems(amount_bought_items);
			}

			if (getColumnIndex(ListsTable.AMOUNT_ITEMS) != -1) {
				int amount_items = getInt(getColumnIndex(ListsTable.AMOUNT_ITEMS));
				list.setAmountItems(amount_items);
			}

			return list;
		}
	}
}
