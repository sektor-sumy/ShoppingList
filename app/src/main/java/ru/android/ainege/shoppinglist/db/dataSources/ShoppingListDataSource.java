package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.ItemEntity;
import ru.android.ainege.shoppinglist.db.entities.ShoppingListEntity;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;

public class ShoppingListDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public ShoppingListDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public ArrayList<ShoppingListEntity> getAllForList(int idList) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ShoppingListTable.TABLE_NAME,
                                 new String[]{ShoppingListTable.COLUMN_ID_ITEM, ShoppingListTable.COLUMN_ID_LIST, ShoppingListTable.COLUMN_IS_BOUGHT},
                                 ShoppingListTable.COLUMN_ID_LIST + "=?",
                                 new String[]{String.valueOf(idList)}, null, null, null, null);
        ArrayList<ShoppingListEntity> itemsInList = new ArrayList<>();
        if(cursor.moveToFirst()) {
            do{
                ShoppingListEntity itemInList = new ShoppingListEntity(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2));
                if(itemInList.getItem() == null) {
                    ItemDataSource itemDS = new ItemDataSource(mContext);
                    ItemEntity item = itemDS.get(itemInList.getIdItem(), true);
                    itemInList.setItem(item);
                }
                itemsInList.add(itemInList);
            } while(cursor.moveToNext());
            cursor.close();
        }
        return itemsInList;
    }

    public int update(ShoppingListEntity itemInList) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ShoppingListTable.COLUMN_IS_BOUGHT, itemInList.isBought());
        return db.update(ShoppingListTable.TABLE_NAME,
                         values,
                         ShoppingListTable.COLUMN_ID_ITEM + " = ? AND " + ShoppingListTable.COLUMN_ID_LIST + " = ?",
                         new String[] { String.valueOf(itemInList.getIdItem()), String.valueOf(itemInList.getIdList())});
    }

    public int update(ShoppingListEntity itemInList, boolean withItem) {
        int i = update(itemInList);
        if(i !=0 && withItem == true){
            ItemDataSource itemDS = new ItemDataSource(mContext);
            i = itemDS.update(itemInList.getItem());
        }
        return i;
    }

}
