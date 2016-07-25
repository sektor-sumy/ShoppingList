package ru.android.ainege.shoppinglist.ui.activities.lists.screen;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.activities.lists.TabletState;

public class ListsScreen extends TabletScreen {
	public static final int SCREEN_ID = 1;

	public ListsScreen(TabletState state) {
		super(state);
		if (mState.getListsActivity().getListsFragment() != null) {
			setListeners();
		}
	}

	public void setListeners() {
		mState.getListsActivity().getListsFragment().setListeners(mState, mState);
	}

	@Override
	public void restore() {
		if (mState.isLandscape()) {
			mState.openList();
		}
	}

	@Override
	public boolean onBackPressed() {
		return true;
	}

	@Override
	public int getScreenId() {
		return SCREEN_ID;
	}

	@Override
	public void toScreen() {
		mState.setLastSelectedListId(-1);
		mState.setFragmentTagForRemove(TabletState.SHOPPING_LIST_TAG);

		mState.animation(Float.valueOf(mState.getListsActivity().getString(R.string.lists_weight_ls)),
				Float.valueOf(mState.getListsActivity().getString(R.string.shopping_list_weight_ls)),
				Float.valueOf(mState.getListsActivity().getString(R.string.item_weight_ls)));
	}

	@Override
	public void onOpenDialog(long idList) {
		onOpenDialog(null, idList);
		mState.setShouldBackPressed(TabletState.DIALOG_BEHAVIOUR_LIST);
	}

	@Override
	public void onListClick(long id) {
		mState.openList(id, false);
		mState.toScreen(mState.getShoppingListScreen());
	}

	@Override
	public void onListUpdated() {

	}

	@Override
	public void onListDeleted(long idDeletedList) {

	}
}
