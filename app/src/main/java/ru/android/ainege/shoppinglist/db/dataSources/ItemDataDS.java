package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.db.tables.ItemDataTable;

public class ItemDataDS extends GenericDS<ItemData> {

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
		return db.update(ItemDataTable.TABLE_NAME, values, ItemDataTable.COLUMN_ID + " = ?",
				new String[]{String.valueOf(data.getIdItemData())});
	}

	public void changeUnit(long oldId, long newId) {
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
}
