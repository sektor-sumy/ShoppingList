package ru.android.ainege.shoppinglist.ui.activities.lists.screen;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;

import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.activities.lists.TabletState;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;

public class ItemScreen extends TabletScreen implements ItemFragment.OnClickListener,
		ShoppingListFragment.OnClickListener, ShoppingListFragment.OnListChangedListener,
		ShoppingListFragment.OnItemChangedListener, ItemFragment.OnItemChangedListener {
	public static final int SCREEN_ID = 3;

	private OnBackPressedListener mOnBackPressedListener;

	private long mIdForAdd = -1;
	private ShoppingList mItemForEdit = null;

	public ItemScreen(TabletState state) {
		super(state);
	}

	@Override
	public void restore() {
		mState.setLayoutWeight(R.string.lists_weight_is, R.string.shopping_list_weight_is, R.string.item_weight_is);
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
		super.onOpenDialog(idList);
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

		mState.updateList();
	}

	@Override
	public void onItemSave(long id, boolean isAdded, boolean isClose) {
		ShoppingListFragment listFragment = (ShoppingListFragment) mState.getListsActivity().getFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
		listFragment.setItemDetailsId(id);
		listFragment.updateData();

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
					onItemAdd(mState.getLastSelectedListId());
				}
			}
		} else {
			toPreviousScreen();
		}
	}

	@Override
	public void onImageClick() {
		ShoppingListFragment listFragment = (ShoppingListFragment) mState.getListsActivity().getFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
		if (listFragment != null) listFragment.closeActionMode();
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
			ItemFragment itemFragment = (ItemFragment) mState.getListsActivity().
					getFragmentManager().findFragmentByTag(TabletState.ITEM_TAG);
			itemFragment.setIsBought(item.isBought());
		}
	}

	@Override
	public void onItemDelete() {
		toPreviousScreen();
	}

	@Override
	public void updateItem(String setting) {
		if (mState.isLandscape()) {
			ItemFragment fr = (ItemFragment) mState.getListsActivity().
					getFragmentManager().findFragmentByTag(TabletState.ITEM_TAG);

			if (setting == null) {
				fr.setCurrency();
				fr.setUnit(-1);
				fr.setCategory(-1);
			} else if (setting.equals(mState.getListsActivity().getString(R.string.settings_key_transition))) {
				fr.setTransitionButtons();
			} else if (setting.equals(mState.getListsActivity().getString(R.string.settings_key_fast_edit))) {
				fr.updateSpinners();
			} else if (setting.equals(mState.getListsActivity().getString(R.string.settings_key_use_category))) {
				fr.setCategory();
			} else if (setting.equals(mState.getListsActivity().getString(R.string.settings_key_currency))) {
				fr.setCurrency();
			}
		}
	}

	public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
		mOnBackPressedListener = onBackPressedListener;
	}

	private boolean isItemChanged() {
		return !(mOnBackPressedListener == null || (mOnBackPressedListener != null && mOnBackPressedListener.onBackPressed()));
	}

	private void toPreviousScreen() {
		mState.updateList();
		mState.toScreen(mState.getShoppingListScreen());
		mState.closeShowcase();
		mState.setFragmentTagForRemove(TabletState.ITEM_TAG);
	}
}