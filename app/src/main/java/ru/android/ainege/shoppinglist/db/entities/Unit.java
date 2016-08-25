package ru.android.ainege.shoppinglist.db.entities;

public class Unit implements Catalog {
	private long mId;
	private String mName;

	public Unit(String name) {
		mName = name;
	}

	public Unit(long id, String name) {
		this(name);
		mId = id;
	}

	public Unit(Unit unit) {
		this(unit.getId(), unit.getName());
	}

	@Override
	public long getId() {
		return mId;
	}

	@Override
	public void setId(long id) {
		mId = id;
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public void setName(String name) {
		mName = name;
	}

	@Override
	public String toString() {
		return mName;
	}
}
