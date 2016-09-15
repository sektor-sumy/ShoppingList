package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;


import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.CatalogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.CategoryFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.CurrencyFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.ItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.UnitFragment;

public class CatalogsActivity extends SingleFragmentActivity {
	public final static String EXTRA_TYPE = "type";
	public final static String EXTRA_DATA = "data";
	public static final String LAST_EDIT = "lastEdit";
	private final static String FRAGMENT_TAG = "settings_catalog_tag";

	private CatalogFragment mCatalogFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mCatalogFragment = (CatalogFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		}
	}

	@Override
	protected CatalogFragment createFragment() {
		Intent intent = getIntent();
		String type = intent.getStringExtra(EXTRA_TYPE);

		if (type.equals(getString(R.string.catalogs_key_item))) {
			mCatalogFragment = new ItemFragment();
		} else if (type.equals(getString(R.string.catalogs_key_currency))) {
			mCatalogFragment = new CurrencyFragment();
		} else if (type.equals(getString(R.string.catalogs_key_unit))) {
			mCatalogFragment = new UnitFragment();
		} else if (type.equals(getString(R.string.catalogs_key_category))) {
			mCatalogFragment = new CategoryFragment();
		}

		mCatalogFragment.setLastEditIds((HashMap<Integer, Long>) intent.getSerializableExtra(EXTRA_DATA));

		return mCatalogFragment;
	}

	@Override
	protected CatalogFragment getFragment() {
		return mCatalogFragment;
	}

	@Override
	protected String getTag() {
		return FRAGMENT_TAG;
	}

	@Override
	protected void onCatalogSelected(int key) {
		if (key == mCatalogFragment.getKey()) {
			return;
		}

		Intent i = new Intent(this, CatalogsActivity.class);
		i.putExtra(CatalogsActivity.EXTRA_TYPE, getString(key));
		i.putExtra(CatalogsActivity.EXTRA_DATA, mCatalogFragment.getLastEditIds());
		i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivity(i);
		}

		finish();
	}

	@Override
	protected void onSettingsSelect() {
		Intent i = new Intent(this, SettingsActivity.class);
		i.putExtra(CatalogsActivity.EXTRA_DATA, mCatalogFragment.getLastEditIds());
		i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivity(i);
		}
	}

	@Override
	protected void adsInitialize() {

	}

	@Override
	public void onBackPressed() {
		if (!closeDrawer()) {
			setResult(Activity.RESULT_OK, new Intent().putExtra(LAST_EDIT, mCatalogFragment.getLastEditIds()));
			super.onBackPressed();
		}
	}
}
