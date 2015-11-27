package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;

import ru.android.ainege.shoppinglist.ui.fragments.SettingsDataFragment;

public class SettingsDataActivity extends SingleFragmentActivity {

	@Override
	protected Fragment getFragment() {
		return new SettingsDataFragment();
	}
}
