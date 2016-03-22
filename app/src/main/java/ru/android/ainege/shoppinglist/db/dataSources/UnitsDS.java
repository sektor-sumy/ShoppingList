package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.db.tables.ItemDataTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class UnitsDS extends DictionaryDS<Unit> {

	public UnitsDS(Context context) {
		super(context);
	}

	public static UnitCursor getAll(SQLiteDatabase db) {
		Cursor cursor = db.query(UnitsTable.TABLE_NAME, null, null,
				null, null, null, UnitsTable.COLUMN_NAME);
		return new UnitCursor(cursor);
	}

	@Override
	public UnitCursor getAll() {
		return getAll(mDbHelper.getReadableDatabase());
	}

	public UnitCursor getAllForSpinner() {
		SQLiteDatabase  db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(UnitsTable.TABLE_NAME, null, null,
				null, null, null, UnitsTable.COLUMN_NAME);

		MatrixCursor extras = new MatrixCursor(new String[] { UnitsTable.COLUMN_ID, UnitsTable.COLUMN_NAME });
		extras.addRow(new String[] { "-1", "Добавить" });
		Cursor[] cursors = { extras, cursor };

		return new UnitCursor(new MergeCursor(cursors));
	}

	@Override
	public UnitCursor getAll(long withoutId) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(UnitsTable.TABLE_NAME, null, UnitsTable.COLUMN_ID + " != " + withoutId,
				null, null, null, UnitsTable.COLUMN_NAME);
		return new UnitCursor(cursor);
	}

	@Override
	public boolean isUsed(long idUnit) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ItemDataTable.TABLE_NAME, null, ItemDataTable.COLUMN_ID_UNIT + " = " + idUnit,
				null, null, null, null);
		boolean result = cursor.getCount() > 0;
		cursor.close();
		return result;
	}

	@Override
	public int update(Unit unit) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(unit);
		return db.update(UnitsTable.TABLE_NAME, values, UnitsTable.COLUMN_ID + " = ?",
				new String[]{String.valueOf(unit.getId())});
	}

	@Override
	public long add(Unit unit) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(unit);
		return db.insert(UnitsTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(UnitsTable.TABLE_NAME, UnitsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	@Override
	public void delete(long id, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();

		try {
			new ItemDataDS(mContext).changeUnit(id, newId);
			delete(id);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private ContentValues createContentValues(Unit unit) {
		ContentValues values = new ContentValues();
		values.put(UnitsTable.COLUMN_NAME, unit.getName());
		return values;
	}

	public static class UnitCursor extends DictionaryCursor<Unit> {
		public UnitCursor(Cursor cursor) {
			super(cursor);
		}

		public Unit getEntity() {
			long id = getLong(getColumnIndex(UnitsTable.COLUMN_ID));
			String name = getString(getColumnIndex(UnitsTable.COLUMN_NAME));

			return new Unit(id, name);
		}
	}
}
