package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;
import android.database.Cursor;

import ru.android.ainege.shoppinglist.db.entities.Dictionary;

public abstract class DictionaryDS<T extends Dictionary> extends GenericDS<T> {

	public DictionaryDS(Context context) {
		super(context);
	}

	public abstract DictionaryCursor<T> getAll(long withoutId);

	public abstract boolean isUsed(long id);

	public abstract void delete(long id, long newId);

	public static abstract class DictionaryCursor<S extends Dictionary> extends EntityCursor<S> {

		public DictionaryCursor(Cursor cursor) {
			super(cursor);
		}
	}
}
