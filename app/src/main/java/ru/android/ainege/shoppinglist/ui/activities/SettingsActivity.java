package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.settings.MainPreferenceFragment;

public class SettingsActivity extends SingleFragmentActivity {
	private final static String FRAGMENT_TAG = "settings_activity_tag";
	private OnBackPressedListener mOnBackPressedListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mOnBackPressedListener = (MainPreferenceFragment) getFragmentManager().findFragmentByTag(getTag());
		}
	}

	@Override
	protected int getDefaultContainer() {
		return android.R.id.content;
	}

	@Override
	protected Fragment getFragment() {
		MainPreferenceFragment fragment = new MainPreferenceFragment();
		mOnBackPressedListener = fragment;
		return fragment;
	}

	@Override
	protected String getTag() {
		return FRAGMENT_TAG;
	}

	@Override
	protected void adsInitialize() {

	}

	@Override
	public void onBackPressed() {
		if (mOnBackPressedListener == null || (mOnBackPressedListener != null && mOnBackPressedListener.onBackPressed())) {
			super.onBackPressed();
		}
	}
}
