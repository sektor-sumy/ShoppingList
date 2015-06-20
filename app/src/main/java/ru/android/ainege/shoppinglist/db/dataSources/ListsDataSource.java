package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.ListEntity;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;

public class ListsDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public ListsDataSource(Context context){
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public ListEntity get(int id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(ListsTable.TABLE_NAME,
                                 null,
                                 ListsTable.COLUMN_ID + "=?",
                                 new String[] { String.valueOf(id) }, null, null, null, null);
        cursor.moveToFirst();
        ListEntity list = new ListEntity(cursor.getInt(0), cursor.getString(1));
        cursor.close();
        return list;
    }

    public ListEntity get(int id, boolean withItems) {
        ListEntity list = get(id);
        if(list.getItemsInList() == null && withItems) {
            ShoppingListDataSource shoppingListDataSource = new ShoppingListDataSource(mContext);
            list.setItemsInList(shoppingListDataSource.getAllForList(list.getId()));
        }
        return list;
    }

}
