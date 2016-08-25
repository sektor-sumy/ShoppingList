package ru.android.ainege.shoppinglist.db.entities;

public class Currency implements Catalog {
	private long mId;
	private String mName;
	private String mSymbol;

	public Currency(String name, String symbol) {
		mName = name;
		mSymbol = symbol;
	}

	public Currency(long id, String name, String symbol) {
		this(name, symbol);
		mId = id;
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

	public String getSymbol() {
		return mSymbol;
	}

	public void setSymbol(String symbol) {
		mSymbol = symbol;
	}

	@Override
	public String toString() {
		return mSymbol + " (" + mName + ")";
	}
}
