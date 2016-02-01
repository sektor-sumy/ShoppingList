package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.db.tables.ItemDataTable;

public class ItemDataDS extends GenericDS<ItemData> {

	public ItemDataDS(Context context) {
		super(context);
	}

	public boolean isUnitUsed(long idUnit) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ItemDataTable.TABLE_NAME, null, ItemDataTable.COLUMN_ID_UNIT + " = " + idUnit,
				null, null, null, null);
		ItemDataCursor dataCursor = new ItemDataCursor(cursor);
		boolean result = dataCursor.getCount() > 0;
		cursor.close();
		return result;
	}

	public boolean isCategoryUsed(long idCategory) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ItemDataTable.TABLE_NAME, null, ItemDataTable.COLUMN_ID_CATEGORY + " = " + idCategory,
				null, null, null, null);
		ItemDataCursor dataCursor = new ItemDataCursor(cursor);
		boolean result = dataCursor.getCount() > 0;
		cursor.close();
		return result;
	}

	@Override
	public int update(ItemData data) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(data);
		return db.update(ItemDataTable.TABLE_NAME, values, ItemDataTable.COLUMN_ID + " = ?",
				new String[]{String.valueOf(data.getId())});
	}

	public void updateUnit(long oldId, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ItemDataTable.COLUMN_ID_UNIT, newId);
		db.update(ItemDataTable.TABLE_NAME, values,
				ItemDataTable.COLUMN_ID_UNIT + " = ?",
				new String[]{String.valueOf(oldId)});
	}

	@Override
	public long add(ItemData data) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(data);
		return db.insert(ItemDataTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(ItemDataTable.TABLE_NAME,
				ItemDataTable.COLUMN_ID + " = ?",
				new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(ItemData data) {
		ContentValues values = new ContentValues();

		values.put(ItemDataTable.COLUMN_ID_UNIT, data.getIdUnit());
		values.put(ItemDataTable.COLUMN_ID_CATEGORY, data.getIdCategory());

		if (data.getAmount() != -1) {
			values.put(ItemDataTable.COLUMN_AMOUNT, data.getAmount());
		}

		if (data.getPrice() != -1) {
			values.put(ItemDataTable.COLUMN_PRICE, data.getPrice());
		}

		if (data.getComment() != null) {
			values.put(ItemDataTable.COLUMN_COMMENT, data.getComment());
		}

		return values;
	}

	public static class ItemDataCursor extends GenericDS.EntityCursor<ItemData> {
		public ItemDataCursor(Cursor cursor) {
			super(cursor);
		}

		public ItemData getEntity() {
			long id = getLong(getColumnIndex(ItemDataTable.COLUMN_ID));
			double amount = getDouble(getColumnIndex(ItemDataTable.COLUMN_AMOUNT));
			long idUnit = getLong(getColumnIndex(ItemDataTable.COLUMN_ID_UNIT));
			double price = getDouble(getColumnIndex(ItemDataTable.COLUMN_PRICE));
			long idCategory = getLong(getColumnIndex(ItemDataTable.COLUMN_ID_CATEGORY));
			String comment = getString(getColumnIndex(ItemDataTable.COLUMN_COMMENT));

			return new ItemData(id, amount, idUnit, price, idCategory, comment);
		}
	}
}
