package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;
import java.util.ArrayList;

public class List implements Serializable {
	private long mId;
	private String mName;
	private long mIdCurrency;
	private String mImagePath;

	private ArrayList<ShoppingList> mItemsInList;
	private Currency mCurrency;

	private int mAmountBoughtItems;
	private int mAmountItems;

	public List(String name, long idCurrency, String imagePath) {
		mName = name;
		mIdCurrency = idCurrency;
		mImagePath = imagePath;
	}

	public List(long id, String name, long idCurrency, String imagePath) {
		this(name, idCurrency, imagePath);
		mId = id;
	}

	public List(List list) {
		this(list.getId(), list.getName(), list.getIdCurrency(), list.getImagePath());
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

	public long getIdCurrency() {
		return mIdCurrency;
	}

	public void setIdCurrency(long id) {
		mIdCurrency = id;
	}

	public String getImagePath() {
		return mImagePath;
	}

	public void setImagePath(String imagePath) {
		mImagePath = imagePath;
	}

	public ArrayList<ShoppingList> getItemsInList() {
		return mItemsInList;
	}

	public void setItemsInList(ArrayList<ShoppingList> itemsInList) {
		mItemsInList = itemsInList;
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
		mAmountBoughtItems = amountBoughtItems;
	}

	public int getAmountItems() {
		return mAmountItems;
	}

	public void setAmountItems(int amountItems) {
		mAmountItems = amountItems;
	}
}

