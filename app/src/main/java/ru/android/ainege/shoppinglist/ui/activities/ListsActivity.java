package ru.android.ainege.shoppinglist.ui.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
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

	private FrameLayout mListsLayout;
	private FrameLayout mShoppingListLayout;
	private FrameLayout mItemLayout;

	private ViewWeightAnimationWrapper mListsWrapper;
	private ViewWeightAnimationWrapper mShoppingListWrapper;
	private ViewWeightAnimationWrapper mItemWrapper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null && shouldOpenLastList()) {
			openLastList();
		}

		if (findViewById(R.id.list_fragment_container) != null) {
			mListsLayout = (FrameLayout) findViewById(R.id.fragment_container);
			mShoppingListLayout = (FrameLayout) findViewById(R.id.list_fragment_container);
			mItemLayout = (FrameLayout) findViewById(R.id.item_fragment_container);

			mListsWrapper = new ViewWeightAnimationWrapper(mListsLayout);
			mShoppingListWrapper = new ViewWeightAnimationWrapper(mShoppingListLayout);
			mItemWrapper = new ViewWeightAnimationWrapper(mItemLayout);
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
			if (findViewById(R.id.list_fragment_container) == null) {
				Intent i = new Intent(this, ShoppingListActivity.class);
				i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);
				startActivity(i);
			} else {
				Fragment newDetail = ShoppingListFragment.newInstance(id);
				injectFragment(newDetail, R.id.list_fragment_container);

				mListsWrapper.setWeight(0.33f);
				mShoppingListWrapper.setWeight(0.67f);
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

	@Override
	public void onBackPressed() {
		if (mShoppingListLayout != null) {
			if (mListsWrapper.getWeight() > 0 && mShoppingListWrapper.getWeight() > 0) { //from lists/shoppingList to lists
				toLists();
			} else if (mListsWrapper.getWeight() == 0) { //from shoppingList/item to lists/shoppingList
				updateList();
				toShoppingList();
			} else {
				super.onBackPressed();
			}
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onListSelected(long id) {
		if (mShoppingListLayout == null) {
			Intent i = new Intent(this, ShoppingListActivity.class);
			i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
			} else {
				startActivity(i);
			}
		} else {
			Fragment newDetail = ShoppingListFragment.newInstance(id);
			injectFragment(newDetail, R.id.list_fragment_container);

			toShoppingList();
		}
	}

	@Override
	public void onListUpdate() {
		if (mShoppingListLayout != null && mShoppingListWrapper.getWeight() > 0) {
			ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentById(R.id.list_fragment_container);
			listFragment.updateList();
		}
	}

	@Override
	public void onShoppingListUpdate() {
		if (mListsLayout != null && mListsWrapper.getWeight() > 0) {
			updateList();
		}
	}

	@Override
	public void onAddItem(long id) {
		openItem( AddItemFragment.newInstance(id));
	}

	@Override
	public void onItemSelected(ShoppingList item) {
		if (item != null && mItemWrapper.getWeight() > 0) {
			openItem(EditItemFragment.newInstance(item));
		}
	}

	@Override
	public void onDeleteShoppingList() {
		if (mShoppingListLayout != null) {
			if (mListsWrapper.getWeight() > 0) {
				onBackPressed();
			} else {
				updateList();
				toLists();
			}

			onShoppingListUpdate();
		}
	}

	@Override
	public void onItemSave(long id) {
		if (mShoppingListLayout != null) {
			ShoppingListFragment listFragment = (ShoppingListFragment) getFragmentManager().findFragmentById(R.id.list_fragment_container);
			listFragment.updateData();
			listFragment.setLastOpenItem(id);
		}
	}

	private void updateList() {
		ListsFragment listFragment = (ListsFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
		listFragment.updateData();
	}

	private void openItem(Fragment fragment) {
		if (mShoppingListLayout != null) {
			injectFragment(fragment, R.id.item_fragment_container);

			toItem();
		}
	}

	private void toLists() {
		animation(1, 0, 0);
	}

	private void toShoppingList() {
		animation(0.33f, 0.67f, 0);
	}

	private void toItem() {
		animation(0, 0.5f, 0.5f);
	}

	private void animation(float lists, float list, float item) {
		ObjectAnimator listsAnim = ObjectAnimator.ofFloat(mListsWrapper,
				"weight",
				mListsWrapper.getWeight(),
				lists);

		ObjectAnimator shoppingListAnim = ObjectAnimator.ofFloat(mShoppingListWrapper,
				"weight",
				mShoppingListWrapper.getWeight(),
				list);

		ObjectAnimator itemAnim = ObjectAnimator.ofFloat(mItemWrapper,
				"weight",
				mItemWrapper.getWeight(),
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
