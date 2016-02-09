package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.fragments.settings.CategoryFragment;
import ru.android.ainege.shoppinglist.ui.fragments.settings.CurrencyFragment;
import ru.android.ainege.shoppinglist.ui.fragments.settings.UnitFragment;

public class SettingsDictionaryActivity extends SingleFragmentActivity {
	public final static String EXTRA_TYPE = "type";

	@Override
	protected Fragment getFragment() {
		Intent intent = getIntent();
		String type = intent.getStringExtra(EXTRA_TYPE);
		Fragment fragment = null;

		if (type.equals(getString(R.string.settings_key_currency))) {
			fragment = new CurrencyFragment();
		} else if (type.equals(getString(R.string.settings_key_unit))) {
			fragment = new UnitFragment();
		} else if (type.equals(getString(R.string.settings_key_category))) {
			fragment = new CategoryFragment();
		}

		return fragment;
	}
}
