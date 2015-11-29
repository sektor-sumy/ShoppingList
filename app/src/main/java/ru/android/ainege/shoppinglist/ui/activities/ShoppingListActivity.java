package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;

public class ShoppingListActivity extends SingleFragmentActivity {
	public final static String EXTRA_ID_LIST = "idList";

	private boolean mIsBoughtFirst;
	private String mDataSave;

	@Override
	protected Fragment getFragment() {
		getSettings();

		Intent intent = getIntent();
		long idList = intent.getLongExtra(EXTRA_ID_LIST, -1);

		return ShoppingListFragment.newInstance(idList, mIsBoughtFirst, mDataSave);
	}

	@Override
	public void onResume() {
		super.onResume();

		getSettings();
	}

	private void getSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		mIsBoughtFirst = prefs.getBoolean(getString(R.string.settings_key_sort_is_bought), true);
		String sortType;
		String regular = prefs.getString(getString(R.string.settings_key_sort_type), "");
		if (regular.contains(getString(R.string.sort_order_alphabet))) {
			sortType = ShoppingListDataSource.ALPHABET;
		} else if (regular.contains(getString(R.string.sort_order_up_price))) {
			sortType = ShoppingListDataSource.UP_PRICE;
		} else if (regular.contains(getString(R.string.sort_order_down_price))) {
			sortType = ShoppingListDataSource.DOWN_PRICE;
		} else if (regular.contains(getString(R.string.sort_order_adding))) {
			sortType = ShoppingListDataSource.ORDER_ADDING;
		} else {
			sortType = ShoppingListDataSource.ALPHABET;
		}
		ShoppingListDataSource itemsInListDS;
		try {
			itemsInListDS = ShoppingListDataSource.getInstance();
		} catch (NullPointerException e) {
			itemsInListDS = ShoppingListDataSource.getInstance(this);
		}
		itemsInListDS.setSortSettings(mIsBoughtFirst, sortType);

		if (!prefs.getBoolean(getString(R.string.settings_key_sort_is_default_data), true)) {
			mDataSave = SettingsDataItem.NOT_USE_DEFAULT_DATA;
		} else {
			String save = prefs.getString(getString(R.string.settings_key_sort_data_item), "");
			if (save.contains(getString(R.string.data_item_button))) {
				mDataSave = SettingsDataItem.SAVE_DATA_BUTTON;
			} else if (save.contains(getString(R.string.data_item_always))) {
				mDataSave = SettingsDataItem.ALWAYS_SAVE_DATA;
			} else if (save.contains(getString(R.string.data_item_never))) {
				mDataSave = SettingsDataItem.NEVER_SAVE_DAT;
			}
		}
	}
}
