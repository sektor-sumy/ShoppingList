package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.UnitEntity;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class UnitsDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public UnitsDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public UnitEntity get(int id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(UnitsTable.TABLE_NAME,
                                 null,
                                 UnitsTable.COLUMN_ID + "=?",
                                 new String[]{String.valueOf(id)}, null, null, null, null);
        cursor.moveToFirst();
        UnitEntity unit = new UnitEntity(cursor.getInt(0), cursor.getString(1));
        cursor.close();
        return unit;
    }

    public List<UnitEntity> getAll() {
        List<UnitEntity> units = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + UnitsTable.TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()) {
            do {
                UnitEntity unit = new UnitEntity(cursor.getInt(0), cursor.getString(1));
                units.add(unit);
            } while(cursor.moveToNext());
        }
        cursor.close();
        return units;
    }
}
