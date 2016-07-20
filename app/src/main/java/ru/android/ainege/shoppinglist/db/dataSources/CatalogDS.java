package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;
import android.database.Cursor;

import ru.android.ainege.shoppinglist.db.entities.Catalog;

public abstract class CatalogDS<T extends Catalog> extends GenericDS<T> {

	public CatalogDS(Context context) {
		super(context);
	}

	public abstract CatalogCursor<T> getAll(long withoutId);

	public abstract boolean isUsed(long id);

	public abstract void delete(long id, long newId);

	public static abstract class CatalogCursor<S extends Catalog> extends EntityCursor<S> {

		public CatalogCursor(Cursor cursor) {
			super(cursor);
		}
	}
}
