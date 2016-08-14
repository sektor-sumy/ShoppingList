package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.settings.CategoryFragment;
import ru.android.ainege.shoppinglist.ui.fragments.settings.CurrencyFragment;
import ru.android.ainege.shoppinglist.ui.fragments.settings.UnitFragment;

public class SettingsCatalogActivity extends SingleFragmentActivity {
	public final static String EXTRA_TYPE = "type";
	private final static String FRAGMENT_TAG = "settings_catalog_tag";

	private OnBackPressedListener mOnBackPressedListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mOnBackPressedListener = (OnBackPressedListener) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		}
	}

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

		mOnBackPressedListener = (OnBackPressedListener) fragment;

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
