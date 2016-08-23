package ru.android.ainege.shoppinglist.ui.activities.Lists.screen;

import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.activities.Lists.TabletState;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public class ListsScreen extends TabletScreen {
	public static final int SCREEN_ID = 1;

	private ListsFragment mListsFragment;

	public ListsScreen(TabletState state) {
		super(state);
	}

	public void setListeners() {
		mListsFragment.setListeners(mState, mState);
	}

	@Override
	public void restore() {
		if (mState.isLandscape()) {
			mState.openList();
		}
	}

	@Override
	public boolean onCreateViewListener(Toolbar toolbar) {
		return true;
	}

	@Override
	public ListsFragment getFragment() {
		return mListsFragment;
	}

	public void setFragment(ListsFragment listsFragment) {
		mListsFragment = listsFragment;
		setListeners();
	}

	@Override
	public void onMainSelected() {

	}

	@Override
	public void onLastListSelected() {
		mState.openLastList();
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
		NavigationView navigationView = (NavigationView) mState.getListsActivity().findViewById(R.id.nav_view);
		navigationView.setCheckedItem(R.id.nav_main);

		mState.setLastSelectedListId(-1);
		mState.setFragmentTagForRemove(TabletState.SHOPPING_LIST_TAG);

		mState.animation(Float.valueOf(mState.getListsActivity().getString(R.string.lists_weight_ls)),
				Float.valueOf(mState.getListsActivity().getString(R.string.shopping_list_weight_ls)),
				Float.valueOf(mState.getListsActivity().getString(R.string.item_weight_ls)));
	}

	@Override
	public void closeActionMode() {

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

	public void updateList() {
		mListsFragment.updateData();
	}
}
