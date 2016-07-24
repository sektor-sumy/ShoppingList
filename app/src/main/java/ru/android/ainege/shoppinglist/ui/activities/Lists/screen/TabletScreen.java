package ru.android.ainege.shoppinglist.ui.activities.lists.screen;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.OnDialogShownListener;
import ru.android.ainege.shoppinglist.ui.activities.lists.TabletState;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.AddItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.EditItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;

public abstract class TabletScreen implements OnDialogShownListener, ListsFragment.OnListSelectListener,
		ShoppingListFragment.OnListChangedListener, ShoppingListFragment.OnClickListener {
	protected TabletState mState;

	public abstract void restore();
	public abstract boolean onBackPressed();
	public abstract int getScreenId();
	public abstract void toScreen();

	public TabletScreen (TabletState state) {
		mState = state;
	}

	@Override
	public void onOpenDialog(long idList) {
		//if screen of shopping list opened with delete mode and open dialog - close delete mode
		ShoppingListFragment listFragment = (ShoppingListFragment) mState.getListsActivity().
				getFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
		if (listFragment != null) listFragment.closeActionMode();
	}

	@Override
	public void onCloseDialog() {
		mState.setShouldBackPressed(TabletState.DIALOG_BEHAVIOUR_DEFAULT);
	}

	@Override
	public void onItemAdd(long idList) {
		ItemFragment fragment = AddItemFragment.newInstance(idList);
		mState.getItemScreen().setListeners(fragment);
		mState.openItem(0, fragment);
	}

	@Override
	public void onItemSelect(ShoppingList item) {
		ItemFragment fragment = EditItemFragment.newInstance(item);
		mState.getItemScreen().setListeners(fragment);
		mState.openItem(item.getIdItem(), fragment);
	}

	public void updateCurrentList() {
		ShoppingListFragment listFragment = (ShoppingListFragment) mState.getListsActivity().
				getFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
		listFragment.updateData();
	}

	public long getLastSelectedListId() {
		return mState.getLastSelectedListId();
	}

	public long getLastSelectedItemId() {
		return mState.getLastSelectedItemId();
	}

	public boolean deleteList(long idDeletedList) {
		boolean result = false;
		ArrayList<List> lists = mState.getListsActivity().getListsFragment().getLists();

		if (lists.size() == 1) { // it was last list
			mState.toScreen(mState.getListsScreen());
		} else { //open first list
			long id = lists.get(0).getId();
			mState.openList(id != idDeletedList ? id : lists.get(1).getId(), true);
			result = true;
		}

		return result;
	}
}
