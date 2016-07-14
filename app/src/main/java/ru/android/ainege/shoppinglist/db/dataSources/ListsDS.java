package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.TableInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.CurrenciesInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.ShoppingListsInterface;
import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.db.entities.List;

public class ListsDS extends GenericDS<List> implements TableInterface.ListsInterface {

	public ListsDS(Context context) {
		super(context);
		mDbHelper.getReadableDatabase();
	}

	@Override
	public ListCursor getAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + TABLE_NAME + ".*, " +
				"SUM(" + ShoppingListsInterface.TABLE_NAME + "." + ShoppingListsInterface.COLUMN_IS_BOUGHT +
				") AS " + ListCursor.AMOUNT_BOUGHT_ITEMS + ", " +
				"COUNT(" + ShoppingListsInterface.TABLE_NAME + "." + ShoppingListsInterface.COLUMN_ID_ITEM +
				") AS " + ListCursor.AMOUNT_ITEMS + " " +
				"FROM " + TABLE_NAME + " left join " + ShoppingListsInterface.TABLE_NAME + " " +
				"ON " + TABLE_NAME + "." + COLUMN_ID + " = " +
				ShoppingListsInterface.TABLE_NAME + "." + ShoppingListsInterface.COLUMN_ID_LIST + " " +
				"GROUP BY " + TABLE_NAME + "." + COLUMN_ID +
				" ORDER BY " + COLUMN_ID + " DESC";
		Cursor cursor = db.rawQuery(selectQuery, null);
		return new ListCursor(cursor);
	}

	public ListCursor get(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + TABLE_NAME + ".*, " +
				CurrenciesInterface.TABLE_NAME + "." + CurrenciesInterface.COLUMN_NAME + ", " +
				CurrenciesInterface.TABLE_NAME + "." + CurrenciesInterface.COLUMN_SYMBOL + " " +
				"FROM " + TABLE_NAME + " INNER JOIN " + CurrenciesInterface.TABLE_NAME + " " +
				"ON " + TABLE_NAME + "." + COLUMN_ID_CURRENCY + " = " +
				CurrenciesInterface.TABLE_NAME + "." + CurrenciesInterface.COLUMN_ID + " " +
				"WHERE " + TABLE_NAME + "." + COLUMN_ID + " = ?";
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
		return new ListCursor(cursor);
	}

	@Override
	public int update(List list) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(list);
		return db.update(TABLE_NAME, values, COLUMN_ID + " = ? ", new String[]{String.valueOf(list.getId())});
	}

	public void changeCurrency(long oldId, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_ID_CURRENCY, newId);
		db.update(TABLE_NAME, values,
				COLUMN_ID_CURRENCY + " = ?",
				new String[]{String.valueOf(oldId)});
	}

	@Override
	public long add(List list) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(list);
		return db.insert(TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(TABLE_NAME, COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(List list) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME, list.getName());
		values.put(COLUMN_ID_CURRENCY, list.getIdCurrency());
		values.put(COLUMN_IMAGE_PATH, list.getImagePath());
		return values;
	}

	public static class ListCursor extends EntityCursor<List> {
		public static final String AMOUNT_BOUGHT_ITEMS = "amount_bought";
		public static final String AMOUNT_ITEMS = "amount_items";

		public ListCursor(Cursor cursor) {
			super(cursor);
		}

		public List getEntity() {
			long id = getLong(getColumnIndex(COLUMN_ID));
			String name = getString(getColumnIndex(COLUMN_NAME));
			long idCurrency = getLong(getColumnIndex(COLUMN_ID_CURRENCY));
			String imagePath = getString(getColumnIndex(COLUMN_IMAGE_PATH));

			List list = new List(id, name, idCurrency, imagePath);

			if (getColumnIndex(CurrenciesInterface.COLUMN_NAME) != -1) {
				String currencyName = getString(getColumnIndex(CurrenciesInterface.COLUMN_NAME));
				String currencySymbol = getString(getColumnIndex(CurrenciesInterface.COLUMN_SYMBOL));
				list.setCurrency(new Currency(idCurrency, currencyName, currencySymbol));
			}

			if (getColumnIndex(AMOUNT_ITEMS) != -1) {
				int amount_items = getInt(getColumnIndex(AMOUNT_ITEMS));
				int amount_bought_items = getInt(getColumnIndex(AMOUNT_BOUGHT_ITEMS));
				list.setAmountItems(amount_items);
				list.setAmountBoughtItems(amount_bought_items);
			}

			return list;
		}
	}
}
