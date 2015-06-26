package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.db.entities.ItemEntity;

public class ItemsTable {

    public static final String TABLE_NAME = "Items";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_ID_UNIT = "id_unit";
    public static final String COLUMN_PRICE = "price";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_AMOUNT + " REAL, "
            + COLUMN_ID_UNIT + " INTEGER, "
            + COLUMN_PRICE + " REAL, "
            + "FOREIGN KEY (" + COLUMN_ID_UNIT + ") REFERENCES " + UnitsTable.TABLE_NAME + " (" + UnitsTable.COLUMN_ID + ")"
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
        ArrayList<ItemEntity> items = new ArrayList<>();
        items.add(new ItemEntity("молоко"));
        items.add(new ItemEntity("хлеб", 1, 1));
        items.add(new ItemEntity("масло сливочное", 2, 1, 70));
        items.add(new ItemEntity("икра мойвы"));
        items.add(new ItemEntity("черный чай", 75));
        items.add(new ItemEntity("молоко", 2, 3));
        items.add(new ItemEntity("хлеб"));
        items.add(new ItemEntity("масло сливочное", 1, 1, 70));
        items.add(new ItemEntity("икра мойвы"));
        items.add(new ItemEntity("черный чай", 75));

        for(ItemEntity item : items) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(COLUMN_NAME, item.getName());
            contentValue.put(COLUMN_AMOUNT, item.getAmount());
            contentValue.put(COLUMN_ID_UNIT, item.getIdUnit());
            contentValue.put(COLUMN_PRICE, item.getPrice());
            database.insert(TABLE_NAME, null, contentValue);
        }
    }
}
