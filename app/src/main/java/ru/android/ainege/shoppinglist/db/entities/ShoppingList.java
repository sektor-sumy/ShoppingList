package ru.android.ainege.shoppinglist.db.entities;

import android.content.Context;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;

public class ShoppingList extends ItemData {
	public final static String ALPHABET = "alphabet";
	public final static String ORDER_ADDING = "orderAdding";
	public final static String UP_PRICE = "upPrice";
	public final static String DOWN_PRICE = "downPrice";
	public final static String UP_PURCHASE_PRICE = "upPurchasePrice";
	public final static String DOWN_PURCHASE_PRICE = "downPurchasePrice";

	private static String mType;
	private static boolean mIsBoughtEndInList;

	private long mIdItem;
	private long mIdList;
	private boolean mIsBought;
	private Date mDate;

	private Item mItem;

	private double mSum = 0;

	public ShoppingList(long idList) {
		mIdList = idList;
		mItem = new Item();
	}

	public ShoppingList(long idItem, long idList, boolean isBought, long idData, Date date) {
		mIdItem = idItem;
		mIdList = idList;
		mIsBought = isBought;
		mIdItemData = idData;
		mDate = date;
	}

	public ShoppingList(ShoppingList list) {
		super(list);
		mIdItem = list.getIdItem();
		mIdList = list.getIdList();
		mIsBought = list.isBought();
		mDate = list.getDate();
		setItem(new Item(list.getItem()));
	}

	public static void setSortSettings(boolean isBoughtEndInList) {
		mIsBoughtEndInList = isBoughtEndInList;
	}

	public static void setSortSettings(String type) {
		mType = type;
	}

	public static void sort(ArrayList<ShoppingList> itemsInList) {
		Collections.sort(itemsInList, new Comparator<ShoppingList>() {
			@Override
			public int compare(ShoppingList lhs, ShoppingList rhs) {
				int result;

				if (mIsBoughtEndInList) {
					if (lhs.isBought() && !rhs.isBought()) {
						return 1;
					} else if (!lhs.isBought() && rhs.isBought()) {
						return -1;
					}
				}

				switch (mType) {
					case ORDER_ADDING:
						result = (int) (lhs.getDate().getTime() - rhs.getDate().getTime());
						break;
					case UP_PRICE:
						result = (int) (lhs.getPrice() - rhs.getPrice());
						break;
					case DOWN_PRICE:
						result = (int) (rhs.getPrice() - lhs.getPrice());
						break;
					case UP_PURCHASE_PRICE:
						result = (int) (getSum(lhs) - getSum(rhs));
						break;
					case DOWN_PURCHASE_PRICE:
						result = (int) (getSum(rhs) - getSum(lhs));
						break;
					case ALPHABET:
					default:
						result = lhs.getItem().getName().compareToIgnoreCase(rhs.getItem().getName());
				}

				return result;
			}
		});
	}

	private static double getSum(ShoppingList sl) {
		return sl.getPrice() * (sl.getAmount() == 0 ? 1 : sl.getAmount());
	}

	public long getIdItem() {
		return mIdItem;
	}

	public void setIdItem(long idItem) {
		mIdItem = idItem;

		if (mItem != null) {
			mItem.setId(mIdItem);
		}
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

	public double getSum() {
		mSum = getSum(this);
		return new BigDecimal(mSum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public void setSum(double sum) {
		mSum = sum;
	}

	public boolean equals(ShoppingList item) {
		return mItem.getName().equals(item.getItem().getName()) &&
				mItem.getImagePath().equals(item.getItem().getImagePath()) &&
				mIsBought == item.isBought() && super.equals(item);
	}

	public long updateItem(Context context) {
		long idItem = mItem.getId();

		mItem.setAmount(mAmount);
		mItem.setUnit(mUnit);
		mItem.setPrice(mPrice);
		mItem.setCategory(mCategory);
		mItem.setComment(mComment);

		if (mItem.isNew()) {
			idItem = new ItemDS(context).add(mItem);
			setIdItem(idItem);

			FirebaseAnalytic.getInstance(context, FirebaseAnalytic.NEW_ITEM)
					.putString(FirebaseAnalytic.NAME, mItem.getName())
					.putString(FirebaseAnalytic.UNIT, mUnit.getName())
					.putString(FirebaseAnalytic.CATEGORY, mCategory.getName())
					.addEvent();
		} else {
			new ItemDS(context).update(mItem);
		}

		return idItem;
	}
}
