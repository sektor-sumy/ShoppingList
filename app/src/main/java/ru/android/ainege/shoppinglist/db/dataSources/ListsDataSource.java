package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;

public class ListsDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public ListsDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public ListCursor get(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ListsTable.TABLE_NAME, null, ListsTable.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        return new ListCursor(cursor);
    }

    public ListCursor getAll() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ListsTable.TABLE_NAME, null, null, null, null, null, null, null);
        return new ListCursor(cursor);
    }

    public int update(List list) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(list.getName());
        return db.update(ListsTable.TABLE_NAME, values, ListsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(list.getId())});
    }

    public long add(String name) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name);
        return db.insert(ListsTable.TABLE_NAME, null, values);
    }

    public void delete(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ListsTable.TABLE_NAME, ListsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
    }

    private ContentValues createContentValues(String name) {
        ContentValues values = new ContentValues();
        values.put(ListsTable.COLUMN_NAME, name);
        return values;
    }

    public static class ListCursor extends CursorWrapper {
        public ListCursor(Cursor cursor) {
            super(cursor);
        }

        public List getList() {
            long id = getLong(getColumnIndex(ListsTable.COLUMN_ID));
            String name = getString(getColumnIndex(ListsTable.COLUMN_NAME));
            long idCurrency = getLong(getColumnIndex(ListsTable.COLUMN_ID_CURRENCY));

            return new List(id, name, idCurrency);
        }
    }
}
