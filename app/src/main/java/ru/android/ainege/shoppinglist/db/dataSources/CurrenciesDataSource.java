package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.db.tables.CurrencyTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;

public class CurrenciesDataSource extends DictionaryDataSource<Currency> {

	public CurrenciesDataSource(Context context) {
		super(context);
	}

	@Override
	public CurrencyCursor getAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select " + CurrencyTable.TABLE_NAME + ".* from " + CurrencyTable.TABLE_NAME +
				" ORDER BY " + CurrencyTable.TABLE_NAME + "." + CurrencyTable.COLUMN_NAME, null);
		return new CurrencyCursor(cursor);
	}

	public CurrencyCursor getByList(long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select " + CurrencyTable.TABLE_NAME + ".* from " + CurrencyTable.TABLE_NAME +
				" INNER JOIN " + ListsTable.TABLE_NAME + " ON " +
				CurrencyTable.TABLE_NAME + "." + CurrencyTable.COLUMN_ID + " = " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID_CURRENCY +
				" where " + ListsTable.TABLE_NAME + " . " + ListsTable.COLUMN_ID + " = ?", new String[]{String.valueOf(idList)});

		return new CurrencyCursor(cursor);
	}

	@Override
	public int update(Currency currency) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(currency);
		return db.update(CurrencyTable.TABLE_NAME, values, CurrencyTable.COLUMN_ID + " = ?",
				new String[]{String.valueOf(currency.getId())});
	}

	@Override
	public long add(Currency currency) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(currency);
		return db.insert(CurrencyTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(CurrencyTable.TABLE_NAME, CurrencyTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(Currency currency) {
		ContentValues values = new ContentValues();
		values.put(CurrencyTable.COLUMN_NAME, currency.getName());
		values.put(CurrencyTable.COLUMN_SYMBOL, currency.getSymbol());
		return values;
	}

	public static class CurrencyCursor extends EntityCursor<Currency> {
		public CurrencyCursor(Cursor cursor) {
			super(cursor);
		}

		public Currency getEntity() {
			long id = getLong(getColumnIndex(CurrencyTable.COLUMN_ID));
			String name = getString(getColumnIndex(CurrencyTable.COLUMN_NAME));
			String symbol = getString(getColumnIndex(CurrencyTable.COLUMN_SYMBOL));

			return new Currency(id, name, symbol);
		}
	}
}
