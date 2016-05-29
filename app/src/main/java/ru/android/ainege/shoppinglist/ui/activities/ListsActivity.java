package ru.android.ainege.shoppinglist.ui.activities;

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

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.AddItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.EditItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;

public class ListsActivity extends SingleFragmentActivity implements ListsFragment.OnListsChangeListener,
		ShoppingListFragment.OnUpdateListListener, ShoppingListFragment.OnListChangeListener,
		ItemFragment.OnItemChangeListener {
	public static final String APP_PREFERENCES = "shopping_list_settings";
	public static final String APP_PREFERENCES_ID = "idList";
	private static final String STATE_SCREEN = "state_screen";

	private static final int HANDSET = -1;
	private static final int LISTS_SCREEN = 1;
	private static final int SHOPPING_LIST_SCREEN = 2;
	private static final int ITEM_SCREEN = 3;

	private FrameLayout mListsLayout;
	private FrameLayout mShoppingListLayout;
	private FrameLayout mItemLayout;

	private boolean mIsTablet = false;
	private int mCurrentScreen = HANDSET;
	private long mLastSelectedListId = -1;
	private long mLastSelectedItemId = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mListsLayout = (FrameLayout) findViewById(R.id.fragment_container);
		mShoppingListLayout = (FrameLayout) findViewById(R.id.list_fragment_container);
		mIsTablet = mShoppingListLayout != null;

		if (mIsTablet) {
			mItemLayout = (FrameLayout) findViewById(R.id.item_fragment_container);
			mCurrentScreen = LISTS_SCREEN;
		}

		if (savedInstanceState == null && shouldOpenLastList()) {
			openLastList();
		} else if (savedInstanceState != null) {
			mCurrentScreen = savedInstanceState.getInt(STATE_SCREEN);

			switch (mCurrentScreen) {
				case ITEM_SCREEN:
					new ViewWeightAnimationWrapper(mListsLayout).setWeight(Float.valueOf(getString(R.string.lists_weight_is)));
					new ViewWeightAnimationWrapper(mShoppingListLayout).setWeight(Float.valueOf(getString(R.string.shopping_list_weight_is)));
					new ViewWeightAnimationWrapper(mItemLayout).setWeight(Float.valueOf(getString(R.string.item_weight_is)));

					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentById(R.id.list_fragment_container);
						listFragment.notOpenActionMode();
					}
					break;
				case SHOPPING_LIST_SCREEN:
					new ViewWeightAnimationWrapper(mListsLayout).setWeight(Float.valueOf(getString(R.string.lists_weight_sls)));
					new ViewWeightAnimationWrapper(mShoppingListLayout).setWeight(Float.valueOf(getString(R.string.shopping_list_weight_sls)));
					new ViewWeightAnimationWrapper(mItemLayout).setWeight(Float.valueOf(getString(R.string.item_weight_sls)));
					break;
				case LISTS_SCREEN:
					new ViewWeightAnimationWrapper(mListsLayout).setWeight(Float.valueOf(getString(R.string.lists_weight_ls)));
					new ViewWeightAnimationWrapper(mShoppingListLayout).setWeight(Float.valueOf(getString(R.string.shopping_list_weight_ls)));
					new ViewWeightAnimationWrapper(mItemLayout).setWeight(Float.valueOf(getString(R.string.item_weight_ls)));
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

		return new ListsFragment();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(STATE_SCREEN, mCurrentScreen);
	}

	@Override
	public void onBackPressed() {
		switch (mCurrentScreen) {
			case ITEM_SCREEN:
				updateList();
				closeKeyboard();
				toShoppingListScreen();
				break;
			case SHOPPING_LIST_SCREEN:
				toListsScreen();
				break;
			default:
				super.onBackPressed();
		}
	}

	@Override
	public void onListSelect(long id) {
		if (mIsTablet) {
			mLastSelectedListId = id;
			injectFragment(ShoppingListFragment.newInstance(id), R.id.list_fragment_container);
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
			ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentById(R.id.list_fragment_container);
			listFragment.updateList();
		}
	}

	@Override
	public void onListDelete(long id) {
		if (mCurrentScreen == SHOPPING_LIST_SCREEN && mLastSelectedListId == id) {
			onBackPressed();
		}
	}

	@Override
	public boolean isLandscapeTablet() {
		if (mIsTablet && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return true;
		}

		return false;
	}

	@Override
	public void onListUpdate() {
		if (mCurrentScreen == SHOPPING_LIST_SCREEN) {
			updateList();
		}
	}

	@Override
	public void onListDelete() {
		if (mIsTablet) {
			updateList();

			if (mCurrentScreen == SHOPPING_LIST_SCREEN) {
				onBackPressed();
			} else if (mCurrentScreen == ITEM_SCREEN) {
				toListsScreen();
			}
		}
	}

	@Override
	public void onItemAdd(long id) {
		if (mIsTablet) {
			mLastSelectedItemId = 0;
			openItem(AddItemFragment.newInstance(id));
		}
	}

	@Override
	public void onItemSelect(ShoppingList item) {
		if (mIsTablet) {
			mLastSelectedItemId = item.getIdItem();
			openItem(EditItemFragment.newInstance(item));
		}
	}

	@Override
	public void onItemSetBought(ShoppingList item) {
		if (mCurrentScreen == ITEM_SCREEN && mLastSelectedItemId == item.getIdItem()) {
			ItemFragment itemFragment = (ItemFragment) getFragmentManager().findFragmentById(R.id.item_fragment_container);
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
	public long getLastSelectedItemId() {
		return mLastSelectedItemId;
	}

	@Override
	public void onItemSave(long id) {
		if (mIsTablet) {
			mLastSelectedItemId = id;
			ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentById(R.id.list_fragment_container);
			listFragment.updateData();

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
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

	private void openLastList() {
		SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		long id = mSettings.getLong(APP_PREFERENCES_ID, -1);

		if (id != -1) {
			if (mIsTablet) {
				mCurrentScreen = SHOPPING_LIST_SCREEN;
				injectFragment(ShoppingListFragment.newInstance(id), R.id.list_fragment_container);

				new ViewWeightAnimationWrapper(mListsLayout).setWeight(Float.valueOf(getString(R.string.lists_weight_sls)));
				new ViewWeightAnimationWrapper(mShoppingListLayout).setWeight(Float.valueOf(getString(R.string.shopping_list_weight_sls)));
			} else {
				Intent i = new Intent(this, ShoppingListActivity.class);
				i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);
				startActivity(i);
			}
		}
	}

	private void closeKeyboard() {
		View view = this.getCurrentFocus();

		if (view != null) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	private void updateList() {
		ListsFragment listFragment = (ListsFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
		listFragment.updateData();
	}

	private void openItem(Fragment fragment) {
		injectFragment(fragment, R.id.item_fragment_container);
		toItemScreen();
	}

	private void toListsScreen() {
		mCurrentScreen = LISTS_SCREEN;
		mLastSelectedListId = -1;

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

	private void animation(float lists, float list, float item) {
		ViewWeightAnimationWrapper listsWrapper = new ViewWeightAnimationWrapper(mListsLayout);
		ObjectAnimator listsAnim = ObjectAnimator.ofFloat(listsWrapper,
				"weight",
				listsWrapper.getWeight(),
				lists);

		ViewWeightAnimationWrapper shoppingListWrapper = new ViewWeightAnimationWrapper(mShoppingListLayout);
		ObjectAnimator shoppingListAnim = ObjectAnimator.ofFloat(shoppingListWrapper,
				"weight",
				shoppingListWrapper.getWeight(),
				list);

		ViewWeightAnimationWrapper itemWrapper = new ViewWeightAnimationWrapper(mItemLayout);
		ObjectAnimator itemAnim = ObjectAnimator.ofFloat(itemWrapper,
				"weight",
				itemWrapper.getWeight(),
				item);

		AnimatorSet an = new AnimatorSet();
		an.playTogether(listsAnim, shoppingListAnim, itemAnim);
		an.setDuration(300);
		an.start();
	}

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
