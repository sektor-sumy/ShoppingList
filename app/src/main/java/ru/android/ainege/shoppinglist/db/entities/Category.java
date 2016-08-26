package ru.android.ainege.shoppinglist.db.entities;

import java.util.ArrayList;

public class Category implements Catalog {
	private long mId;
	private String mName;
	private int mColor;
	private ArrayList mItemsByCategories;

	private double mSpentSum;
	private int mBoughtItemsCount;

	public Category(String name, int color) {
		mName = name;
		mColor = color;
	}

	public Category(long id, String name, int color) {
		this(name, color);
		mId = id;
	}

	public Category(Category category) {
		this(category.getId(), category.getName(), category.getColor());
	}

	public Category(ArrayList itemsByCategories) {
		mItemsByCategories = itemsByCategories;
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

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
	}

	@Override
	public String toString() {
		return mName;
	}

	public ArrayList getItemsByCategories() {
		return mItemsByCategories;
	}

	public void setItemsByCategories(ArrayList itemsByCategories) {
		mItemsByCategories = itemsByCategories;
	}

	public double getSpentSum() {
		return mSpentSum;
	}

	public void setSpentSum(double spentSum) {
		mSpentSum = spentSum;
	}

	public int getBoughtItemsCount() {
		return mBoughtItemsCount;
	}

	public void setBoughtItemsCount(int boughtItemsCount) {
		mBoughtItemsCount = boughtItemsCount;
	}

	public double calculateTotalSum() {
		return sum(false);
	}

	public double calculateSpentSum() {
		mSpentSum = sum(true);
		return mSpentSum;
	}

	public int countBoughtItems() {
		mBoughtItemsCount = 0;

		for (Object item : mItemsByCategories) {
			if (((ShoppingList) item).isBought()) {
				mBoughtItemsCount++;
			}
		}

		return mBoughtItemsCount;
	}

	private double sum(boolean onlyBought) {
		double sum = 0;

		for (Object item : mItemsByCategories) {
			ShoppingList itemInList = ((ShoppingList) item);

			if ((onlyBought && itemInList.isBought()) || !onlyBought) {
				sum += itemInList.getSum();
			}
		}

		return sum;
	}
}
