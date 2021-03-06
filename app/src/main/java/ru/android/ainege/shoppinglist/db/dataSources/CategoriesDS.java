package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.TableInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.ItemDataInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.ShoppingListsInterface;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.view.spinners.GeneralSpinner;

public class CategoriesDS extends CatalogDS<Category> implements TableInterface.CategoriesInterface {

	public static CategoryCursor getAll(SQLiteDatabase db) {
		Cursor cursor = db.query(TABLE_NAME, null, null,
				null, null, null, COLUMN_NAME);

		return new CategoryCursor(cursor);
	}

	public static HashMap<String, Category> getCategories(SQLiteDatabase db) {
		ArrayList<Category> categoriesDB = getAll(db).getEntities();
		HashMap<String, Category> unit = new HashMap<>();

		for (Category c : categoriesDB) {
			unit.put(c.getName(), c);
		}

		return unit;
	}

	public CategoriesDS(Context context) {
		super(context);
	}

	@Override
	public CategoryCursor getAll() {
		return getAll(mDbHelper.getReadableDatabase());
	}

	@Override
	public CategoryCursor getAllForSpinner() {
		Cursor cursor = getAll();

		MatrixCursor extras = new MatrixCursor(new String[] { COLUMN_ID,
				COLUMN_NAME, COLUMN_COLOR });
		extras.addRow(new String[] { String.valueOf(GeneralSpinner.ID_ADD_CATALOG), mContext.getString(R.string.add), "0" });
		Cursor[] cursors = { extras, cursor };

		return new CategoryCursor(new MergeCursor(cursors));
	}

	@Override
	public CategoryCursor getAll(long withoutId) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " != " + withoutId,
				null, null, null, COLUMN_NAME);

		return new CategoryCursor(cursor);
	}

	public CategoryCursor getCategoriesInList(long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + TABLE_NAME + ".*" +
				" FROM " + TABLE_NAME +
				" INNER JOIN " + ItemDataInterface.TABLE_NAME +
				" ON " + TABLE_NAME + "." + COLUMN_ID + " = " + ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID_CATEGORY +
				" INNER JOIN " + ShoppingListsInterface.TABLE_NAME +
				" ON " + ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID + " = " + ShoppingListsInterface.TABLE_NAME + "." + ShoppingListsInterface.COLUMN_ID_DATA +
				" WHERE " + ShoppingListsInterface.TABLE_NAME + "." + ShoppingListsInterface.COLUMN_ID_LIST + " = ?" +
				" GROUP BY " + TABLE_NAME + "." + COLUMN_NAME +
				" ORDER BY " + COLUMN_NAME;
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(idList)});

		return new CategoryCursor(cursor);
	}

	@Override
	public boolean isUsed(long idCategory) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ItemDataInterface.TABLE_NAME, null, ItemDataInterface.COLUMN_ID_CATEGORY + " = " + idCategory,
				null, null, null, null);
		boolean result = cursor.getCount() > 0;
		cursor.close();

		return result;
	}

	@Override
	public int update(Category category) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(category);

		return db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(category.getId())});
	}

	@Override
	public long add(Category category) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(category);

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
			new ItemDataDS(mContext).changeCategory(id, newId);

			delete(id);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private ContentValues createContentValues(Category category) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME, category.getName());
		values.put(COLUMN_COLOR, category.getColor());

		return values;
	}

	public static class CategoryCursor extends CatalogCursor<Category> {
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
			long id = getLong(getColumnIndex(COLUMN_ID));
			String name = getString(getColumnIndex(COLUMN_NAME));
			int color = getInt(getColumnIndex(COLUMN_COLOR));

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
				category.setItemsByCategories(itemsInCategory);
			}

			return category;
		}
	}
}
