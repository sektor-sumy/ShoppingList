package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.db.entities.ShoppingListEntity;

public class ShoppingListTable {
    public static final String TABLE_NAME = "ShoppingList";

    public static final String COLUMN_ID_ITEM = "id_item";
    public static final String COLUMN_ID_LIST = "id_list";
    public static final String COLUMN_IS_BOUGHT = "is_bought";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "("
            + COLUMN_ID_ITEM + " INTEGER NOT NULL, "
            + COLUMN_ID_LIST + " INTEGER NOT NULL, "
            + COLUMN_IS_BOUGHT + " INTEGER NOT NULL, "
            + "FOREIGN KEY (" + COLUMN_ID_ITEM + ") REFERENCES " + ItemsTable.TABLE_NAME + " (" + ItemsTable.COLUMN_ID + "), "
            + "FOREIGN KEY (" + COLUMN_ID_LIST + ") REFERENCES " + ListsTable.TABLE_NAME + " (" + ListsTable.COLUMN_ID + "), "
            + "PRIMARY KEY (" + COLUMN_ID_ITEM + ", " + COLUMN_ID_LIST + ")"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(TABLE_CREATE);
        initialData(database);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    private static void initialData(SQLiteDatabase database){
        ArrayList<ShoppingListEntity> inList = new ArrayList<>();
        inList.add(new ShoppingListEntity(1, 1, false));
        inList.add(new ShoppingListEntity(2, 1, true));
        inList.add(new ShoppingListEntity(3, 1, true));
        inList.add(new ShoppingListEntity(4, 1, false));
        inList.add(new ShoppingListEntity(5, 1, false));
        inList.add(new ShoppingListEntity(6, 1, true));
        inList.add(new ShoppingListEntity(7, 1, true));
        inList.add(new ShoppingListEntity(8, 1, false));
        inList.add(new ShoppingListEntity(9, 1, false));
        inList.add(new ShoppingListEntity(10, 1, false));

        for(ShoppingListEntity sl : inList){
            ContentValues contentValue = new ContentValues();
            contentValue.put(COLUMN_ID_ITEM, sl.getIdItem());
            contentValue.put(COLUMN_ID_LIST, sl.getIdList());
            contentValue.put(COLUMN_IS_BOUGHT, sl.isBought());
            database.insert(TABLE_NAME, null, contentValue);
        }
    }
}
