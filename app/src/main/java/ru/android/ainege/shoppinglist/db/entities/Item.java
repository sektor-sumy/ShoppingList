package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;

public class Item implements Serializable {
	private long mId;
	private String mName;
	private double mAmount;
	private long mIdUnit = 1;
	private double mPrice;
	private String mComment;
	private String mDefaultImagePath;
	private String mImagePath;
	private Unit mUnit;

	public Item(String name, long idUnit, String defaultImagePath, String imagePath) {
		mName = name;
		mIdUnit = idUnit;
		mDefaultImagePath = defaultImagePath;
		mImagePath = imagePath;
	}

	public Item(long id, String name, double amount, long idUnit, double price, String comment, String defaultImagePath, String imagePath) {
		this(name, idUnit, defaultImagePath, imagePath);
		mId = id;
		mAmount = amount;
		mPrice = price;
		mComment = comment;
	}

	public Item(String name, double amount, Unit unit, double price, String comment, String imagePath) {
		this(0, name, amount, unit.getId(), price, comment, null, imagePath);
		mUnit = unit;
	}

	public Item(long id, String name, String defaultImagePath, String imagePath) {
		this(id, name, -1, 1, -1, null, defaultImagePath, imagePath);
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

	public Unit getUnit() {
		return mUnit;
	}

	public void setUnit(Unit unit) {
		mUnit = unit;
		if (mUnit != null) {
			mIdUnit = mUnit.getId();
		}
	}

	public String getComment() {
		return mComment;
	}

	public void setComment(String comment) {
		mComment = comment;
	}

	public void setDefaultImagePath(String imagePath) {
		mDefaultImagePath = imagePath;
	}

	public String getDefaultImagePath() {
		return mDefaultImagePath;
	}

	public void setImagePath(String imagePath) {
		mImagePath = imagePath;
	}

	public String getImagePath() {
		return mImagePath;
	}
}
