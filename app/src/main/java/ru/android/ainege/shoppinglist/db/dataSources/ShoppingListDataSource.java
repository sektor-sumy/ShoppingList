package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class ShoppingListDataSource {
    public final static String ALPHABET = "alphabet";
    public final static String UP_PRICE = "upPrice";
    public final static String DOWN_PRICE = "downPrice";
    public final static String ORDER_ADDING = "orderAdding";

    private static ShoppingListDataSource instance;

    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;
    public boolean mIsBoughtEndInList;
    public String mType;
    private String order;

    private ShoppingListDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public static ShoppingListDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new ShoppingListDataSource(context);
        }

        return instance;
    }

    public static ShoppingListDataSource getInstance() {
        if (instance == null) {
            throw new NullPointerException();
        }

        return instance;
    }

    public void setSortSettings(boolean isBoughtFirst, String type) {
        mIsBoughtEndInList = isBoughtFirst;
        mType = type;
        order = getOrder(mIsBoughtEndInList, mType);
    }

    public ShoppingListCursor getItemsInList(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String selectQuery = "SELECT " + ShoppingListTable.COLUMN_ID_ITEM + ", " +
                ShoppingListTable.COLUMN_IS_BOUGHT + ", " +
                ItemsTable.COLUMN_NAME + ", " +
                ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_AMOUNT + ", " +
                ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_UNIT + ", " +
                UnitsTable.COLUMN_NAME + ", " +
                ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_PRICE + ", " +
                ShoppingListTable.COLUMN_DATE +
                " FROM " + ShoppingListTable.TABLE_NAME + " INNER JOIN " + ItemsTable.TABLE_NAME + " ON " +
                ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM + " = " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID +
                " LEFT JOIN " + UnitsTable.TABLE_NAME + " ON " +
                ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_UNIT + " = " + UnitsTable.TABLE_NAME + "." + UnitsTable.COLUMN_ID +
                " WHERE " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_LIST + " = ? " +
                " ORDER BY " + order;
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(id)});
        return new ShoppingListCursor(cursor);
    }


    public ShoppingListCursor existItemInList(String name, long idList) { //// TODO: 20.10.2015
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + ShoppingListTable.TABLE_NAME + " INNER JOIN " + ItemsTable.TABLE_NAME +
                " ON " + ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_ID_ITEM + " = " + ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_ID +
                " where " + ItemsTable.COLUMN_NAME + " like '" + name +
                "' AND " + ShoppingListTable.COLUMN_ID_LIST + " = " + idList, null);
        return new ShoppingListCursor(cursor);
    }

    public int setIsBought(boolean isBought, long idItem, long idList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(isBought);
        return db.update(ShoppingListTable.TABLE_NAME,
                values,
                ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
                new String[]{String.valueOf(idItem), String.valueOf(idList)});
    }

    public int update(ShoppingList shoppingList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(shoppingList);
        return db.update(ShoppingListTable.TABLE_NAME, values,
                ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
                new String[]{String.valueOf(shoppingList.getIdItem()), String.valueOf(shoppingList.getIdList())});
    }

    public long add(ShoppingList shoppingList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = createContentValues(shoppingList);
        values.put(ShoppingListTable.COLUMN_ID_LIST, shoppingList.getIdList());
        values.put(ShoppingListTable.COLUMN_DATE, String.valueOf(shoppingList.getDate()));
        return db.insert(ShoppingListTable.TABLE_NAME, null, values);
    }

    public void delete(long idItem, long idList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ShoppingListTable.TABLE_NAME,
                ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
                new String[]{String.valueOf(idItem), String.valueOf(idList)});
    }

    public void deleteAllBought(long idList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(ShoppingListTable.TABLE_NAME,
                ShoppingListTable.COLUMN_IS_BOUGHT + " = 1 AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
                new String[]{String.valueOf(idList)});
    }

    private ContentValues createContentValues(boolean isBought) {
        ContentValues values = new ContentValues();
        values.put(ShoppingListTable.COLUMN_IS_BOUGHT, isBought);
        return values;
    }

    private ContentValues createContentValues(ShoppingList shoppingList) {
        ContentValues values = createContentValues(shoppingList.isBought());
        values.put(ShoppingListTable.COLUMN_AMOUNT, shoppingList.getAmount());
        values.put(ShoppingListTable.COLUMN_ID_UNIT, shoppingList.getIdUnit());
        values.put(ShoppingListTable.COLUMN_PRICE, shoppingList.getPrice());
        values.put(ShoppingListTable.COLUMN_ID_ITEM, shoppingList.getIdItem());
        return values;
    }

    public static class ShoppingListCursor extends CursorWrapper {
        public ShoppingListCursor(Cursor cursor) {
            super(cursor);
        }

        public ShoppingList getItem() {
            long idItem = getLong(getColumnIndex(ShoppingListTable.COLUMN_ID_ITEM));
            boolean isBought = getInt(getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) == 1;
            double amount = getDouble(getColumnIndex(ShoppingListTable.COLUMN_AMOUNT));
            double price = getDouble(getColumnIndex(ShoppingListTable.COLUMN_PRICE));
            long date = getLong(getColumnIndex(ShoppingListTable.COLUMN_DATE));

            ShoppingList shoppingList = new ShoppingList(idItem, isBought, amount, price, new Date(date));

            if (getColumnIndex(ItemsTable.COLUMN_NAME) != -1) {
                String nameItem = getString(getColumnIndex(ItemsTable.COLUMN_NAME));
                long idUnit = getLong(getColumnIndex(ShoppingListTable.COLUMN_ID_UNIT));
                String nameUnit = getString(getColumnIndex(UnitsTable.COLUMN_NAME));

                shoppingList.setItem(new Item(idItem, nameItem));
                shoppingList.setUnit(new Unit(idUnit, nameUnit));
            }

            return shoppingList;
        }

        public ArrayList<ShoppingList> getItemsAsList(){
            ArrayList<ShoppingList> itemsInList = new ArrayList<>();
            moveToFirst();
            do {
                itemsInList.add(getItem());
            } while (moveToNext());
            return itemsInList;
        }
    }

    private String getOrder(boolean isBoughtFirst, String type) {
        String order = "";
        if (isBoughtFirst) {
            order += ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_IS_BOUGHT + ", ";
        }
        switch (type) {
            case ALPHABET:
                order += ItemsTable.TABLE_NAME + "." + ItemsTable.COLUMN_NAME;
                break;
            case UP_PRICE:
                order += ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_PRICE;
                break;
            case DOWN_PRICE:
                order += ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_PRICE + " DESC";
                break;
            case ORDER_ADDING:
                order += ShoppingListTable.TABLE_NAME + "." + ShoppingListTable.COLUMN_DATE;
                break;
        }
        return order;
    }
}
