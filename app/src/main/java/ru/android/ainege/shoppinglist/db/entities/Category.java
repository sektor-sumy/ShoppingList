package ru.android.ainege.shoppinglist.db.entities;

import java.util.ArrayList;

public class Category extends Dictionary {
	private int mColor;
	private ArrayList<ShoppingList> mItemsByCategoriesInList;

	public Category(String name, int color) {
		super(name);
		mColor = color;
	}

	public Category(long id, String name, int color) {
		super(id, name);
		mColor = color;
	}

	public Category( ArrayList<ShoppingList>  itemsByCategories) {
		super("");
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
}
