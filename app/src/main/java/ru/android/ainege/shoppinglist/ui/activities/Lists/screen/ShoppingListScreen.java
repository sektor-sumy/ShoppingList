package ru.android.ainege.shoppinglist.ui.activities.lists.screen;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.activities.lists.TabletState;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;

public class ShoppingListScreen extends TabletScreen implements ShoppingListFragment.OnClickListener,
		ListsFragment.OnListChangedListener, ShoppingListFragment.OnListChangedListener {
	public static final int SCREEN_ID = 2;

	public ShoppingListScreen(TabletState state) {
		super(state);
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
	public boolean onBackPressed() {
		boolean result = false;

		if (mState.isLandscape()) {
			result = true;
		} else {
			mState.toScreen(mState.getListsScreen());
			mState.closeShowcase();
		}

		return result;
	}

	@Override
	public int getScreenId() {
		return SCREEN_ID;
	}

	@Override
	public void toScreen() {
		mState.setLastSelectedItemId(-1);

		mState.animation(Float.valueOf(mState.getListsActivity().getString(R.string.lists_weight_sls)),
				Float.valueOf(mState.getListsActivity().getString(R.string.shopping_list_weight_sls)),
				Float.valueOf(mState.getListsActivity().getString(R.string.item_weight_sls)));
	}

	@Override
	public void onOpenDialog(long idList) {
		super.onOpenDialog(idList);

		if (mState.getLastSelectedListId() != idList) {
			mState.setShouldBackPressed(TabletState.DIALOG_BEHAVIOUR_LIST);
		}
	}

	@Override
	public void onListClick(long id) {
		ShoppingListFragment listFragment = (ShoppingListFragment) mState.getListsActivity().
				getFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
		listFragment.closeActionMode();

		mState.openList(id, false);
		mState.toScreen(this);
	}

	@Override
	public void onListUpdated(long id) {
		if (mState.getLastSelectedListId() == id) {
			ShoppingListFragment listFragment = (ShoppingListFragment) mState.getListsActivity().
					getFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
			listFragment.updateList();
		}
	}

	@Override
	public void onListUpdated() {
		mState.updateList();
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

		mState.updateList();
	}

	@Override
	public void onShowCaseShown() {
		ShoppingListFragment listFragment = (ShoppingListFragment) mState.getListsActivity().
				getFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
		listFragment.showCaseView();
	}

	public void openScreen() {
		mState.setCurrentScreen(this);
		mState.setLayoutWeight(R.string.lists_weight_sls, R.string.shopping_list_weight_sls, R.string.item_weight_sls);
	}

}