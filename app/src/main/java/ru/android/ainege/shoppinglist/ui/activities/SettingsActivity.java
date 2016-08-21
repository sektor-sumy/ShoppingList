package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;

import ru.android.ainege.shoppinglist.ui.fragments.MainPreferenceFragment;

public class SettingsActivity extends SingleFragmentActivity {
	private final static String FRAGMENT_TAG = "settings_activity_tag";

	private MainPreferenceFragment mPreferenceFragment;

	@Override
	protected int getDefaultContainer() {
		return android.R.id.content;
	}

	@Override
	protected MainPreferenceFragment createFragment() {
		mPreferenceFragment = new MainPreferenceFragment();
		return mPreferenceFragment;
	}

	@Override
	protected MainPreferenceFragment getFragment() {
		return mPreferenceFragment;
	}

	@Override
	protected String getTag() {
		return FRAGMENT_TAG;
	}

	@Override
	protected void adsInitialize() {

	}

	@Override
	protected void onCatalogSelected(int key) {
		Intent i = new Intent(this, CatalogsActivity.class);
		i.putExtra(CatalogsActivity.EXTRA_TYPE, getString(key));
		i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivity(i);
		}
	}

	@Override
	protected void onSettingsSelect() {

	}

	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_OK,
				new Intent().putExtra(CatalogsActivity.LAST_EDIT, getIntent().getSerializableExtra(CatalogsActivity.EXTRA_DATA)));
		super.onBackPressed();
	}
}
