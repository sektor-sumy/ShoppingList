package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class ItemDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public ItemDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public int update(String name, double amount, long idUnit, double price, long idItem) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name, amount, idUnit, price);
        return db.update(ItemsTable.TABLE_NAME,
                values,
                ItemsTable.COLUMN_ID + " = ?",
                new String[] {String.valueOf(idItem)});
    }

    public long add(String name, double amount, long idUnit, double price) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name, amount, idUnit, price);
        return db.insert(ItemsTable.TABLE_NAME, null, values);
    }

    private ContentValues createContentValues(String name, double amount, long idUnit, double price) {
        ContentValues values = new ContentValues();
        values.put(ItemsTable.COLUMN_NAME, name);
        if(amount != 0) {
            values.put(ItemsTable.COLUMN_AMOUNT, amount);
            values.put(ItemsTable.COLUMN_ID_UNIT, idUnit);
        } else {
            values.put(ItemsTable.COLUMN_AMOUNT, 0);
            values.put(ItemsTable.COLUMN_ID_UNIT, UnitsTable.ID_NULL);
        }
        values.put(ItemsTable.COLUMN_PRICE, price);
        return values;
    }
}
