package ru.android.ainege.shoppinglist.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.AddItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.EditItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class ListsActivity extends SingleFragmentActivity implements ListsFragment.OnListsChangeListener,
		ShoppingListFragment.OnUpdateListListener, ShoppingListFragment.OnListChangeListener,
		ItemFragment.OnItemChangeListener {
	public static final String APP_PREFERENCES = "shopping_list_settings";
	public static final String APP_PREFERENCES_ID = "idList";
	private static final String STATE_SCREEN = "state_screen";
	private static final String STATE_LAST_LIST_ID = "state_last_list_id";
	private static final String STATE_LAST_ITEM_ID = "state_last_item_id";

	private static final String LISTS_TAG = "lists_tag";
	private static final String SHOPPING_LIST_TAG = "shopping_list_tag";
	private static final String ITEM_TAG = "item_tag";

	private static final int HANDSET = -1;
	private static final int LISTS_SCREEN = 1;
	private static final int SHOPPING_LIST_SCREEN = 2;
	private static final int ITEM_SCREEN = 3;

	private ListsFragment mListsFragment;

	private FrameLayout mListsLayout;
	private FrameLayout mShoppingListLayout;
	private FrameLayout mItemLayout;

	private boolean mIsTablet = false;
	private boolean mIsLandscapeTablet = false;
	private int mCurrentScreen = HANDSET;
	private long mLastSelectedListId = -1;
	private long mLastSelectedItemId = -1;
	private String mFragmentTagForRemove;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mListsLayout = (FrameLayout) findViewById(R.id.fragment_container);
		mIsTablet = getResources().getBoolean(R.bool.isTablet);

		if (mIsTablet) {
			mShoppingListLayout = (FrameLayout) findViewById(R.id.list_fragment_container);
			mItemLayout = (FrameLayout) findViewById(R.id.item_fragment_container);

			mCurrentScreen = LISTS_SCREEN;
			mIsLandscapeTablet = getResources().getBoolean(R.bool.isTablet) && getResources().getBoolean(R.bool.isLandscape);
		}

		if (savedInstanceState == null) {
			if (mIsLandscapeTablet) {
				openList();
			} else if (shouldOpenLastList()){
				openLastList();
			}
		} else if (savedInstanceState != null) {
			mCurrentScreen = savedInstanceState.getInt(STATE_SCREEN);
			mLastSelectedListId = savedInstanceState.getLong(STATE_LAST_LIST_ID);
			mLastSelectedItemId = savedInstanceState.getLong(STATE_LAST_ITEM_ID);
			mListsFragment = (ListsFragment) getFragmentManager().findFragmentByTag(LISTS_TAG);

			switch (mCurrentScreen) {
				case ITEM_SCREEN:
					new ViewWeightAnimationWrapper(mListsLayout).setWeight(Float.valueOf(getString(R.string.lists_weight_is)));
					new ViewWeightAnimationWrapper(mShoppingListLayout).setWeight(Float.valueOf(getString(R.string.shopping_list_weight_is)));
					new ViewWeightAnimationWrapper(mItemLayout).setWeight(Float.valueOf(getString(R.string.item_weight_is)));

					if (!mIsLandscapeTablet) {
						ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentByTag(SHOPPING_LIST_TAG);
						listFragment.notOpenActionMode();
					}
					break;
				case SHOPPING_LIST_SCREEN:
					if (!(new MaterialShowcaseSequence(this, Showcase.SHOT_LIST).hasFired())) {
						mCurrentScreen = LISTS_SCREEN;
						break;
					} else {
						new ViewWeightAnimationWrapper(mListsLayout).setWeight(Float.valueOf(getString(R.string.lists_weight_sls)));
						new ViewWeightAnimationWrapper(mShoppingListLayout).setWeight(Float.valueOf(getString(R.string.shopping_list_weight_sls)));
						new ViewWeightAnimationWrapper(mItemLayout).setWeight(Float.valueOf(getString(R.string.item_weight_sls)));
					}

					break;
				case LISTS_SCREEN:
					if (mIsLandscapeTablet) {
						openList();
					}
					break;
				default:
					break;
			}
		}
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_main;
	}

	@Override
	protected Fragment getFragment() {
		init();
		mListsFragment = new ListsFragment();

		return mListsFragment;
	}

	@Override
	protected String getTag() {
		return LISTS_TAG;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(STATE_SCREEN, mCurrentScreen);
		outState.putLong(STATE_LAST_LIST_ID, mLastSelectedListId);
		outState.putLong(STATE_LAST_ITEM_ID, mLastSelectedItemId);
	}

	@Override
	public void onBackPressed() {
		switch (mCurrentScreen) {
			case ITEM_SCREEN:
				updateList();
				toShoppingListScreen();
				closeShowcase();
				mFragmentTagForRemove = ITEM_TAG;
				break;
			case SHOPPING_LIST_SCREEN:
				if (mIsLandscapeTablet) {
					super.onBackPressed();
				} else {
					toListsScreen();
					closeShowcase();
				}
				break;
			default:
				super.onBackPressed();
		}
	}

	@Override
	public void onListSelect(long id) {
		if (mIsTablet) {
			openList(id, false);
			toShoppingListScreen();
		} else {
			Intent i = new Intent(this, ShoppingListActivity.class);
			i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
			} else {
				startActivity(i);
			}
		}
	}

	@Override
	public void onListUpdate(long id) {
		if (mCurrentScreen == SHOPPING_LIST_SCREEN && mLastSelectedListId == id) {
			ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentByTag(SHOPPING_LIST_TAG);
			listFragment.updateList();
		}
	}

	@Override
	public void onListDelete(long idDeletedList, long idNewList) {
		if (mCurrentScreen == SHOPPING_LIST_SCREEN && mLastSelectedListId == idDeletedList) {
			if (idNewList == -1) {
				toListsScreen();
			} else {
				openList(idNewList, true);
			}
		}
	}

	@Override
	public long getLastSelectedListId() {
		return mLastSelectedListId;
	}

	@Override
	public void onShowCaseShown() {
		if (mCurrentScreen == SHOPPING_LIST_SCREEN) {
			ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentByTag(SHOPPING_LIST_TAG);
			listFragment.showCaseView();
		}
	}

	@Override
	public void onListUpdate() {
		if (mCurrentScreen == SHOPPING_LIST_SCREEN) {
			updateList();
		}
	}

	@Override
	public void onListDelete(long idDeletedList) {
		if (mIsTablet) {
			if (mCurrentScreen == SHOPPING_LIST_SCREEN) {
				if (mIsLandscapeTablet) {
					ArrayList<List>  lists = mListsFragment.getLists();

					if (lists.size() == 1) { // it was last list
						toListsScreen();
					} else { //open first list
						long id = lists.get(0).getId();
						openList(id != idDeletedList ? id : lists.get(1).getId(), true);
					}
				} else {
					onBackPressed();
				}
			} else if (mCurrentScreen == ITEM_SCREEN) {
				ArrayList<List>  lists = mListsFragment.getLists();

				if (lists.size() == 1) { // it was last list
					toListsScreen();
				} else { //open first list
					long id = lists.get(0).getId();
					openList(id != idDeletedList ? id : lists.get(1).getId(), true);

					toShoppingListScreen();
				}

				mFragmentTagForRemove = ITEM_TAG;
				mLastSelectedItemId = -1;
			}

			updateList();
		}
	}

	@Override
	public void onItemAdd(long idList) {
		if (mIsTablet) {
			openItem(0, AddItemFragment.newInstance(idList));
		}
	}

	@Override
	public void onItemSelect(ShoppingList item) {
		if (mIsTablet) {
			openItem(item.getIdItem(), EditItemFragment.newInstance(item));
		}
	}

	@Override
	public void onItemSetBought(ShoppingList item) {
		if (mCurrentScreen == ITEM_SCREEN && mLastSelectedItemId == item.getIdItem()) {
			ItemFragment itemFragment = (ItemFragment) getFragmentManager().findFragmentByTag(ITEM_TAG);
			itemFragment.setIsBought(item.isBought());
		}
	}

	@Override
	public void onItemDelete() {
		if (mCurrentScreen == ITEM_SCREEN) {
			onBackPressed();
		}
	}

	@Override
	public void updateItem(String setting) {
		if (mIsLandscapeTablet && mCurrentScreen == ITEM_SCREEN) {
			ItemFragment fr = (ItemFragment) getFragmentManager().findFragmentByTag(ITEM_TAG);

			if (setting.equals(getString(R.string.settings_key_transition))) {
				fr.setTransitionButtons();
			} else if (setting.equals(getString(R.string.settings_key_fast_edit))) {
				fr.updateSpinners();
			} else if (setting.equals(getString(R.string.settings_key_use_category))) {
				fr.setCategory();
			}
		}
	}

	@Override
	public long getLastSelectedItemId() {
		return mLastSelectedItemId;
	}

	@Override
	public void onItemSave(long id) {
		if (mIsTablet) {
			ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentByTag(SHOPPING_LIST_TAG);
			listFragment.updateData();

			if (mIsLandscapeTablet) {
				onItemAdd(mLastSelectedListId);
			} else {
				onBackPressed();
			}
		}
	}

	private void init() {
		SharedPreferences sp = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

		if (sp.getBoolean("isFirst", false)) {
			return;
		}

		// app inited
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean("isFirst", true);
		e.apply();

		//set currency
		CurrenciesDS currenciesDS = new CurrenciesDS(this);
		CurrenciesDS.CurrencyCursor c = currenciesDS.getByName("Рубль");
		if (c.moveToFirst()) {
			long id = c.getEntity().getId();

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(getString(R.string.settings_key_currency), id);
			editor.apply();
		}

		//update ruble symbol on old device
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			currenciesDS.update("руб.", "\u20BD");
			currenciesDS.update("ман.", "\u20BC");
			currenciesDS.update("тг", "\u20B8");
		}
	}

	private boolean shouldOpenLastList() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isShould = false;

		if (prefs.getBoolean(getString(R.string.settings_key_open_last_list), false)) {
			SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
			isShould = mSettings.contains(APP_PREFERENCES_ID);
		}

		return isShould;
	}

	private boolean openLastList() {
		SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		long id = mSettings.getLong(APP_PREFERENCES_ID, -1);
		boolean result = false;

		if (id != -1) {
			if (mIsTablet) {
				setShoppingListScreen();
				openList(id, false);
			} else {
				Intent i = new Intent(this, ShoppingListActivity.class);
				i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);
				startActivity(i);
			}

			result = true;
		}

		return result;
	}

	private void openList() {
		if (!openLastList()) {  //if not open last opened list, open first in list
			ArrayList<List> lists = mListsFragment.getLists();

			if (lists == null) {
				mListsFragment.setOnListsLoadListener(new ListsFragment.OnListsLoadFinishedListener() {
					@Override
					public void onLoadFinished(ArrayList<List> lists) {
						setShoppingListScreen();
						openList(lists.get(0).getId(), true);
						mListsFragment.setOnListsLoadListener(null);
					}
				});
			} else {
				setShoppingListScreen();
				openList(lists.get(0).getId(), true);
			}
		}
	}

	private void setShoppingListScreen() {
		mCurrentScreen = SHOPPING_LIST_SCREEN;
		new ViewWeightAnimationWrapper(mListsLayout).setWeight(Float.valueOf(getString(R.string.lists_weight_sls)));
		new ViewWeightAnimationWrapper(mShoppingListLayout).setWeight(Float.valueOf(getString(R.string.shopping_list_weight_sls)));
	}

	private void openList(long id, boolean isScrollToList) {
		mLastSelectedListId = id;
		injectFragment(R.id.list_fragment_container, ShoppingListFragment.newInstance(id), SHOPPING_LIST_TAG);

		if (isScrollToList) {
			mListsFragment.scrollToList(id);
		}
	}

	private void updateList() {
		mListsFragment.updateData();
	}

	private void openItem(long idItem, Fragment fragment) {
		mLastSelectedItemId = idItem;
		injectFragment(R.id.item_fragment_container, fragment, ITEM_TAG);
		toItemScreen();
	}

	private void closeShowcase() {
		View v = findViewById(R.id.content_box);

		if (v != null) {
			MaterialShowcaseView v1 = (MaterialShowcaseView) v.getParent();
			v1.removeFromWindow();
		}
	}

	//<editor-fold desc="Animation">
	private void toListsScreen() {
		mCurrentScreen = LISTS_SCREEN;
		mLastSelectedListId = -1;
		mFragmentTagForRemove = SHOPPING_LIST_TAG;

		animation(Float.valueOf(getString(R.string.lists_weight_ls)),
				Float.valueOf(getString(R.string.shopping_list_weight_ls)),
				Float.valueOf(getString(R.string.item_weight_ls)));
	}

	private void toShoppingListScreen() {
		mCurrentScreen = SHOPPING_LIST_SCREEN;
		mLastSelectedItemId = -1;

		animation(Float.valueOf(getString(R.string.lists_weight_sls)),
				Float.valueOf(getString(R.string.shopping_list_weight_sls)),
				Float.valueOf(getString(R.string.item_weight_sls)));
	}

	private void toItemScreen() {
		mCurrentScreen = ITEM_SCREEN;

		animation(Float.valueOf(getString(R.string.lists_weight_is)),
				Float.valueOf(getString(R.string.shopping_list_weight_is)),
				Float.valueOf(getString(R.string.item_weight_is)));
	}

	private void animation(final float lists, final float list, final float item) {
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
					removeFragment(mFragmentTagForRemove);
					mFragmentTagForRemove = null;
				}
			}
		});
	}
	//</editor-fold>

	public class ViewWeightAnimationWrapper {

		private View view;

		public ViewWeightAnimationWrapper(View view) {
			if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
				this.view = view;
			} else {
				throw new IllegalArgumentException("The view should have LinearLayout as parent");
			}
		}

		public float getWeight() {
			return ((LinearLayout.LayoutParams) view.getLayoutParams()).weight;
		}

		public void setWeight(float weight) {
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
			params.weight = weight;
			view.setLayoutParams(params);
		}
	}
}
