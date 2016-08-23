package ru.android.ainege.shoppinglist.ui.activities.Lists.screen;

import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.activities.Lists.TabletState;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;

public class ShoppingListScreen extends TabletScreen implements	ListsFragment.OnListChangedListener {
	public static final int SCREEN_ID = 2;

	private ShoppingListFragment mShoppingListFragment;
	private ActionBarDrawerToggle mToggle;

	public ShoppingListScreen(TabletState state) {
		super(state);
	}

	public void setListeners(ShoppingListFragment fragment) {
		mState.getListsScreen().getFragment().setOnListChangedListener(this);
		mShoppingListFragment = fragment;
		mShoppingListFragment.setListeners(mState, mState, mState);
	}

	@Override
	public void restore() {
		if (!(new MaterialShowcaseSequence(mState.getListsActivity(), Showcase.SHOT_LIST).hasFired())) {
			mState.setCurrentScreen(mState.getListsScreen());
			mState.getListsActivity().removeFragmentFromUI(TabletState.SHOPPING_LIST_TAG);
		} else {
			mState.setLayoutWeight(R.string.lists_weight_sls, R.string.shopping_list_weight_sls, R.string.item_weight_sls);
		}
	}

	@Override
	public boolean onCreateViewListener(Toolbar toolbar) {

		if (!mState.isLandscape()) {
			return true;
		} else {
			mToggle = new ActionBarDrawerToggle(
					mState.getListsActivity(), mState.getListsActivity().getDrawerLayout(), toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
			mState.getListsActivity().getDrawerLayout().addDrawerListener(mToggle);
			mToggle.syncState();

			mToggle.setDrawerIndicatorEnabled(false);
			return false;
		}
	}

	@Override
	public ShoppingListFragment getFragment() {
		return mShoppingListFragment;
	}

	@Override
	public void onMainSelected() {
		if (!mState.isLandscape()) {
			onBackPressed();
		}
	}

	@Override
	public void onLastListSelected() {

	}

	@Override
	public boolean onBackPressed() {
		boolean result = false;

		if (mState.isLandscape()) {
			result = true;
		} else {
			mState.toScreen(mState.getListsScreen());
			mState.closeShowcase();

			mState.getListsScreen().getFragment().setOnListChangedListener(null);
		}

		return result;
	}

	@Override
	public int getScreenId() {
		return SCREEN_ID;
	}

	@Override
	public void toScreen() {
		NavigationView navigationView = (NavigationView) mState.getListsActivity().findViewById(R.id.nav_view);
		navigationView.setCheckedItem(R.id.nav_last_list);

		mState.setLastSelectedItemId(-1);

		mState.animation(Float.valueOf(mState.getListsActivity().getString(R.string.lists_weight_sls)),
				Float.valueOf(mState.getListsActivity().getString(R.string.shopping_list_weight_sls)),
				Float.valueOf(mState.getListsActivity().getString(R.string.item_weight_sls)));
	}

	@Override
	public void closeActionMode() {
		mShoppingListFragment.closeActionMode();
	}

	@Override
	public void onOpenDialog(long idList) {
		onOpenDialog(mShoppingListFragment, idList);

		if (mState.getLastSelectedListId() != idList) {
			mState.setShouldBackPressed(TabletState.DIALOG_BEHAVIOUR_LIST);
		}
	}

	@Override
	public void onListClick(long id) {
		mShoppingListFragment.closeActionMode();

		mState.openList(id, false);
		mState.toScreen(this);
	}

	@Override
	public void onListUpdated(long id) {
		if (mState.getLastSelectedListId() == id) {
			mShoppingListFragment.updateList();
		}
	}

	@Override
	public void onListUpdated() {
		mState.getListsScreen().updateList();
	}

	@Override
	public void onListDeleted(long idDeletedList, long idNewList) {
		if (mState.getLastSelectedListId() == idDeletedList) {
			if (idNewList == -1) {
				mState.toScreen(mState.getListsScreen());
			} else {
				mState.openList(idNewList, true);
			}
		}
	}

	@Override
	public void onListDeleted(long idDeletedList) {
		if (mState.isLandscape()) {
			deleteList(idDeletedList);
		} else {
			onBackPressed();
		}

		mState.getListsScreen().updateList();
	}

	@Override
	public void onShowCaseShown() {
		mShoppingListFragment.showCaseView();
	}

	public void openScreen() {
		mState.setCurrentScreen(this);
		mState.setLayoutWeight(R.string.lists_weight_sls, R.string.shopping_list_weight_sls, R.string.item_weight_sls);
	}

	void showDrawerIcon() {
		if (mState.isLandscape()) {
			mToggle.setDrawerIndicatorEnabled(true);
		}
	}

	void hideDrawerIcon() {
		if (mState.isLandscape()) {
			mToggle.setDrawerIndicatorEnabled(false);
		}
	}
}
