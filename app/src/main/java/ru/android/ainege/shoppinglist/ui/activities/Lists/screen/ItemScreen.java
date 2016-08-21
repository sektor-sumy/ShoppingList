package ru.android.ainege.shoppinglist.ui.activities.Lists.screen;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Toast;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;

import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.activities.Lists.TabletState;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;

public class ItemScreen extends TabletScreen implements ItemFragment.OnClickListener,
		ShoppingListFragment.OnItemChangedListener, ItemFragment.OnItemChangedListener {
	public static final int SCREEN_ID = 3;

	private OnBackPressedListener mOnBackPressedListener;

	private long mIdForAdd = -1;
	private ShoppingList mItemForEdit = null;

	private ShoppingListFragment mShoppingListFragment;
	private ItemFragment mItemFragment;

	public ItemScreen(TabletState state) {
		super(state);
	}

	public void setListeners(ItemFragment fragment) {
		mShoppingListFragment = (ShoppingListFragment) mState.getListsActivity().getFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
		mShoppingListFragment.setOnItemChangedListener(this);

		mItemFragment = fragment;
		mItemFragment.setListeners(this, this);
		mOnBackPressedListener = mItemFragment;

		mState.getListsActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	@Override
	public void restore() {
		mState.setLayoutWeight(R.string.lists_weight_is, R.string.shopping_list_weight_is, R.string.item_weight_is);
	}

	@Override
	public ItemFragment getFragment() {
		return mItemFragment;
	}

	@Override
	public void onMainSelected() {
		if (mState.isLandscape()) {
			onBackPressed();
		} else {
			mState.toScreen(mState.getListsScreen());
		}
		
	}

	@Override
	public void onLastListSelected() {
		if (!mState.isLandscape()) {
			onBackPressed();
		}
	}

	@Override
	public boolean onBackPressed() {
		if (!isItemChanged()) {
			toPreviousScreen();
		}

		return false;
	}

	@Override
	public int getScreenId() {
		return SCREEN_ID;
	}

	@Override
	public void toScreen() {
		mState.animation(Float.valueOf(mState.getListsActivity().getString(R.string.lists_weight_is)),
				Float.valueOf(mState.getListsActivity().getString(R.string.shopping_list_weight_is)),
				Float.valueOf(mState.getListsActivity().getString(R.string.item_weight_is)));
	}

	@Override
	public void onOpenDialog(long idList) {
		onOpenDialog(mShoppingListFragment, idList);
		mState.setShouldBackPressed(TabletState.DIALOG_BEHAVIOUR_ITEM);
	}

	@Override
	public void onListClick(long id) {

	}

	@Override
	public void onListUpdated() {

	}

	@Override
	public void onListDeleted(long idDeletedList) {
		if (deleteList(idDeletedList)) {
			mState.toScreen(mState.getShoppingListScreen());
		}

		mState.setFragmentTagForRemove(TabletState.ITEM_TAG);
		mState.setLastSelectedItemId(-1);

		mState.getListsScreen().updateList();
	}

	@Override
	public void onItemSave(long id, boolean isAdded, boolean isClose) {
		mShoppingListFragment.setItemDetailsId(id);
		mShoppingListFragment.updateData();

		if (mState.isLandscape()) {
			Toast.makeText(mState.getListsActivity(), mState.getListsActivity().getString(R.string.data_save), Toast.LENGTH_SHORT).show();

			if (mIdForAdd != -1) {
				super.onItemAdd(mIdForAdd);
				mIdForAdd = -1;
			} else if (mItemForEdit != null) {
				super.onItemSelect(mItemForEdit);
				mItemForEdit = null;
			} else if (isClose) {
				toPreviousScreen();
			} else if (isAdded) {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mState.getListsActivity());
				boolean isItemShowcaseFired = new MaterialShowcaseSequence(mState.getListsActivity(), Showcase.SHOT_ITEM_IN_LIST).hasFired();
				boolean isCategoryShowcaseFired = new MaterialShowcaseSequence(mState.getListsActivity(), Showcase.SHOT_CATEGORY).hasFired();
				boolean isCollapseCategoryShowcaseFired = new MaterialShowcaseSequence(mState.getListsActivity(), Showcase.SHOT_CATEGORY_COLLAPSE).hasFired();

				if (!isItemShowcaseFired ||
						(!isCategoryShowcaseFired && sharedPref.getBoolean(mState.getListsActivity().getString(R.string.settings_key_use_category), true)) ||
						(!isCollapseCategoryShowcaseFired && sharedPref.getBoolean(mState.getListsActivity().getString(R.string.settings_key_collapse_category), true))) {
					toPreviousScreen();
				} else {
					super.onItemAdd(mState.getLastSelectedListId());
				}
			}
		} else {
			toPreviousScreen();
		}
	}

	@Override
	public void onImageClick() {
		mShoppingListFragment.closeActionMode();
	}

	@Override
	public void onNotSave() {
		if (mIdForAdd != -1) {
			super.onItemAdd(mIdForAdd);
			mIdForAdd = -1;
		} else if (mItemForEdit != null) {
			super.onItemSelect(mItemForEdit);
			mItemForEdit = null;
		} else {
			toPreviousScreen();
		}
	}

	@Override
	public void onItemAdd(long idList) {
		if (isItemChanged()) {
			mIdForAdd = idList;
		} else {
			super.onItemAdd(idList);
		}
	}

	@Override
	public void onItemSelect(ShoppingList item) {
		if (isItemChanged()) {
			mItemForEdit = item;
		} else {
			super.onItemSelect(item);
		}
	}

	@Override
	public void onItemSetBought(ShoppingList item) {
		if (mState.getLastSelectedItemId() == item.getIdItem()) {
			mItemFragment.setIsBought(item.isBought());
		}
	}

	@Override
	public void onItemDelete() {
		toPreviousScreen();
	}

	@Override
	public void updateCatalogs(String catalogKey) {
		if (catalogKey.equals(mState.getListsActivity().getString(R.string.catalogs_key_currency))) {
			mItemFragment.setCurrency();
		}
	}

	public void updateCurrentList() {
		mShoppingListFragment.setList();
		mShoppingListFragment.updateData();
	}

	private boolean isItemChanged() {
		return !(mOnBackPressedListener == null || (mOnBackPressedListener != null && mOnBackPressedListener.onBackPressed()));
	}

	private void toPreviousScreen() {
		mItemFragment.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		mState.getListsScreen().updateList();
		mState.toScreen(mState.getShoppingListScreen());
		mState.closeShowcase();
		mState.setFragmentTagForRemove(TabletState.ITEM_TAG);

		mShoppingListFragment.setOnItemChangedListener(null);
	}
}
