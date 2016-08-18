package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.activities.lists.ListsActivity;

public abstract class SingleFragmentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	private static final int LISTS = 1;
	private static final int CATALOGS = 2;
	private static final int SETTINGS = 3;

	private DrawerLayout mDrawerLayout;

	protected abstract Fragment getFragment();
	protected abstract String getTag();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());
		adsInitialize();

		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		if (savedInstanceState == null) {
			injectFragment(getDefaultContainer(), getFragment(), getTag());
		}

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.nav_main:
				openCatalog(ListsActivity.class, null, LISTS);
				break;
			case R.id.nav_catalog_items:
				Toast.makeText(getApplicationContext(), getString(R.string.catalogs_items), Toast.LENGTH_SHORT).show();
				break;
			case R.id.nav_catalog_catalogs:
				openCatalog(SettingsCatalogActivity.class,
						new String[][] {{SettingsCatalogActivity.EXTRA_TYPE, getString(R.string.settings_key_category)}},
						CATALOGS);
				break;
			case R.id.nav_catalog_units:
				openCatalog(SettingsCatalogActivity.class,
						new String[][] {{SettingsCatalogActivity.EXTRA_TYPE, getString(R.string.settings_key_unit)}},
						CATALOGS);
				break;
			case R.id.nav_catalog_currencies:
				openCatalog(SettingsCatalogActivity.class,
						new String[][] {{SettingsCatalogActivity.EXTRA_TYPE, getString(R.string.settings_key_currency)}},
						CATALOGS);
				break;
			case R.id.nav_settings:
				openCatalog(SettingsActivity.class, null, SETTINGS);
				break;
			case R.id.nav_feedback:
				Toast.makeText(getApplicationContext(), getString(R.string.feedback), Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);

		return true;
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	protected int getLayout() {
		return R.layout.activity_fragment;
	}

	protected int getDefaultContainer() {
		return R.id.fragment_container;
	}

	protected void injectFragment(Integer container, Fragment fragment, String tag) {
		FragmentManager fm = getFragmentManager();
		fm.beginTransaction().replace(container, fragment, tag).commit();
	}

	protected void removeFragment(String tag) {
		FragmentManager fm = getFragmentManager();
		fm.beginTransaction()
				.remove(fm.findFragmentByTag(tag))
				.commit();
	}

	protected void adsInitialize() {
		MobileAds.initialize(this, "ca-app-pub-3804902313328755~1215266223");

		final AdView adView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.build();
		adView.loadAd(adRequest);

		adView.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				adView.setVisibility(View.VISIBLE);
			}

		});
	}

	private void openCatalog( Class<?> cls, String[][] extras, int requestCode) {
		Intent i = new Intent(this, cls);

		if (extras != null) {
			for (String[] extra : extras) {
				i.putExtra(extra[0], extra[1]);
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivityForResult(i, requestCode, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivityForResult(i, requestCode);
		}
	}
}
