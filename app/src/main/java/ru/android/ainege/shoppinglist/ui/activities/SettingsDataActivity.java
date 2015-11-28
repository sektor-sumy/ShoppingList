package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;

import ru.android.ainege.shoppinglist.ui.fragments.settings.CurrencyFragment;

public class SettingsDataActivity extends SingleFragmentActivity {

	@Override
	protected Fragment getFragment() {
		return new CurrencyFragment();
	}
}
