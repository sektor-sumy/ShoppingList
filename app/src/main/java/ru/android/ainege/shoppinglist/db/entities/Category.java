package ru.android.ainege.shoppinglist.db.entities;

import java.util.ArrayList;

public class Category extends Dictionary {
	private ArrayList<ShoppingList> mItemsByCategoriesInList;

	public Category(String name) {
		super(name);
	}

	public Category(long id, String name) {
		super(id, name);
	}

	public ArrayList<ShoppingList> getItemsByCategoryInList() {
		return mItemsByCategoriesInList;
	}

	public void setItemsByCategoryInList(ArrayList<ShoppingList> itemsByCategoryInList) {
		mItemsByCategoriesInList = itemsByCategoryInList;
	}
}
