package ru.android.ainege.shoppinglist.db.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class ItemsTable {

    public static final String TABLE_NAME = "Items";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "item_name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_ID_UNIT = "id_unit";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_COMMENT = "comment";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
            + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_AMOUNT + " REAL, "
            + COLUMN_ID_UNIT + " INTEGER, "
            + COLUMN_PRICE + " REAL, "
            + COLUMN_COMMENT + " TEXT, "
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
        String[] item = {
                "молоко",
                "хлеб",
                "масло сливочное",
                "черный чай",
                "рис",
                "гречка",
        };
        double[] amount = { 10, 11, 0, 20, 13, 14 };
        int[] amountUnit = { 2, 2, 2, 1, 1, 1 };
        double[] price = { 1055.00, 0, 1066, 1087, 1070, 1005 };
        String[] comment = {
                "",
                "буханка белого",
                "проверь дату изготовления",
                "крупнолистовой",
                "",
                "не жареная",
        };

        for(int i = 0; i < item.length; i++) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(COLUMN_NAME, item[i]);
            contentValue.put(COLUMN_AMOUNT, amount[i]);
            contentValue.put(COLUMN_ID_UNIT, amountUnit[i]);
            contentValue.put(COLUMN_PRICE, price[i]);
            contentValue.put(COLUMN_COMMENT, comment[i]);
            database.insert(TABLE_NAME, null, contentValue);
        }
    }
}
