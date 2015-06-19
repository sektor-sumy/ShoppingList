package ainege.android.ru.shoppinglist;

import android.database.sqlite.SQLiteDatabase;

public class ItemsTable {

    public static final String TABLE_NAME = "Items";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_ID_UNIT_FK = "id_unit_fk";
    public static final String COLUMN_PRICE = "price";


    private static final String DATABASE_CREATE = "create table " + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_AMOUNT + " text, "
            + COLUMN_ID_UNIT_FK + " integer, "
            + COLUMN_PRICE + " real, "
            + "FOREIGN KEY (" + COLUMN_ID_UNIT_FK + ") REFERENCES " + UnitsTable.TABLE_NAME + " (" + UnitsTable.COLUMN_ID + ")"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

}
