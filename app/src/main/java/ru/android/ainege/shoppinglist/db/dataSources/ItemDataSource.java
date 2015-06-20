package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.ItemEntity;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;

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
        if(item.getIdUnit() != 0 && withUnit) {
            UnitsDataSource unitsDataSource = new UnitsDataSource(mContext);
            item.setUnit(unitsDataSource.get(item.getIdUnit()));
        }
        return item;
    }
}
