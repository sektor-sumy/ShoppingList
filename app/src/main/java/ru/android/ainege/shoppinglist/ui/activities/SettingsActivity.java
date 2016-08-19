package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;

import ru.android.ainege.shoppinglist.ui.fragments.MainPreferenceFragment;

public class SettingsActivity extends SingleFragmentActivity {
	private final static String FRAGMENT_TAG = "settings_activity_tag";

	@Override
	protected int getDefaultContainer() {
		return android.R.id.content;
	}

	@Override
	protected Fragment getFragment() {
		return new MainPreferenceFragment();
	}

	@Override
	protected String getTag() {
		return FRAGMENT_TAG;
	}

	@Override
	protected void adsInitialize() {

	}
}
