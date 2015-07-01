package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.ItemEntity;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class ItemDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public ItemDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public ItemEntity get(int id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ItemsTable.TABLE_NAME,
                                 null,
                                 ItemsTable.COLUMN_ID + "=?",
                                 new String[] { String.valueOf(id) }, null, null, null, null);
        cursor.moveToFirst();
        ItemEntity item = new ItemEntity(cursor.getInt(0),
                                         cursor.getString(1),
                                         cursor.getInt(2),
                                         cursor.getInt(3),
                                         cursor.getDouble(4));
        cursor.close();
        return item;
    }

    public ItemEntity get(int id, boolean withUnit) {
        ItemEntity item = get(id);
        if(item.getIdUnit() != UnitsTable.ID_NULL && withUnit) {
            UnitsDataSource unitDS = new UnitsDataSource(mContext);
            item.setUnit(unitDS.get(item.getIdUnit()));
        }
        return item;
    }

    public int update(ItemEntity item) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ItemsTable.COLUMN_NAME, item.getName());
        if(item.getAmount() != 0) {
            values.put(ItemsTable.COLUMN_AMOUNT, item.getAmount());
            values.put(ItemsTable.COLUMN_ID_UNIT, item.getIdUnit());
        } else {
            values.put(ItemsTable.COLUMN_AMOUNT, 0);
            values.put(ItemsTable.COLUMN_ID_UNIT, UnitsTable.ID_NULL);
        }
        values.put(ItemsTable.COLUMN_PRICE, item.getPrice());
        return db.update(ItemsTable.TABLE_NAME,
                values,
                ItemsTable.COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    public long add(ItemEntity item) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ItemsTable.COLUMN_NAME, item.getName());
        if(item.getAmount() != 0) {
            values.put(ItemsTable.COLUMN_AMOUNT, item.getAmount());
            values.put(ItemsTable.COLUMN_ID_UNIT, item.getIdUnit());
        } else {
            values.put(ItemsTable.COLUMN_AMOUNT, 0);
            values.put(ItemsTable.COLUMN_ID_UNIT, UnitsTable.ID_NULL);
        }
        values.put(ItemsTable.COLUMN_PRICE, item.getPrice());

        long id = db.insert(ItemsTable.TABLE_NAME, null, values);
        db.close();
        return id;
    }
}
