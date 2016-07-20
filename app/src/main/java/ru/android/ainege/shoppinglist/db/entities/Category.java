package ru.android.ainege.shoppinglist.db.entities;

import java.util.ArrayList;

public class Category extends Catalog {
	private int mColor;
	private ArrayList<ShoppingList> mItemsByCategoriesInList;

	private double mSpentSum;
	private int mBoughtItemsCount;

	public Category(Category category) {
		this(category.getId(), category.getName(), category.getColor());
	}

	public Category(String name, int color) {
		super(name);
		mColor = color;
	}

	public Category(long id, String name, int color) {
		super(id, name);
		mColor = color;
	}

	public Category(ArrayList<ShoppingList> itemsByCategories) {
		super(null);
		mItemsByCategoriesInList = itemsByCategories;
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
	}

	public ArrayList<ShoppingList> getItemsByCategoryInList() {
		return mItemsByCategoriesInList;
	}

	public void setItemsByCategoryInList(ArrayList<ShoppingList> itemsByCategoryInList) {
		mItemsByCategoriesInList = itemsByCategoryInList;
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

		for (ShoppingList item : mItemsByCategoriesInList) {
			if (item.isBought()) {
				mBoughtItemsCount++;
			}
		}

		return mBoughtItemsCount;
	}

	private double sum(boolean onlyBought) {
		double sum = 0;

		for (ShoppingList item : mItemsByCategoriesInList) {
			if ((onlyBought && item.isBought()) || !onlyBought) {
				sum += item.getSum();
			}
		}

		return sum;
	}
}
