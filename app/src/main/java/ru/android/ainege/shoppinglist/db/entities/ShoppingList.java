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
	private long mIdItemData;
	private Date mDate;
	private Item mItem;
	private ItemData mItemData;

	public ShoppingList(long idItem, long idList, boolean isBought, long idData, Date date) {
		mIdItem = idItem;
		mIdList = idList;
		mIsBought = isBought;
		mIdItemData = idData;
		mDate = date;
	}

	public ShoppingList(Item item, long idList, boolean isBought, ItemData itemData) {
		this(item.getId(), idList, isBought, itemData.getId(), null);
		mItem = item;
		mItemData = itemData;
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

	public long getIdItemData() {
		return mIdItemData;
	}

	public void setIdItemData(long id) {
		mIdItemData = id;
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

	public ItemData getItemData() {
		return mItemData;
	}

	public void setItemData(ItemData itemData) {
		mItemData = itemData;
	}

	public boolean equals(ShoppingList item) {
		return !(!mItem.getName().equals(item.getItem().getName()) ||
				!mItem.getImagePath().equals(item.getItem().getImagePath()) ||
				mIsBought != item.isBought() || getItemData().getAmount() != item.getItemData().getAmount() ||
				getItemData().getIdUnit() != item.getItemData().getIdUnit() || getItemData().getPrice() != item.getItemData().getPrice() ||
				!getItemData().getComment().equals(item.getItemData().getComment()));
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
						result = (int) (lhs.getItemData().getPrice() - rhs.getItemData().getPrice());
						break;
					case DOWN_PRICE:
						result = (int) (rhs.getItemData().getPrice() - lhs.getItemData().getPrice());
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
