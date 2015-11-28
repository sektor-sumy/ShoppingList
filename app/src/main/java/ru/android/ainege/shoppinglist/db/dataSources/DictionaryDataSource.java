package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.Context;

import ru.android.ainege.shoppinglist.db.entities.Dictionary;

public abstract class DictionaryDataSource<T extends Dictionary> extends GenericDataSource<T>{

	public DictionaryDataSource(Context context) {
		super(context);
	}

	public abstract EntityCursor<T> getAll();
}
