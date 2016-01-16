package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.tables.CategoriesTable;

public class CategoriesDS extends DictionaryDS<Category> {

	public CategoriesDS(Context context) {
		super(context);
	}

	@Override
	public EntityCursor<Category> getAll() {
		return getAll(mDbHelper.getReadableDatabase());
	}

	public static CategoryCursor getAll(SQLiteDatabase db) {
		Cursor cursor = db.query(CategoriesTable.TABLE_NAME, null, null,
				null, null, null, CategoriesTable.COLUMN_NAME);
		return new CategoryCursor(cursor);
	}

	public long getRandomId(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(CategoriesTable.TABLE_NAME, null, CategoriesTable.COLUMN_ID + " != " + id,
				null, null, null, CategoriesTable.COLUMN_NAME, "1");
		CategoryCursor category = new CategoryCursor(cursor);
		category.moveToFirst();
		long selectedId = category.getEntity().getId();
		cursor.close();
		return selectedId;
	}

	@Override
	public int update(Category category) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(category);
		return db.update(CategoriesTable.TABLE_NAME, values, CategoriesTable.COLUMN_ID + " = ?",
				new String[]{String.valueOf(category.getId())});
	}

	@Override
	public long add(Category category) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(category);
		return db.insert(CategoriesTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		long newId = getRandomId(id);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			//TODO update category
			//new ListsDS(mContext).updateCategory(id, newId);

			db.delete(CategoriesTable.TABLE_NAME, CategoriesTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private ContentValues createContentValues(Category category) {
		ContentValues values = new ContentValues();
		values.put(CategoriesTable.COLUMN_NAME, category.getName());
		return values;
	}

	public static class CategoryCursor extends EntityCursor<Category> {
		public CategoryCursor(Cursor cursor) {
			super(cursor);
		}

		public Category getEntity() {
			long id = getLong(getColumnIndex(CategoriesTable.COLUMN_ID));
			String name = getString(getColumnIndex(CategoriesTable.COLUMN_NAME));

			return new Category(id, name);
		}
	}
}
