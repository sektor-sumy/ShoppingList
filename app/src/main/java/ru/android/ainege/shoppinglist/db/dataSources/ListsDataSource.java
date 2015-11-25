package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.tables.CurrencyTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;

public class ListsDataSource {
	private Context mContext;
	private ShoppingListSQLiteHelper mDbHelper;

	public ListsDataSource(Context context) {
		mContext = context;
		mDbHelper = new ShoppingListSQLiteHelper(mContext);
	}

	public ListCursor get(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + ListsTable.TABLE_NAME + ".*, " +
				CurrencyTable.TABLE_NAME  + "." + CurrencyTable.COLUMN_NAME + ", " +
				CurrencyTable.TABLE_NAME  + "." + CurrencyTable.COLUMN_SYMBOL + " " +
				"FROM " + ListsTable.TABLE_NAME + " INNER JOIN " + CurrencyTable.TABLE_NAME + " " +
				"ON " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID_CURRENCY + " = " +
				CurrencyTable.TABLE_NAME + "." + CurrencyTable.COLUMN_ID + " " +
				"WHERE " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID + " = ?";
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
		return new ListCursor(cursor);
	}

	public ListCursor getAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ListsTable.TABLE_NAME, null, null, null, null, null, null, null);
		return new ListCursor(cursor);
	}

	public ListCursor getAllWithStatictic() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + ListsTable.TABLE_NAME + ".*, " +
				"SUM(" + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_IS_BOUGHT +
				") AS " + ListsTable.AMOUNT_BOUGHT_ITEMS + ", " +
				"COUNT(" + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM +
				") AS " + ListsTable.AMOUNT_ITEMS + " " +
				"FROM " + ListsTable.TABLE_NAME + " left join " + ShoppingListTable.TABLE_NAME + " " +
				"ON " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID + " = " +
				ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_LIST + " " +
				"GROUP BY " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID;
		Cursor cursor = db.rawQuery(selectQuery, null);
		return new ListCursor(cursor);
	}

	public int update(List list) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(list);
		return db.update(ListsTable.TABLE_NAME, values, ListsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(list.getId())});
	}

	public long add(List list) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(list);
		return db.insert(ListsTable.TABLE_NAME, null, values);
	}

	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(ListsTable.TABLE_NAME, ListsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(List list) {
		ContentValues values = new ContentValues();
		values.put(ListsTable.COLUMN_NAME, list.getName());
		values.put(ListsTable.COLUMN_ID_CURRENCY, list.getIdCurrenty());
		return values;
	}

	public static class ListCursor extends CursorWrapper {
		public ListCursor(Cursor cursor) {
			super(cursor);
		}

		public List getList() {
			long id = getLong(getColumnIndex(ListsTable.COLUMN_ID));
			String name = getString(getColumnIndex(ListsTable.COLUMN_NAME));
			long idCurrency = getLong(getColumnIndex(ListsTable.COLUMN_ID_CURRENCY));

			List list = new List(id, name, idCurrency);

			if (getColumnIndex(CurrencyTable.COLUMN_NAME) != -1) {
				String currencyName = getString(getColumnIndex(CurrencyTable.COLUMN_NAME));
				String currencySymbol = getString(getColumnIndex(CurrencyTable.COLUMN_SYMBOL));
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

		public ArrayList<List> getLists() {
			ArrayList<List> list = new ArrayList<>();
			moveToFirst();
			do {
				list.add(getList());
			} while (moveToNext());
			return list;
		}
	}
}
