package ru.android.ainege.shoppinglist.db.tables;

import android.database.sqlite.SQLiteDatabase;

public class ListsTable {

    public static final String TABLE_NAME = "Lists";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";

    private static final String DATABASE_CREATE = "create table " + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
}
