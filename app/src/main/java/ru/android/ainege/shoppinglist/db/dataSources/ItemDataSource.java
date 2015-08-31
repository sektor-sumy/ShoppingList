package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;

public class ItemDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public ItemDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public Cursor getName(String name) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ItemsTable.TABLE_NAME, new String[]{ItemsTable.COLUMN_NAME}, ItemsTable.COLUMN_NAME + " like ?", new String[]{name}, null, null, null);
        return cursor.moveToFirst() ? cursor : null;
    }

    public Cursor getNames(String substring) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + ItemsTable.TABLE_NAME + " where " + ItemsTable.COLUMN_NAME + " like '" + substring + "%';", null);
        return cursor.moveToFirst() ? cursor : null;
    }

    public int update(String name, double amount, long idUnit, double price, long idItem) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name, amount, idUnit, price);
        return db.update(ItemsTable.TABLE_NAME,
                values,
                ItemsTable.COLUMN_ID + " = ?",
                new String[] {String.valueOf(idItem)});
    }

    public int updateName(String name, long idItem) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name);
        return db.update(ItemsTable.TABLE_NAME,
                values,
                ItemsTable.COLUMN_ID + " = ?",
                new String[] {String.valueOf(idItem)});
    }

    public long add(String name) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name);
        return db.insert(ItemsTable.TABLE_NAME, null, values);
    }

    public long add(String name, double amount, long idUnit, double price) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name, amount, idUnit, price);
        return db.insert(ItemsTable.TABLE_NAME, null, values);
    }

    private ContentValues createContentValues(String name) {
        ContentValues values = new ContentValues();
        values.put(ItemsTable.COLUMN_NAME, name);
        return values;
    }

    private ContentValues createContentValues(String name, double amount, long idUnit, double price) {
        ContentValues values = createContentValues(name);
        values.put(ItemsTable.COLUMN_AMOUNT, amount);
        values.put(ItemsTable.COLUMN_ID_UNIT, idUnit);
        values.put(ItemsTable.COLUMN_PRICE, price);
        return values;
    }
}
