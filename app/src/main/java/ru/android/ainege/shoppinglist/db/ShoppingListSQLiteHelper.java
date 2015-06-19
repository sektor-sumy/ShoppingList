package ru.android.ainege.shoppinglist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class ShoppingListSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shoppingList.db";
    private static final int DATABASE_VERSION = 1;

    public ShoppingListSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        UnitsTable.onCreate(sqLiteDatabase);
        ListsTable.onCreate(sqLiteDatabase);
        ItemsTable.onCreate(sqLiteDatabase);
        ShoppingListTable.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }


}
