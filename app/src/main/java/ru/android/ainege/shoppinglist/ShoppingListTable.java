package ru.android.ainege.shoppinglist;

import android.database.sqlite.SQLiteDatabase;

public class ShoppingListTable {
    public static final String TABLE_NAME = "ShoppingList";

    public static final String COLUMN_ID_ITEM_FK = "id_item_fk";
    public static final String COLUMN_ID_LIST_FK = "id_list_fk";
    public static final String COLUMN_IS_BOUGHT = "is_bought";

    private static final String DATABASE_CREATE = "create table " + TABLE_NAME
            + "("
            + COLUMN_ID_ITEM_FK + " integer not null, "
            + COLUMN_ID_LIST_FK + " integer not null, "
            + COLUMN_IS_BOUGHT + " integer not null, "
            + "FOREIGN KEY (" + COLUMN_ID_ITEM_FK + ") REFERENCES " + ItemsTable.TABLE_NAME + " (" + ItemsTable.COLUMN_ID + "), "
            + "FOREIGN KEY (" + COLUMN_ID_LIST_FK + ") REFERENCES " + ListsTable.TABLE_NAME + " (" + ListsTable.COLUMN_ID + "), "
            + "primary key (" + COLUMN_ID_ITEM_FK + ", " + COLUMN_ID_LIST_FK + ")"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
}
