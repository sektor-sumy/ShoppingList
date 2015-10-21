package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

public class UnitsDataSource {
    private Context mContext;
    private ShoppingListSQLiteHelper mDbHelper;

    public UnitsDataSource(Context context) {
        mContext = context;
        mDbHelper = new ShoppingListSQLiteHelper(mContext);
    }

    public UnitCursor getAll() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(UnitsTable.TABLE_NAME, null, null, null, null, null, null, null);
        return new UnitCursor(cursor);
    }

    public static class UnitCursor extends CursorWrapper {
        public UnitCursor(Cursor cursor) {
            super(cursor);
        }

        public Unit getUnit() {
            long id = getLong(getColumnIndex(UnitsTable.COLUMN_ID));
            String name = getString(getColumnIndex(UnitsTable.COLUMN_NAME));

            return new Unit(id, name);
        }
    }
}
