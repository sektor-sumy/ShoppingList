package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.TableInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.ListsInterface;
import ru.android.ainege.shoppinglist.db.entities.Currency;

public class CurrenciesDS extends DictionaryDS<Currency> implements TableInterface.CurrenciesInterface {
	public CurrenciesDS(Context context) {
		super(context);
	}

	@Override
	public CurrencyCursor getAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null,
				null, null, null, COLUMN_NAME);
		return new CurrencyCursor(cursor);
	}

	@Override
	public CurrencyCursor getAll(long withoutId) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " != " + withoutId,
				null, null, null, COLUMN_NAME);
		return new CurrencyCursor(cursor);
	}

	public CurrencyCursor getByName(String name) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME + " = '" + name + "'",
				null, null, null, null);
		return new CurrencyCursor(cursor);
	}

	public CurrencyCursor getByList(long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select " + TABLE_NAME + ".* from " + TABLE_NAME +
				" INNER JOIN " + ListsInterface.TABLE_NAME + " ON " +
				TABLE_NAME + "." + COLUMN_ID + " = " + ListsInterface.TABLE_NAME + "." + ListsInterface.COLUMN_ID_CURRENCY +
				" where " + ListsInterface.TABLE_NAME + " . " + ListsInterface.COLUMN_ID + " = ?", new String[]{String.valueOf(idList)});
		return new CurrencyCursor(cursor);
	}

	@Override
	public boolean isUsed(long idCurrency) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ListsInterface.TABLE_NAME, null, ListsInterface.COLUMN_ID_CURRENCY + " = " + idCurrency,
				null, null, null, null);
		boolean result = cursor.getCount() > 0;
		cursor.close();
		return result;
	}

	@Override
	public int update(Currency currency) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(currency);
		return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
				new String[]{String.valueOf(currency.getId())});
	}

	public int update(String newSymbol, String oldSymbol) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_SYMBOL, newSymbol);
		return db.update(TABLE_NAME, values, COLUMN_SYMBOL + " = ?",
				new String[]{oldSymbol});
	}

	@Override
	public long add(Currency currency) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(currency);
		return db.insert(TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(TABLE_NAME, COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	@Override
	public void delete(long id, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();

		try {
			new ListsDS(mContext).changeCurrency(id, newId);
			delete(id);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private ContentValues createContentValues(Currency currency) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME, currency.getName());
		values.put(COLUMN_SYMBOL, currency.getSymbol());
		return values;
	}

	public static class CurrencyCursor extends DictionaryCursor<Currency> {
		public CurrencyCursor(Cursor cursor) {
			super(cursor);
		}

		public Currency getEntity() {
			long id = getLong(getColumnIndex(COLUMN_ID));
			String name = getString(getColumnIndex(COLUMN_NAME));
			String symbol = getString(getColumnIndex(COLUMN_SYMBOL));

			return new Currency(id, name, symbol);
		}
	}
}
