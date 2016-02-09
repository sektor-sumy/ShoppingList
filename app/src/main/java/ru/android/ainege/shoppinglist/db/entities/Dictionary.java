package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;

public class Dictionary implements Serializable {
	private long mId;
	private String mName;

	public Dictionary(String name) {
		mName = name;
	}

	public Dictionary(long id, String name) {
		this(name);
		mId = id;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	@Override
	public String toString() {
		return mName;
	}
}
