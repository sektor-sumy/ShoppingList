package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;

public abstract class ItemData implements Serializable {
	protected long mIdItemData;
	protected double mAmount;
	protected long mIdUnit;
	protected double mPrice;
	protected long mIdCategory;
	protected String mComment;

	protected Unit mUnit;
	protected Category mCategory;

	protected ItemData() {
	}

	public abstract long getIdItem();
	public abstract void setIdItem(long id);

	public ItemData(ItemData itemData) {
		mIdItemData = itemData.getIdItemData();
		mAmount = itemData.getAmount();
		mPrice = itemData.getPrice();
		mComment = itemData.getComment();

		if (itemData.getUnit() != null) {
			setUnit(new Unit(itemData.getUnit()));
		}

		if (itemData.getCategory() != null) {
			setCategory(new Category(itemData.getCategory()));
		}
	}

	public long getIdItemData() {
		return mIdItemData;
	}

	public void setIdItemData(long id) {
		mIdItemData = id;
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
		mPrice = price;
	}

	public long getIdCategory() {
		return mIdCategory;
	}

	public void setIdCategory(long idCategory) {
		mIdCategory = idCategory;
	}

	public String getComment() {
		return mComment;
	}

	public void setComment(String comment) {
		mComment = comment;
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

	public Category getCategory() {
		return mCategory;
	}

	public void setCategory(Category category) {
		mCategory = category;

		if (mCategory != null) {
			mIdCategory = mCategory.getId();
		}
	}

	public boolean equals(ItemData item) {
		boolean equalsComment;

		if (mComment == null) {
			equalsComment = item.getComment() == null || item.getComment().length() == 0;
		} else {
			equalsComment = (mComment.length() == 0 && (item.getComment() == null || item.getComment().length() == 0))
					|| mComment.equals(item.getComment());
		}

		return mIdItemData == item.getIdItemData() &&
				mAmount == item.getAmount() &&
				mIdUnit == item.getIdUnit() &&
				mPrice == item.getPrice() &&
				mIdCategory == item.getIdCategory() &&
				equalsComment;
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof ItemData && equals((ItemData) object);
	}
}
