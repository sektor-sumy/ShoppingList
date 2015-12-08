package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public class ListsActivity extends SingleFragmentActivity {
	public static final String APP_PREFERENCES = "shopping_list_settings";

	@Override
	protected Fragment getFragment() {
		init();

		return new ListsFragment();
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

		//set currency - ruble
		CurrenciesDataSource.CurrencyCursor c = new CurrenciesDataSource(this).getByName("рубль");
		if (c.moveToFirst()) {
			long id = c.getEntity().getId();

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(getString(R.string.settings_key_currency), id);
			editor.apply();
		}

		//update ruble symbol on old device
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			new CurrenciesDataSource(this).update("руб.", "\u20BD");
		}
	}
}
