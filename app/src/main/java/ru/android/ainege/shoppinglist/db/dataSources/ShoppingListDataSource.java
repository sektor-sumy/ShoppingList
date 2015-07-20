package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;

public class ShoppingListDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public ShoppingListDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public int setIsBought(boolean isBought, long idItem, long idList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(isBought);
        return db.update(ShoppingListTable.TABLE_NAME,
                         values,
                         ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
                         new String[] {String.valueOf(idItem), String.valueOf(idList)});
    }

    public long add(long idItem, long idList, boolean isBought) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(isBought);
        values.put(ShoppingListTable.COLUMN_ID_ITEM, idItem);
        values.put(ShoppingListTable.COLUMN_ID_LIST, idList);
        return db.insert(ShoppingListTable.TABLE_NAME, null, values);
    }

    public void delete(long idItem, long idList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ShoppingListTable.TABLE_NAME,
                ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
                new String[] { String.valueOf(idItem), String.valueOf(idList) });
    }

    private ContentValues createContentValues(boolean isBought) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListTable.COLUMN_IS_BOUGHT, isBought);
        return values;
    }
}
