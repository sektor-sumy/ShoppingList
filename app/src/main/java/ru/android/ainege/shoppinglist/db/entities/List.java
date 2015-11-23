package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;
import java.util.ArrayList;

public class List implements Serializable {
	private long mId;
	private String mName;
	private long mIdCurrency;
	private ArrayList<ShoppingList> mItemsInList;
	private Currency mCurrency;

	private int mAmountBoughtItems;
	private int mAmountItems;

	public List(String name) {
		mName = name;
	}

	public List(String name, long currency) {
		mName = name;
		mIdCurrency = currency;
	}

	public List(long id, String name) {
		this(name);
		mId = id;
	}

	public List(long id, String name, long idCurrency) {
		this(id, name);
		mIdCurrency = idCurrency;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public long getIdCurrenty() {
		return mIdCurrency;
	}

	public void setIdCurrenty(long id) {
		this.mIdCurrency = id;
	}

	public ArrayList<ShoppingList> getItemsInList() {
		return mItemsInList;
	}

	public void setItemsInList(ArrayList<ShoppingList> itemsInList) {
		this.mItemsInList = itemsInList;
	}

	public Currency getCurrency() {
		return mCurrency;
	}

	public void setCurrency(Currency currency) {
		mCurrency = currency;
		if (mCurrency != null) {
			mIdCurrency = mCurrency.getId();
		}
	}

	public int getAmountBoughtItems() {
		return mAmountBoughtItems;
	}

	public void setAmountBoughtItems(int amountBoughtItems) {
		this.mAmountBoughtItems = amountBoughtItems;
	}

	public int getAmountItems() {
		return mAmountItems;
	}

	public void setAmountItems(int amountItems) {
		this.mAmountItems = amountItems;
	}
}

