package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.ListEntity;

public class ListsTable {

    public static final String TABLE_NAME = "Lists";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL"
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
        ListEntity[] lists = {
                new ListEntity("Мой список"),
                new ListEntity("Купить на ДР"),
        };

        for(ListEntity list : lists){
            ContentValues contentValue = new ContentValues();
            contentValue.put(COLUMN_NAME, list.getName());
            database.insert(TABLE_NAME, null, contentValue);
        }
    }
}
