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
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public class ListsActivity extends SingleFragmentActivity {
	public static final String APP_PREFERENCES = "shopping_list_settings";
	public static final String APP_PREFERENCES_ID = "idList";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		openLastList();
	}

	@Override
	protected Fragment getFragment() {
		init();

		return new ListsFragment();
	}

	private void openLastList() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isOpenLastList = prefs.getBoolean(getString(R.string.settings_key_open_last_list), false);

		if (isOpenLastList) {
			SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
			if (mSettings.contains(APP_PREFERENCES_ID)) {
				long id = mSettings.getLong(APP_PREFERENCES_ID, -1);
				if (id != -1) {
					Intent i = new Intent(this, ShoppingListActivity.class);
					i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
					} else {
						startActivity(i);
					}
				}
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
		CurrenciesDataSource currenciesDS =  new CurrenciesDataSource(this);
		CurrenciesDataSource.CurrencyCursor c = currenciesDS.getByName("Рубль");
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
}
