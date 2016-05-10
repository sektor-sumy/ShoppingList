package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;

public class ListsActivity extends SingleFragmentActivity implements ListsFragment.OnListsChangeListener, ShoppingListFragment.OnUpdateListListener, ShoppingListFragment.OnListChangeListener {
	public static final String APP_PREFERENCES = "shopping_list_settings";
	public static final String APP_PREFERENCES_ID = "idList";
	private static final int ITEM = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null && shouldOpenLastList()) {
			openLastList();
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;

		switch (requestCode) {
			case ITEM:
				ShoppingListFragment itemFragment = (ShoppingListFragment) getFragmentManager().findFragmentById(R.id.right_fragment_container);
				itemFragment.updateData();
				break;
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
			if (findViewById(R.id.right_fragment_container) == null) {
				Intent i = new Intent(this, ShoppingListActivity.class);
				i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);
				startActivity(i);
			} else {
				Fragment newDetail = ShoppingListFragment.newInstance(id);
				injectFragment(newDetail, R.id.right_fragment_container);
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
	public void onListSelected(long id) {
		if (findViewById(R.id.right_fragment_container) == null) {
			Intent i = new Intent(this, ShoppingListActivity.class);
			i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
			} else {
				startActivity(i);
			}
		} else {
			Fragment newDetail = ShoppingListFragment.newInstance(id);
			injectFragment(newDetail, R.id.right_fragment_container);
		}
	}

	@Override
	public void onUpgradeList() {
		ListsFragment listFragment = (ListsFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
		listFragment.updateData();
	}

	@Override
	public void onAddItem(long id) {
		if (findViewById(R.id.right_fragment_container) != null) {
			Intent i = new Intent(this, ShoppingListActivity.class);
			i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);
			startActivityForResult(i, ITEM);
		}
	}

	@Override
	public void onItemSelected(ShoppingList item) {
		if (findViewById(R.id.right_fragment_container) != null) {
			Intent i = new Intent(this, ShoppingListActivity.class);
			i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, item.getIdList());
			i.putExtra(ShoppingListActivity.EXTRA_ITEM, item);
			startActivityForResult(i, ITEM);
		}
	}
}
