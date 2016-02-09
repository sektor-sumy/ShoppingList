package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;

import ru.android.ainege.shoppinglist.db.entities.Dictionary;

public abstract class DictionaryDS<T extends Dictionary> extends GenericDS<T> {

	public DictionaryDS(Context context) {
		super(context);
	}

	public abstract long getRandomId(long id);

	public abstract boolean isUsed(long id);
}
