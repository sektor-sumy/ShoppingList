package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;

public class ItemData implements Serializable {
	private long mId;
	private double mAmount;
	private long mIdUnit;
	private double mPrice;
	private long mIdCategory;
	private String mComment;
	private Unit mUnit;
	private Category mCategory;

	public ItemData() {}

	public ItemData(long idUnit, long idCategory) {
		mIdUnit = idUnit;
		mIdCategory = idCategory;
	}

	public ItemData(long id, double amount, long idUnit, double price, long idCategory, String comment) {
		this(idUnit, idCategory);
		mId = id;
		mAmount = amount;
		mPrice = price;
		mComment = comment;
	}

	public ItemData(double amount, long idUnit, double price, long idCategory, String comment) {
		this(idUnit, idCategory);
		mAmount = amount;
		mPrice = price;
		mComment = comment;
	}

	public ItemData(double amount, Unit unit, double price, Category category, String comment) {
		this(amount, unit.getId(), price, category.getId(), comment);
		mUnit = unit;
		mComment = comment;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
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
}
