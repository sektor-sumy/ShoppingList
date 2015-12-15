package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ShoppingList implements Serializable {
	private static String mType;
	private static boolean mIsBoughtEndInList;

	public final static String ALPHABET = "alphabet";
	public final static String UP_PRICE = "upPrice";
	public final static String DOWN_PRICE = "downPrice";
	public final static String ORDER_ADDING = "orderAdding";

	private long mIdItem;
	private long mIdList;
	private boolean mIsBought;
	private double mAmount;
	private long mIdUnit;
	private double mPrice;
	private String mComment;
	private Date mDate;
	private Item mItem;
	private Unit mUnit;

	private ShoppingList(boolean isBought, double amount, double price, String comment) {
		mIsBought = isBought;
		mAmount = amount;
		mPrice = price;
		mComment = comment;
	}

	private ShoppingList(long idItem, boolean isBought, double amount, long idUnit, double price, String comment, Date date) {
		this(isBought, amount, price, comment);
		mIdItem = idItem;
		mIdUnit = idUnit;
		mDate = date;
	}

	public ShoppingList(long idItem, long idList, boolean isBought, double amount, long idUnit, double price, String comment, Date date) {
		this(idItem, isBought, amount, idUnit, price, comment, date);
		mIdList = idList;
	}

	public ShoppingList(Item item, long idList, boolean isBought, double amount, Unit unit, double price, String comment) {
		this(isBought, amount, price, comment);
		mItem = item;
		mIdItem = mItem.getId();
		mIdList = idList;
		mUnit = unit;
	}

	public long getIdItem() {
		return mIdItem;
	}

	public void setIdItem(long idItem) {
		mIdItem = idItem;
	}

	public long getIdList() {
		return mIdList;
	}

	public void setIdList(long idList) {
		mIdList = idList;
	}

	public boolean isBought() {
		return mIsBought;
	}

	public void setBought(boolean bought) {
		mIsBought = bought;
	}

	public double getAmount() {
		return mAmount;
	}

	public void setAmount(double amount) {
		mAmount = amount;
	}

	public long getIdUnit() {
		return mIdUnit;
	}

	public void setIdUnit(long idUnit) {
		mIdUnit = idUnit;
	}

	public double getPrice() {
		return mPrice;
	}

	public void setPrice(double price) {
		this.mPrice = price;
	}

	public String getComment() {
		return mComment;
	}

	public void setComment(String comment) {
		mComment = comment;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	public Item getItem() {
		return mItem;
	}

	public void setItem(Item item) {
		mItem = item;
		if (mItem != null) {
			mIdItem = mItem.getId();
		}
	}

	public Unit getUnit() {
		return mUnit;
	}

	public void setUnit(Unit unit) {
		mUnit = unit;
		if (mUnit != null) {
			mIdUnit = mUnit.getId();
		}
	}

	public static void setSortSettings(boolean isBoughtEndInList, String type) {
		mIsBoughtEndInList = isBoughtEndInList;
		mType = type;
	}

	public static void sort(ArrayList<ShoppingList> itemsInList) {
		Collections.sort(itemsInList, new Comparator<ShoppingList>() {
			@Override
			public int compare(ShoppingList lhs, ShoppingList rhs) {
				int result = 0;

				if (mIsBoughtEndInList) {
					if (lhs.isBought() && !rhs.isBought()) {
						return 1;
					} else if (!lhs.isBought() && rhs.isBought()) {
						return -1;
					}
				}

				switch (mType) {
					case ALPHABET:
						result = lhs.getItem().getName().compareToIgnoreCase(rhs.getItem().getName());
						break;
					case UP_PRICE:
						result = (int) (lhs.getPrice() - rhs.getPrice());
						break;
					case DOWN_PRICE:
						result = (int) (rhs.getPrice() - lhs.getPrice());
						break;
					case ORDER_ADDING:
						result = (int) (lhs.getDate().getTime() - rhs.getDate().getTime());
						break;
				}

				return result;
			}
		});
	}
}
