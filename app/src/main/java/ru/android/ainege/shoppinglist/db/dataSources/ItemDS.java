package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.db.tables.CategoriesTable;
import ru.android.ainege.shoppinglist.db.tables.ItemDataTable;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;

public class ItemDS extends GenericDS<Item> {

	public ItemDS(Context context) {
		super(context);
	}

	//for db v1
	public static ItemCursor getAll(SQLiteDatabase db) {
		return new ItemDS.ItemCursor(db.query(ItemsTable.TABLE_NAME, null, null,
				null, null, null, null));
	}

	@Override
	public EntityCursor<ItemData> getAll() {
		return null;
	}

	public ItemCursor getWithData(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + ItemsTable.TABLE_NAME + ".*, " +
				ItemDataTable.COLUMN_AMOUNT + ", " +
				ItemDataTable.COLUMN_ID_UNIT + ", " +
				ItemDataTable.COLUMN_PRICE + ", " +
				ItemDataTable.COLUMN_ID_CATEGORY + ", " +
				ItemDataTable.COLUMN_COMMENT +
				" FROM " + ItemsTable.TABLE_NAME + " INNER JOIN " + ItemDataTable.TABLE_NAME + " ON " +
				ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID_DATA + " = " + ItemDataTable.TABLE_NAME + "." + ItemDataTable.COLUMN_ID +
				" WHERE " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID + " = ? ";
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
		return new ItemCursor(cursor);
	}

	public ItemCursor getByName(String name) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(ItemsTable.TABLE_NAME, null, ItemsTable.COLUMN_NAME + " like ?",
				new String[]{name}, null, null, null);
		return new ItemCursor(cursor);
	}

	public Cursor getNames(String substring) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + ItemsTable.TABLE_NAME + ".*, " +
				CategoriesTable.TABLE_NAME + "." + CategoriesTable.COLUMN_COLOR +
				" FROM " + ItemsTable.TABLE_NAME +
				" INNER JOIN " + ItemDataTable.TABLE_NAME +
				" ON " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID_DATA + " = " +
				ItemDataTable.TABLE_NAME + "." + ItemDataTable.COLUMN_ID +
				" INNER JOIN " + CategoriesTable.TABLE_NAME +
				" ON " + ItemDataTable.TABLE_NAME + "." + ItemDataTable.COLUMN_ID_CATEGORY + " = " +
				CategoriesTable.TABLE_NAME + "." + CategoriesTable.COLUMN_ID +
				" WHERE " + ItemsTable.COLUMN_NAME + " LIKE '%" + substring + "%'", null);
		return cursor;
	}

	@Override
	public int update(Item item) {
		new ItemDataDS(mContext).update(new ItemData(item));

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(item);
		return db.update(ItemsTable.TABLE_NAME, values, ItemsTable.COLUMN_ID + " = ?",
				new String[]{String.valueOf(item.getIdItemData())});
	}

	@Override
	public long add(Item item) {
		long idData = new ItemDataDS(mContext).add(new ItemData(item));
		item.setIdItemData(idData);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(item);
		return db.insert(ItemsTable.TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(ItemsTable.TABLE_NAME, ItemsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(Item item) {
		ContentValues values = new ContentValues();
		values.put(ItemsTable.COLUMN_NAME, item.getName());
		values.put(ItemsTable.COLUMN_DEFAULT_IMAGE_PATH, item.getDefaultImagePath());
		values.put(ItemsTable.COLUMN_IMAGE_PATH, item.getImagePath());
		values.put(ItemsTable.COLUMN_ID_DATA, item.getIdItemData());
		return values;
	}

	public static class ItemCursor extends EntityCursor<Item> {
		public ItemCursor(Cursor cursor) {
			super(cursor);
		}

		public Item getEntity() {
			long id = getLong(getColumnIndex(ItemsTable.COLUMN_ID));
			String name = getString(getColumnIndex(ItemsTable.COLUMN_NAME));
			String defaultImagePath = getString(getColumnIndex(ItemsTable.COLUMN_DEFAULT_IMAGE_PATH));
			String imagePath = getString(getColumnIndex(ItemsTable.COLUMN_IMAGE_PATH));
			long idData = getLong(getColumnIndex(ItemsTable.COLUMN_ID_DATA));

			Item item = new Item(id, name, defaultImagePath, imagePath, idData);

			if (getColumnIndex(ItemDataTable.COLUMN_AMOUNT) != -1) {
				item.setAmount(getDouble(getColumnIndex(ItemDataTable.COLUMN_AMOUNT)));
				item.setIdUnit(getLong(getColumnIndex(ItemDataTable.COLUMN_ID_UNIT)));
				item.setPrice(getDouble(getColumnIndex(ItemDataTable.COLUMN_PRICE)));
				item.setIdCategory(getLong(getColumnIndex(ItemDataTable.COLUMN_ID_CATEGORY)));
				item.setComment(getString(getColumnIndex(ItemDataTable.COLUMN_COMMENT)));
			}

			return item;
		}
	}
}
