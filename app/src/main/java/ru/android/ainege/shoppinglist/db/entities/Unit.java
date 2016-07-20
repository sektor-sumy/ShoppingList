package ru.android.ainege.shoppinglist.db.entities;

public class Unit extends Dictionary {

	public Unit(Unit unit) {
		this(unit.getId(), unit.getName());
	}

	public Unit(String name) {
		super(name);
	}

	public Unit(long id, String name) {
		super(id, name);
	}
}
