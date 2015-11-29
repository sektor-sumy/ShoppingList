package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class UnitsDataSource extends DictionaryDataSource<Unit> {

	public UnitsDataSource(Context context) {
		super(context);
	}

	@Override
	public UnitCursor getAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("Select " + UnitsTable.TABLE_NAME + ".* from " + UnitsTable.TABLE_NAME +
				" ORDER BY " + UnitsTable.TABLE_NAME + "." + UnitsTable.COLUMN_NAME, null);
		return new UnitCursor(cursor);
	}

	public long getRandomId(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(UnitsTable.TABLE_NAME, null, UnitsTable.COLUMN_ID + " != " + id,
				null, null, null, UnitsTable.COLUMN_NAME, "1");
		UnitCursor unit = new UnitCursor(cursor);
		unit.moveToFirst();
		return unit.getEntity().getId();
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
		ShoppingListDataSource.getInstance().updateUnit(id, getRandomId(id));

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(UnitsTable.TABLE_NAME, UnitsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(Unit unit) {
		ContentValues values = new ContentValues();
		values.put(UnitsTable.COLUMN_NAME, unit.getName());
		return values;
	}

	public static class UnitCursor extends EntityCursor<Unit> {
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
