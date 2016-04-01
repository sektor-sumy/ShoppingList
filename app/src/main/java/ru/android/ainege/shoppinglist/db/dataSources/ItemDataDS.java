package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ITable;
import ru.android.ainege.shoppinglist.db.entities.ItemData;

public class ItemDataDS extends GenericDS<ItemData> implements ITable.IItemData{

	public ItemDataDS(Context context) {
		super(context);
	}

	@Override
	public EntityCursor<ItemData> getAll() {
		return null;
	}

	@Override
	public int update(ItemData data) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(data);
		return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
				new String[]{String.valueOf(data.getIdItemData())});
	}

	public void changeUnit(long oldId, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_ID_UNIT, newId);
		db.update(TABLE_NAME, values,
				COLUMN_ID_UNIT + " = ?",
				new String[]{String.valueOf(oldId)});
	}

	public void changeCategory(long oldId, long newId) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_ID_CATEGORY, newId);
		db.update(TABLE_NAME, values,
				COLUMN_ID_CATEGORY + " = ?",
				new String[]{String.valueOf(oldId)});
	}

	@Override
	public long add(ItemData data) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(data);
		return db.insert(TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(TABLE_NAME,
				COLUMN_ID + " = ?",
				new String[]{String.valueOf(id)});
	}

	private ContentValues createContentValues(ItemData data) {
		ContentValues values = new ContentValues();

		values.put(COLUMN_ID_UNIT, data.getIdUnit());
		values.put(COLUMN_ID_CATEGORY, data.getIdCategory());

		if (data.getAmount() != -1) {
			values.put(COLUMN_AMOUNT, data.getAmount());
		}

		if (data.getPrice() != -1) {
			values.put(COLUMN_PRICE, data.getPrice());
		}

		if (data.getComment() != null) {
			values.put(COLUMN_COMMENT, data.getComment());
		}

		return values;
	}
}
