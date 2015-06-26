package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.entities.UnitEntity;

public class UnitsTable {

    public static final String TABLE_NAME = "Units";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";

    public static final int ID_NULL = 0;

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

    private static void initialData(SQLiteDatabase database) {
        UnitEntity[] units = {
                new UnitEntity("шт"),
                new UnitEntity("кг"),
                new UnitEntity("л"),
        };

        for(UnitEntity unit : units) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(COLUMN_NAME, unit.getName());
            database.insert(TABLE_NAME, null, contentValue);
        }
    }

}
