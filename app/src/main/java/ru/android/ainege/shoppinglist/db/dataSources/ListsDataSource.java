package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class ListsDataSource {
    public final static String ALPHABET = "alphabet";
    public final static String UP_PRICE = "upPrice";
    public final static String DOWN_PRICE = "downPrice";
    public final static String ORDER_ADDING = "orderAdding";

    private static ListsDataSource instance;

    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;
    public boolean mIsBoughtFirst;
    public String mType;
    private String order;

    private ListsDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public static ListsDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new ListsDataSource(context);
        }

        return instance;
    }

    public static ListsDataSource getInstance() {
        if (instance == null) {
            throw new NullPointerException();
        }

        return instance;
    }

    public void setSortSettings(boolean isBoughtFirst, String type) {
        mIsBoughtFirst = isBoughtFirst;
        mType = type;
        order = getOrder(mIsBoughtFirst, mType);
    }

    public Cursor get(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ListsTable.TABLE_NAME,
                null,
                ListsTable.COLUMN_ID + "=?",
                new String[] {String.valueOf(id)}, null, null, null, null);
        return cursor.moveToFirst() ? cursor : null;
    }

    public Cursor getItemsInList(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String selectQuery = "SELECT " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID + ", " +
                                       ShoppingListTable.COLUMN_IS_BOUGHT + ", " +
                                       ItemsTable.COLUMN_NAME + ", " +
                                       ItemsTable.COLUMN_AMOUNT + ", " +
                                       UnitsTable.COLUMN_NAME + ", " +
                                       ItemsTable.COLUMN_PRICE +
                            " FROM " +  ShoppingListTable.TABLE_NAME + " INNER JOIN " + ItemsTable.TABLE_NAME + " ON " +
                                        ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM + " = " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID +
                                        " LEFT JOIN " + UnitsTable.TABLE_NAME + " ON " +
                                        ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID_UNIT + " = " + UnitsTable.TABLE_NAME + "." + UnitsTable.COLUMN_ID +
                            " WHERE " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_LIST + " = ? " +
                            " ORDER BY " + order;
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
        return cursor.moveToFirst() ? cursor : null;
    }

    public Cursor getAll() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ListsTable.TABLE_NAME, null, null, null, null, null, null, null);
        return cursor.moveToFirst() ? cursor : null;
    }

    public long add(String name) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name);
        return db.insert(ListsTable.TABLE_NAME, null, values);
    }

    public void delete(long id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ListsTable.TABLE_NAME,
                ListsTable.COLUMN_ID + " = ? ",
                new String[] { String.valueOf(id) });
    }

    public int update(long id, String name) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(name);
        return db.update(ListsTable.TABLE_NAME, values, ListsTable.COLUMN_ID + " = ? ", new String[] {String.valueOf(id) });
    }

    private ContentValues createContentValues(String name) {
        ContentValues values = new ContentValues();
        values.put(ListsTable.COLUMN_NAME, name);
        return values;
    }

    private String getOrder(boolean isBoughtFirst, String type){
        String order = "";
        if (isBoughtFirst) {
            order += ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_IS_BOUGHT + ", ";
        }
        switch(type) {
            case ALPHABET:
                order += ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_NAME;
                break;
            case UP_PRICE:
                order += ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_PRICE;
                break;
            case DOWN_PRICE:
                order += ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_PRICE + " DESC";
                break;
            case ORDER_ADDING:
                order += ItemsTable.TABLE_NAME + "." + BaseColumns._ID;
                break;
        }
        return order;
    }
}
