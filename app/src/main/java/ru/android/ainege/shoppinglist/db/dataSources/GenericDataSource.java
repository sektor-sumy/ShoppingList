package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;

public abstract class GenericDataSource<T> {
	final Context mContext;
	final ShoppingListSQLiteHelper mDbHelper;

	GenericDataSource(Context context) {
		mContext = context;
		mDbHelper = new ShoppingListSQLiteHelper(mContext);
	}

	public abstract int update(T currency);

	public abstract long add(T currency);

	public abstract void delete(long id);

	public static abstract class EntityCursor<S> extends CursorWrapper {
		public EntityCursor(Cursor cursor) {
			super(cursor);
		}

		public abstract S getEntity();

		public ArrayList<S> getEntities() {
			ArrayList<S> list = new ArrayList<>();
			moveToFirst();

			do {
				list.add(getEntity());
			} while (moveToNext());

			return list;
		}
	}
}
