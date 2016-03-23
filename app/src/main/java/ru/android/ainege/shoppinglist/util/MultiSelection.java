package ru.android.ainege.shoppinglist.util;

import java.util.ArrayList;
import java.util.List;

import ru.android.ainege.shoppinglist.db.entities.ShoppingList;


public class MultiSelection {
	private static MultiSelection sInstance;
	private ArrayList<ShoppingList> mSelectedItems = new ArrayList<>();

	public static MultiSelection getInstance() {
		if (sInstance == null) {
			sInstance = new MultiSelection();
		}

		return sInstance;
	}

	public ArrayList<ShoppingList> getSelectedItems() {
		return mSelectedItems;
	}

	public boolean isContains(ShoppingList item) {
		return mSelectedItems.contains(item);
	}

	public void delete(Object item) {
		mSelectedItems.remove(item);
	}

	public void clearSelections() {
		mSelectedItems.clear();
	}

	public void toggleSelection(ShoppingList item) {
		toggleSelection(item, true);
	}

	private void toggleSelection(ShoppingList item, boolean removeIfExist) {
		if (mSelectedItems.contains(item)) {
			if (removeIfExist) {
				for (int i = 0; i < mSelectedItems.size(); i++) {
					if (mSelectedItems.get(i).getIdItem() == item.getIdItem()) {
						mSelectedItems.remove(i);
						break;
					}
				}
			}
		} else {
			mSelectedItems.add(item);
		}
	}

	public void selectAllItems(List<Object> items, boolean isBought) {
		boolean isAllSelected = checkAllBoughtSelected(items, isBought);

		for (Object item : items) {
			if (item instanceof ShoppingList && ((ShoppingList) item).isBought() == isBought) {
				//if all item selected - remove selection from them
				//else select items
				toggleSelection((ShoppingList) item, isAllSelected);
			}
		}
	}

	private boolean checkAllBoughtSelected(List<Object> items, boolean isBought) {
		for (Object item : items) {
			if (item instanceof ShoppingList && ((ShoppingList) item).isBought() == isBought) {
				if (!mSelectedItems.contains(item)) {
					return false;
				}
			}
		}

		return true;
	}

	public void selectAllItemsInCategory(List<ShoppingList> items) {
		boolean isAllSelected = checkAllSelected(items);

		for (ShoppingList item : items) {
			toggleSelection(item, isAllSelected);
		}
	}

	private boolean checkAllSelected(List<ShoppingList> items) {
		for (ShoppingList item : items) {
			if (!mSelectedItems.contains(item)) {
				return false;
			}
		}

		return true;
	}
}
