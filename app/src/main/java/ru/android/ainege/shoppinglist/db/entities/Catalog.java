package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;

public interface Catalog extends Serializable {
	long getId();
	void setId(long id);

	String getName();
	void setName(String name);

	String toString();
}
