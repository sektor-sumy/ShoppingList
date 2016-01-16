package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.db.tables.CurrenciesTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;

public class CurrenciesDS extends DictionaryDS<Currency> {

	public CurrenciesDS(Context context) {
		super(context);
	}

	@Override
	public CurrencyCursor getAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(CurrenciesTable.TABLE_NAME, null, null,
				null, null, null, CurrenciesTable.COLUMN_NAME);
		return new CurrencyCursor(cursor);
	}

	public CurrencyCursor getByName(String name) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(CurrenciesTable.TABLE_NAME, null, CurrenciesTable.COLUMN_NAME + " = '" + name + "'",
				null, null, null, null);
		return new CurrencyCursor(cursor);
	}

	public CurrencyCursor getByList(long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select " + CurrenciesTable.TABLE_NAME + ".* from " + CurrenciesTable.TABLE_NAME +
				" INNER JOIN " + ListsTable.TABLE_NAME + " ON " +
				CurrenciesTable.TABLE_NAME + "." + CurrenciesTable.COLUMN_ID + " = " + ListsTable.TABLE_NAME + "." + ListsTable.COLUMN_ID_CURRENCY +
				" where " + ListsTable.TABLE_NAME + " . " + ListsTable.COLUMN_ID + " = ?", new String[]{String.valueOf(idList)});
		return new CurrencyCursor(cursor);
	}

	public long getRandomId(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(CurrenciesTable.TABLE_NAME, null, CurrenciesTable.COLUMN_ID + " != " + id,
				null, null, null, CurrenciesTable.COLUMN_NAME, "1");
		CurrencyCursor currencyCursor = new CurrencyCursor(cursor);
		currencyCursor.moveToFirst();
		long selectedId = currencyCursor.getEntity().getId();
		cursor.close();
		return selectedId;
	}

	@Override
	public int update(Currency currency) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(currency);
		return db.update(CurrenciesTable.TABLE_NAME, values, CurrenciesTable.COLUMN_ID + " = ?",
				new String[]{String.valueOf(currency.getId())});
	}

	public int update(String newSymbol, String oldSymbol) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(CurrenciesTable.COLUMN_SYMBOL, newSymbol);
		return db.update(CurrenciesTable.TABLE_NAME, values, CurrenciesTable.COLUMN_SYMBOL + " = ?",
				new String[]{oldSymbol});
	}

	@Override
	public long add(Currency currency) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(currency);
		return db.insert(CurrenciesTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		long idDefaultCurrency = prefs.getLong(mContext.getString(R.string.settings_key_currency), -1);

		if (id == idDefaultCurrency || idDefaultCurrency == -1) {
			idDefaultCurrency = getRandomId(id);
		}

		new ListsDS(mContext).updateCurrency(id, idDefaultCurrency);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(CurrenciesTable.TABLE_NAME, CurrenciesTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(Currency currency) {
		ContentValues values = new ContentValues();
		values.put(CurrenciesTable.COLUMN_NAME, currency.getName());
		values.put(CurrenciesTable.COLUMN_SYMBOL, currency.getSymbol());
		return values;
	}

	public static class CurrencyCursor extends EntityCursor<Currency> {
		public CurrencyCursor(Cursor cursor) {
			super(cursor);
		}

		public Currency getEntity() {
			long id = getLong(getColumnIndex(CurrenciesTable.COLUMN_ID));
			String name = getString(getColumnIndex(CurrenciesTable.COLUMN_NAME));
			String symbol = getString(getColumnIndex(CurrenciesTable.COLUMN_SYMBOL));

			return new Currency(id, name, symbol);
		}
	}
}
