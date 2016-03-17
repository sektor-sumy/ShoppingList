package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.tables.CategoriesTable;
import ru.android.ainege.shoppinglist.db.tables.ItemDataTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;

public class CategoriesDS extends DictionaryDS<Category> {

	public CategoriesDS(Context context) {
		super(context);
	}

	public static CategoryCursor getAll(SQLiteDatabase db) {
		Cursor cursor = db.query(CategoriesTable.TABLE_NAME, null, null,
				null, null, null, CategoriesTable.COLUMN_NAME);
		return new CategoryCursor(cursor);
	}

	@Override
	public EntityCursor<Category> getAll() {
		return getAll(mDbHelper.getReadableDatabase());
	}

	@Override
	public CategoryCursor getAll(long withoutId) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(CategoriesTable.TABLE_NAME, null, CategoriesTable.COLUMN_ID + " != " + withoutId,
				null, null, null, CategoriesTable.COLUMN_NAME);
		return new CategoryCursor(cursor);
	}

	public CategoryCursor getCategoriesInList(long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + CategoriesTable.TABLE_NAME + ".*" +
				" FROM " + CategoriesTable.TABLE_NAME +
				" INNER JOIN " + ItemDataTable.TABLE_NAME +
				" ON " + CategoriesTable.TABLE_NAME + "." + CategoriesTable.COLUMN_ID + " = " + ItemDataTable.TABLE_NAME + "." + ItemDataTable.COLUMN_ID_CATEGORY +
				" INNER JOIN " + ShoppingListTable.TABLE_NAME +
				" ON " + ItemDataTable.TABLE_NAME + "." + ItemDataTable.COLUMN_ID + " = " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_DATA +
				" WHERE " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_LIST + " = ?" +
				" GROUP BY " + CategoriesTable.TABLE_NAME + "." + CategoriesTable.COLUMN_NAME +
				" ORDER BY " + CategoriesTable.COLUMN_NAME;
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(idList)});
		return new CategoryCursor(cursor);
	}

	@Override
	public boolean isUsed(long idCategory) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ItemDataTable.TABLE_NAME, null, ItemDataTable.COLUMN_ID_CATEGORY + " = " + idCategory,
				null, null, null, null);
		boolean result = cursor.getCount() > 0;
		cursor.close();
		return result;
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
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(CategoriesTable.TABLE_NAME, CategoriesTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	@Override
	public void delete(long id, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			new ItemDataDS(mContext).changeCategory(id, newId);

			delete(id);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private ContentValues createContentValues(Category category) {
		ContentValues values = new ContentValues();
		values.put(CategoriesTable.COLUMN_NAME, category.getName());
		values.put(CategoriesTable.COLUMN_COLOR, category.getColor());
		return values;
	}

	public static class CategoryCursor extends DictionaryCursor<Category> {
		private ShoppingListCursor mItemsInListCursor;

		public CategoryCursor(Cursor cursor) {
			super(cursor);
		}

		public ShoppingListCursor getItemsInListCursor() {
			return mItemsInListCursor;
		}

		public void setItemsInListCursor(ShoppingListCursor itemsInListCursor) {
			mItemsInListCursor = itemsInListCursor;
		}

		public Category getEntity() {
			long id = getLong(getColumnIndex(CategoriesTable.COLUMN_ID));
			String name = getString(getColumnIndex(CategoriesTable.COLUMN_NAME));
			int color = getInt(getColumnIndex(CategoriesTable.COLUMN_COLOR));

			Category category = new Category(id, name, color);

			if (mItemsInListCursor != null) {
				ArrayList<ShoppingList> itemsInCategory = new ArrayList<>();

				for (ShoppingList item : mItemsInListCursor.getEntities()) {
					if (category.getId() == item.getIdCategory()) {
						item.setCategory(category);
						itemsInCategory.add(item);
					}
				}

				ShoppingList.sort(itemsInCategory);
				category.setItemsByCategoryInList(itemsInCategory);
			}

			return category;
		}
	}
}
