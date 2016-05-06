package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;

import ru.android.ainege.shoppinglist.ui.fragments.settings.MainPreferenceFragment;

public class SettingsActivity extends SingleFragmentActivity {
	@Override
	protected int getDefaultContainer() {
		return android.R.id.content;
	}

	@Override
	protected Fragment getFragment() {
		return new MainPreferenceFragment();
	}
}
