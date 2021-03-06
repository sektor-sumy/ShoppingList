package ru.android.ainege.shoppinglist.ui.activities.Lists;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.OnDialogShownListener;
import ru.android.ainege.shoppinglist.ui.activities.Lists.screen.ItemScreen;
import ru.android.ainege.shoppinglist.ui.activities.Lists.screen.ListsScreen;
import ru.android.ainege.shoppinglist.ui.activities.Lists.screen.ShoppingListScreen;
import ru.android.ainege.shoppinglist.ui.activities.Lists.screen.TabletScreen;
import ru.android.ainege.shoppinglist.ui.fragments.list.ListsFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class TabletState implements StateInterface, OnDialogShownListener,
		ShoppingListFragment.OnListChangedListener, ShoppingListFragment.OnClickListener {
	public static final String SHOPPING_LIST_TAG = "shopping_list_tag";
	public static final String ITEM_TAG = "item_tag";

	public static final int DIALOG_BEHAVIOUR_DEFAULT = 0;
	public static final int DIALOG_BEHAVIOUR_LIST = 1;
	public static final int DIALOG_BEHAVIOUR_ITEM = 2;

	private static final String STATE_SCREEN = "state_screen";
	private static final String STATE_LAST_LIST_ID = "state_last_list_id";
	private static final String STATE_LAST_ITEM_ID = "state_last_item_id";
	private static final String STATE_SHOULD_BACK_PRESSED = "state_should_back_pressed";

	private ListsActivity mListsActivity;

	private FrameLayout mListsLayout;
	private FrameLayout mShoppingListLayout;
	private FrameLayout mItemLayout;
	private boolean mIsLandscape = false;

	private TabletScreen mCurrentScreen;
	private ListsScreen mListsScreen;
	private ShoppingListScreen mShoppingListScreen;
	private ItemScreen mItemScreen;
	private long mLastSelectedListId = -1;
	private long mLastSelectedItemId = -1;
	private String mFragmentTagForRemove;
	private int mShouldBackPressed;

	public TabletState(ListsActivity listsActivity) {
		mListsActivity = listsActivity;
		mItemScreen = new ItemScreen(this);
		mShoppingListScreen = new ShoppingListScreen(this);
		mCurrentScreen = mListsScreen = new ListsScreen(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mListsLayout = (FrameLayout) mListsActivity.findViewById(R.id.fragment_container);
		mShoppingListLayout = (FrameLayout) mListsActivity.findViewById(R.id.list_fragment_container);
		mItemLayout = (FrameLayout) mListsActivity.findViewById(R.id.item_fragment_container);

		mIsLandscape = mListsActivity.getResources().getBoolean(R.bool.isLandscape);

		if (savedInstanceState == null) {
			boolean lists_select = mListsActivity.getIntent().getBooleanExtra(ListsActivity.LISTS_SELECT, false);
			boolean last_list_select = mListsActivity.getIntent().getBooleanExtra(ListsActivity.LAST_LIST_SELECT, false);

			if (mIsLandscape) {
				openList();
			} else if (last_list_select || !lists_select && mListsActivity.shouldOpenLastList()) {
				openLastList();
			}
		} else {
			ListsFragment fragment = (ListsFragment) mListsActivity.getSupportFragmentManager().findFragmentByTag(ListsActivity.LISTS_TAG);
			mListsScreen.setFragment(fragment);
			mLastSelectedListId = savedInstanceState.getLong(STATE_LAST_LIST_ID);
			mLastSelectedItemId = savedInstanceState.getLong(STATE_LAST_ITEM_ID);
			mShouldBackPressed = savedInstanceState.getInt(STATE_SHOULD_BACK_PRESSED);
			int idScreen = savedInstanceState.getInt(STATE_SCREEN);

			//for retained tablet with open dialog
			if (!mIsLandscape && mShouldBackPressed != DIALOG_BEHAVIOUR_DEFAULT) {
				idScreen--;
			} else if (mIsLandscape && mShouldBackPressed == DIALOG_BEHAVIOUR_ITEM) {
				idScreen++;
			}

			ShoppingListFragment shippingListFragment;

			switch (idScreen) {
				case ListsScreen.SCREEN_ID:
					mCurrentScreen = mListsScreen;
					break;
				case ShoppingListScreen.SCREEN_ID:
					mCurrentScreen = mShoppingListScreen;

					shippingListFragment = (ShoppingListFragment) getListsActivity().getSupportFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
					mShoppingListScreen.setListeners(shippingListFragment);
					break;
				case ItemScreen.SCREEN_ID:
					mCurrentScreen = mItemScreen;

					shippingListFragment = (ShoppingListFragment) getListsActivity().getSupportFragmentManager().findFragmentByTag(TabletState.SHOPPING_LIST_TAG);
					mShoppingListScreen.setListeners(shippingListFragment);

					ItemFragment itemFragment = (ItemFragment) getListsActivity().getSupportFragmentManager().findFragmentByTag(TabletState.ITEM_TAG);
					mItemScreen.setListeners(itemFragment);
					break;
			}

			mCurrentScreen.restore();
		}

		mListsActivity.getDrawerLayout().addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);

				if (!(mCurrentScreen instanceof ListsScreen)) {
					mShoppingListScreen.getFragment().setDrawerOpened(slideOffset != 0);
				}
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);

				if (!(mCurrentScreen instanceof ListsScreen)) {
					mShoppingListScreen.getFragment().closeActionMode();
				}
			}
		});
	}

	@Override
	public boolean onCreateViewListener(Fragment fragment, Toolbar toolbar) {
		boolean isSetIcon = true;

		if (fragment instanceof ListsFragment) {
			isSetIcon = mListsScreen.onCreateViewListener(toolbar);
		} else if (fragment instanceof ShoppingListFragment) {
			isSetIcon = mShoppingListScreen.onCreateViewListener(toolbar);
		} else if (fragment instanceof ItemFragment) {
			isSetIcon = mItemScreen.onCreateViewListener(toolbar);
		}

		return isSetIcon;
	}

	@Override
	public Fragment getFragment() {
		return mCurrentScreen.getFragment();
	}

	@Override
	public void setFragment(ListsFragment listsFragment) {
		mListsScreen.setFragment(listsFragment);
	}

	@Override
	public void onMainSelected() {
		mCurrentScreen.onMainSelected();
	}

	@Override
	public void onLastListSelected() {
		mCurrentScreen.onLastListSelected();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_SCREEN, mCurrentScreen.getScreenId());
		outState.putLong(STATE_LAST_LIST_ID, mLastSelectedListId);
		outState.putLong(STATE_LAST_ITEM_ID, mLastSelectedItemId);
		outState.putInt(STATE_SHOULD_BACK_PRESSED, mShouldBackPressed);
	}

	@Override
	public boolean onBackPressed() {
		return mCurrentScreen.onBackPressed();
	}

	@Override
	public void onOpenDialog(long id) {
		mCurrentScreen.onOpenDialog(id);
	}

	@Override
	public void onCloseDialog() {
		mCurrentScreen.onCloseDialog();
	}

	@Override
	public void onListClick(long id) {
		mCurrentScreen.onListClick(id);
	}

	@Override
	public void onListUpdated() {
		mCurrentScreen.onListUpdated();
	}

	@Override
	public void onListDeleted(long idDeletedList) {
		mCurrentScreen.onListDeleted(idDeletedList);
	}

	@Override
	public void onItemAdd(long id) {
		mCurrentScreen.onItemAdd(id);
	}

	@Override
	public void onItemSelect(ShoppingList item) {
		mCurrentScreen.onItemSelect(item);
	}

	public ListsActivity getListsActivity() {
		return mListsActivity;
	}

	public boolean isLandscape() {
		return mIsLandscape;
	}

	public void setCurrentScreen(TabletScreen currentScreen) {
		mCurrentScreen = currentScreen;
	}

	public TabletScreen getCurrentScreen() {
		return mCurrentScreen;
	}

	public ListsScreen getListsScreen() {
		return mListsScreen;
	}

	public ShoppingListScreen getShoppingListScreen() {
		return mShoppingListScreen;
	}

	public ItemScreen getItemScreen() {
		return mItemScreen;
	}

	public long getLastSelectedListId() {
		return mLastSelectedListId;
	}

	public void setLastSelectedListId(long lastSelectedListId) {
		mLastSelectedListId = lastSelectedListId;
	}

	public long getLastSelectedItemId() {
		return mLastSelectedItemId;
	}

	public void setLastSelectedItemId(long lastSelectedItemId) {
		mLastSelectedItemId = lastSelectedItemId;
	}

	public void setFragmentTagForRemove(String fragmentTagForRemove) {
		mFragmentTagForRemove = fragmentTagForRemove;
	}

	public void setShouldBackPressed(int shouldBackPressed) {
		mShouldBackPressed = shouldBackPressed;
	}

	public void closeShowcase() {
		View v = mListsActivity.findViewById(R.id.content_box);

		if (v != null) {
			MaterialShowcaseView v1 = (MaterialShowcaseView) v.getParent();
			v1.removeFromWindow();
		}
	}

	public void openItem(long idItem, Fragment fragment) {
		mLastSelectedItemId = idItem;
		mListsActivity.injectFragmentToUI(R.id.item_fragment_container, fragment, ITEM_TAG);
		toScreen(mItemScreen);
	}

	public void openList() {
		if (!openLastList()) {  //if not open last opened list, open first in list
			final ListsFragment listsFragment = mListsScreen.getFragment();
			ArrayList<List> lists = listsFragment.getLists();

			if (lists == null || lists.isEmpty()) {
				listsFragment.setOnListsLoadListener(new ListsFragment.OnListsLoadFinishedListener() {
					@Override
					public void onLoadFinished(ArrayList<List> lists) {
						mShoppingListScreen.openScreen();
						openList(lists.get(0).getId(), true);
						listsFragment.setOnListsLoadListener(null);
					}
				});
			} else {
				mShoppingListScreen.openScreen();
				openList(lists.get(0).getId(), true);
			}
		}
	}

	public void openList(long id, boolean isScrollToList) {
		mLastSelectedListId = id;
		ShoppingListFragment fragment = ShoppingListFragment.newInstance(id);
		mShoppingListScreen.setListeners(fragment);
		mListsActivity.injectFragmentToUI(R.id.list_fragment_container, fragment, SHOPPING_LIST_TAG);

		if (isScrollToList) {
			mListsScreen.getFragment().scrollToList(id);
		}
	}

	public boolean openLastList() {
		long id = mListsActivity.getSavedListId();
		boolean result = false;

		if (id != -1) {
			mShoppingListScreen.openScreen();
			openList(id, false);
			result = true;
		}

		return result;
	}

	//<editor-fold desc="Animation">
	public void toScreen(TabletScreen screen) {
		mCurrentScreen = screen;
		mCurrentScreen.toScreen();
	}

	public void setLayoutWeight(int listsWeight, int shoppingListWeight, int itemWeight) {
		new ViewWeightAnimationWrapper(mListsLayout).setWeight(Float.valueOf(mListsActivity.getString(listsWeight)));
		new ViewWeightAnimationWrapper(mShoppingListLayout).setWeight(Float.valueOf(mListsActivity.getString(shoppingListWeight)));
		new ViewWeightAnimationWrapper(mItemLayout).setWeight(Float.valueOf(mListsActivity.getString(itemWeight)));
	}

	public void animation(final float lists, final float list, final float item) {
		final ViewWeightAnimationWrapper listsWrapper = new ViewWeightAnimationWrapper(mListsLayout);
		ObjectAnimator listsAnim = ObjectAnimator.ofFloat(listsWrapper,
				"weight",
				listsWrapper.getWeight(),
				lists);

		final ViewWeightAnimationWrapper shoppingListWrapper = new ViewWeightAnimationWrapper(mShoppingListLayout);
		ObjectAnimator shoppingListAnim = ObjectAnimator.ofFloat(shoppingListWrapper,
				"weight",
				shoppingListWrapper.getWeight(),
				list);

		final ViewWeightAnimationWrapper itemWrapper = new ViewWeightAnimationWrapper(mItemLayout);
		ObjectAnimator itemAnim = ObjectAnimator.ofFloat(itemWrapper,
				"weight",
				itemWrapper.getWeight(),
				item);

		final AnimatorSet an = new AnimatorSet();
		an.playTogether(listsAnim, shoppingListAnim, itemAnim);
		an.setDuration(300);
		an.start();

		an.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);

				if (listsWrapper.getWeight() < lists ||
						shoppingListWrapper.getWeight() < list ||
						itemWrapper.getWeight() < item) {
					animation(lists, list, item);
				} else if (mFragmentTagForRemove != null) {
					mListsActivity.removeFragmentFromUI(mFragmentTagForRemove);
					mFragmentTagForRemove = null;
				}
			}
		});
	}
	//</editor-fold>

	private class ViewWeightAnimationWrapper {

		private View view;

		ViewWeightAnimationWrapper(View view) {
			if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
				this.view = view;
			} else {
				throw new IllegalArgumentException("The view should have LinearLayout as parent");
			}
		}

		float getWeight() {
			return ((LinearLayout.LayoutParams) view.getLayoutParams()).weight;
		}

		void setWeight(float weight) {
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
			params.weight = weight;
			view.setLayoutParams(params);
		}
	}
}
