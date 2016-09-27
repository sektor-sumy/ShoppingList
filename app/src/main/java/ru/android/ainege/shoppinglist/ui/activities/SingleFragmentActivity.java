package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.activities.Lists.ListsActivity;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.ItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.list.ListsFragment;
import ru.android.ainege.shoppinglist.ui.fragments.OnCreateViewListener;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.CategoryFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.CurrencyFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.UnitFragment;

import ru.android.ainege.shoppinglist.util.MobileAd;

public abstract class SingleFragmentActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, OnCreateViewListener {
	protected static final String APP_PREFERENCES = "shopping_list_settings";
	private static final String APP_PREFERENCES_ID = "idList";

	private static final int LISTS = 101;
	public static final int CATALOGS = 102;
	public static final int SETTINGS = 103;

	private AdView mAdView;
	private DrawerLayout mDrawerLayout;

	protected abstract Fragment createFragment();
	protected abstract Fragment getFragment();
	protected abstract String getTag();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());
		adsInitialize();

		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		if (savedInstanceState == null) {
			injectFragment(getDefaultContainer(), createFragment(), getTag());
		}

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mAdView.resume();
		setCheckedItem();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mAdView.pause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mAdView.destroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case CATALOGS:
			case SETTINGS:
				getFragment().onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (!closeDrawer()) {
			super.onBackPressed();
		}
	}

	@Override
	public void onCreateViewListener(Fragment fragment, Toolbar toolbar) {
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawerLayout.addDrawerListener(toggle);
		toggle.syncState();
	}

	@Override
	public void onDeleteSavedList() {
		((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(R.id.nav_last_list).setEnabled(false);
	}

	public long getSavedListId() {
		SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
		return mSettings.getLong(APP_PREFERENCES_ID, -1);
	}

	protected void superOnBackPressed() {
		super.onBackPressed();
	}

	protected int getLayout() {
		return R.layout.activity_fragment;
	}

	protected int getDefaultContainer() {
		return R.id.fragment_container;
	}

	//<editor-fold desc="navigation drawer">
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.nav_main:
				onMainSelected();
				break;
			case R.id.nav_last_list:
				onLastListSelected();
				break;
			case R.id.nav_catalog_items:
				onCatalogSelected(R.string.catalogs_key_item);
				break;
			case R.id.nav_catalog_catalogs:
				onCatalogSelected(R.string.catalogs_key_category);
				break;
			case R.id.nav_catalog_units:
				onCatalogSelected(R.string.catalogs_key_unit);
				break;
			case R.id.nav_catalog_currencies:
				onCatalogSelected(R.string.catalogs_key_currency);
				break;
			case R.id.nav_settings:
				onSettingsSelect();
				break;
			case R.id.nav_feedback:
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.parse("mailto:" + getString(R.string.feedback_email)));
				intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject));

				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivity(intent);
				} else {
					Toast.makeText(this, getString(R.string.feedback_error), Toast.LENGTH_LONG).show();
				}

				break;
			default:
				break;
		}

		mDrawerLayout.closeDrawer(GravityCompat.START);

		return true;
	}

	public DrawerLayout getDrawerLayout() {
		return mDrawerLayout;
	}

	public boolean closeDrawer() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
			return true;
		}

		return false;
	}

	protected void onMainSelected() {
		Intent i = new Intent(this, ListsActivity.class);
		i.putExtra(ListsActivity.LISTS_SELECT, true);

		openActivity(i, new int[] {Intent.FLAG_ACTIVITY_CLEAR_TOP}, LISTS);
	}

	protected void onLastListSelected() {
		if (getResources().getBoolean(R.bool.isTablet)) {
			Intent i = new Intent(this, ListsActivity.class);
			i.putExtra(ListsActivity.LAST_LIST_SELECT, true);
			openActivity(i, new int[] {Intent.FLAG_ACTIVITY_CLEAR_TOP}, LISTS);
		} else {
			openLastList();
		}
	}

	protected void onCatalogSelected(int key) {
		Intent i = new Intent(this, CatalogsActivity.class);
		i.putExtra(CatalogsActivity.EXTRA_TYPE, getString(key));

		openActivity(i, null, CATALOGS);
	}

	protected void onSettingsSelect() {
		openActivity(new Intent(this, SettingsActivity.class), null, SETTINGS);
	}

	private void openActivity(Intent i, int[] flags, int requestCode) {
		if (flags != null) {
			for (int flag : flags) {
				i.addFlags(flag);
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivityForResult(i, requestCode, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivityForResult(i, requestCode);
		}
	}

	private void setCheckedItem() {
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

		if (getFragment() instanceof ListsFragment) {
			navigationView.setCheckedItem(R.id.nav_main);
		} else if (getFragment() instanceof ShoppingListFragment) {
			navigationView.setCheckedItem(R.id.nav_last_list);
		} else if (getFragment() instanceof ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment) {
			navigationView.setCheckedItem(R.id.nav_last_list);
		} else if (getFragment() instanceof ItemFragment) {
			navigationView.setCheckedItem(R.id.nav_catalog_items);
		} else if (getFragment() instanceof CategoryFragment) {
			navigationView.setCheckedItem(R.id.nav_catalog_catalogs);
		} else if (getFragment() instanceof UnitFragment) {
			navigationView.setCheckedItem(R.id.nav_catalog_units);
		} else if (getFragment() instanceof CurrencyFragment) {
			navigationView.setCheckedItem(R.id.nav_catalog_currencies);
		}

		if (getSavedListId() == -1) {
			navigationView.getMenu().findItem(R.id.nav_last_list).setEnabled(false);
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isUseCategory = sharedPreferences.getBoolean(getString(R.string.settings_key_use_category), true);
		navigationView.getMenu().findItem(R.id.nav_catalog_catalogs).setEnabled(isUseCategory);
	}
	//</editor-fold>

	public boolean openLastList() {
		long id = getSavedListId();
		boolean result = false;

		if (id != -1) {
			Intent i = new Intent(this, ShoppingListActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);
			startActivity(i);
			result = true;
		}

		return result;
	}

	protected void injectFragment(Integer container, Fragment fragment, String tag) {
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction().replace(container, fragment, tag).commit();
	}

	protected void removeFragment(String tag) {
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentByTag(tag);

		if (fragment != null) {
			fm.beginTransaction()
					.remove(fragment)
					.commit();
		}
	}

	protected void adsInitialize() {
		mAdView = (AdView) findViewById(R.id.adView);
		MobileAd ad = new MobileAd();
		ad.adsInitialize(this, mAdView);
	}
}
