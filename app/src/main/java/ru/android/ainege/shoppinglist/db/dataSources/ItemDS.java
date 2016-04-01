package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ITable;
import ru.android.ainege.shoppinglist.db.ITable.ICategories;
import ru.android.ainege.shoppinglist.db.ITable.IItemData;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ItemData;

public class ItemDS extends GenericDS<Item> implements ITable.IItems {

	public ItemDS(Context context) {
		super(context);
	}

	@Override
	public EntityCursor<ItemData> getAll() {
		return null;
	}

	public ItemCursor getWithData(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = "SELECT " + TABLE_NAME + ".*, " +
				IItemData.COLUMN_AMOUNT + ", " +
				IItemData.COLUMN_ID_UNIT + ", " +
				IItemData.COLUMN_PRICE + ", " +
				IItemData.COLUMN_ID_CATEGORY + ", " +
				IItemData.COLUMN_COMMENT +
				" FROM " + TABLE_NAME + " INNER JOIN " + IItemData.TABLE_NAME + " ON " +
				TABLE_NAME + "." + COLUMN_ID_DATA + " = " + IItemData.TABLE_NAME + "." + IItemData.COLUMN_ID +
				" WHERE " + TABLE_NAME + "." + COLUMN_ID + " = ? ";
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
		return new ItemCursor(cursor);
	}

	public ItemCursor getByName(String name) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, COLUMN_NAME + " like ?",
				new String[]{name}, null, null, null);
		return new ItemCursor(cursor);
	}

	public Cursor getNames(String substring) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + TABLE_NAME + ".*, " +
				ICategories.TABLE_NAME + "." + ICategories.COLUMN_COLOR +
				" FROM " + TABLE_NAME +
				" INNER JOIN " + IItemData.TABLE_NAME +
				" ON " + TABLE_NAME + "." + COLUMN_ID_DATA + " = " +
				IItemData.TABLE_NAME + "." + IItemData.COLUMN_ID +
				" INNER JOIN " + ICategories.TABLE_NAME +
				" ON " + IItemData.TABLE_NAME + "." + IItemData.COLUMN_ID_CATEGORY + " = " +
				ICategories.TABLE_NAME + "." + ICategories.COLUMN_ID +
				" WHERE " + COLUMN_NAME + " LIKE '%" + substring + "%'", null);
		return cursor;
	}

	@Override
	public int update(Item item) {
		new ItemDataDS(mContext).update(new ItemData(item));

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(item);
		return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
				new String[]{String.valueOf(item.getIdItemData())});
	}

	@Override
	public long add(Item item) {
		long idData = new ItemDataDS(mContext).add(new ItemData(item));
		item.setIdItemData(idData);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(item);
		return db.insert(TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(TABLE_NAME, COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(Item item) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME, item.getName());
		values.put(COLUMN_DEFAULT_IMAGE_PATH, item.getDefaultImagePath());
		values.put(COLUMN_IMAGE_PATH, item.getImagePath());
		values.put(COLUMN_ID_DATA, item.getIdItemData());
		return values;
	}

	public static class ItemCursor extends EntityCursor<Item> {
		public ItemCursor(Cursor cursor) {
			super(cursor);
		}

		public Item getEntity() {
			long id = getLong(getColumnIndex(COLUMN_ID));
			String name = getString(getColumnIndex(COLUMN_NAME));
			String defaultImagePath = getString(getColumnIndex(COLUMN_DEFAULT_IMAGE_PATH));
			String imagePath = getString(getColumnIndex(COLUMN_IMAGE_PATH));
			long idData = getLong(getColumnIndex(COLUMN_ID_DATA));

			Item item = new Item(id, name, defaultImagePath, imagePath, idData);

			if (getColumnIndex(IItemData.COLUMN_AMOUNT) != -1) {
				item.setAmount(getDouble(getColumnIndex(IItemData.COLUMN_AMOUNT)));
				item.setIdUnit(getLong(getColumnIndex(IItemData.COLUMN_ID_UNIT)));
				item.setPrice(getDouble(getColumnIndex(IItemData.COLUMN_PRICE)));
				item.setIdCategory(getLong(getColumnIndex(IItemData.COLUMN_ID_CATEGORY)));
				item.setComment(getString(getColumnIndex(IItemData.COLUMN_COMMENT)));
			}

			return item;
		}
	}
}
