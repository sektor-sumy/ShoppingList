package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;

public class ItemDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public ItemDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public ItemCursor get(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ItemsTable.TABLE_NAME, null, ItemsTable.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        return new ItemCursor(cursor);
    }

    public ItemCursor getName(String name) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ItemsTable.TABLE_NAME, new String[]{ItemsTable.COLUMN_NAME},
                ItemsTable.COLUMN_NAME + " like ?", new String[]{name}, null, null, null);
        return new ItemCursor(cursor);
    }

    public ItemCursor getNames(String substring) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + ItemsTable.TABLE_NAME + " where " +
                ItemsTable.COLUMN_NAME + " like '" + substring + "%';", null);
        return new ItemCursor(cursor);
    }

    public int update(Item item) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(item);
        return db.update(ItemsTable.TABLE_NAME, values, ItemsTable.COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    public long add(Item item) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(item);
        return db.insert(ItemsTable.TABLE_NAME, null, values);
    }

    private ContentValues createContentValues(Item item) {
        ContentValues values = new ContentValues();
        values.put(ItemsTable.COLUMN_NAME, item.getName());
        if (item.getAmount() != -1) {
            values.put(ItemsTable.COLUMN_AMOUNT, item.getAmount());
            values.put(ItemsTable.COLUMN_ID_UNIT, item.getIdUnit());
        }
        if (item.getPrice() != -1) {
            values.put(ItemsTable.COLUMN_PRICE, item.getPrice());
        }
        return values;
    }

    public static class ItemCursor extends CursorWrapper {
        public ItemCursor(Cursor cursor) {
            super(cursor);
        }

        public Item getItem() {
            long id = getLong(getColumnIndex(ItemsTable.COLUMN_ID));
            String name = getString(getColumnIndex(ItemsTable.COLUMN_NAME));
            double amount = getDouble(getColumnIndex(ItemsTable.COLUMN_AMOUNT));
            long idUnit = getLong(getColumnIndex(ItemsTable.COLUMN_ID_UNIT));
            double price = getDouble(getColumnIndex(ItemsTable.COLUMN_PRICE));

            return new Item(id, name, amount, idUnit, price);
        }
    }
}
