package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;

import ru.android.ainege.shoppinglist.db.entities.Dictionary;

public abstract class DictionaryDS<T extends Dictionary> extends GenericDS<T> {

	DictionaryDS(Context context) {
		super(context);
	}

	public abstract EntityCursor<T> getAll();

	public abstract long getRandomId(long id);
}
