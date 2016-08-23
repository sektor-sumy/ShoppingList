package ru.android.ainege.shoppinglist.ui.activities.Lists;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.ui.activities.SingleFragmentActivity;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;

public class ListsActivity extends SingleFragmentActivity {
	public static final String LISTS_SELECT = "lists_select";
	public static final String LAST_LIST_SELECT = "last_list_select";
	static final String LISTS_TAG = "lists_tag";

	private StateInterface mState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (getResources().getBoolean(R.bool.isTablet)) {
			mState = new TabletState(this);
		} else {
			mState = new PhoneState(this);
		}

		super.onCreate(savedInstanceState);
		mState.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateViewListener(Fragment fragment, Toolbar toolbar) {
		if (mState.onCreateViewListener(fragment, toolbar)) {
			super.onCreateViewListener(fragment, toolbar);
		}
	}

	@Override
	protected int getLayout() {
		return R.layout.activity_main;
	}

	@Override
	protected ListsFragment createFragment() {
		init();

		ListsFragment fragment = new ListsFragment();
		mState.setFragment(fragment);

		return fragment;
	}

	@Override
	protected Fragment getFragment() {
		return mState.getFragment();
	}

	@Override
	protected String getTag() {
		return LISTS_TAG;
	}

	@Override
	protected void onMainSelected() {
		mState.onMainSelected();
	}

	@Override
	protected void onLastListSelected() {
		mState.onLastListSelected();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mState.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if (mState.onBackPressed()) {
			super.onBackPressed();
		}
	}

	void injectFragmentToUI(Integer container, Fragment fragment, String tag) {
		injectFragment(container, fragment, tag);
	}

	public void removeFragmentFromUI(String tag) {
		removeFragment(tag);
	}

	boolean shouldOpenLastList() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isShould = false;

		if (prefs.getBoolean(getString(R.string.settings_key_open_last_list), false)) {
			isShould = getSaveListId() != -1;
		}

		return isShould;
	}

	private void init() {
		SharedPreferences sp = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		addAnalytics();

		if (sp.getBoolean("isFirst", false)) {
			return;
		}

		// app inited
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean("isFirst", true);
		e.apply();

		//set currency
		CurrenciesDS currenciesDS = new CurrenciesDS(this);
		CurrenciesDS.CurrencyCursor c = currenciesDS.getByName("Рубль");
		if (c.moveToFirst()) {
			long id = c.getEntity().getId();

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.putLong(getString(R.string.catalogs_key_currency), id);
			editor.apply();
		}

		//update ruble symbol on old device
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			currenciesDS.update("руб.", "\u20BD");
			currenciesDS.update("ман.", "\u20BC");
			currenciesDS.update("тг", "\u20B8");
		}
	}

	private void addAnalytics() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		FirebaseAnalytic.getInstance(this, null)
				.setUserProperty(FirebaseAnalytic.IS_OPEN_LAST_LIST, String.valueOf(settings.getBoolean(getString(R.string.settings_key_open_last_list), false)))
				.setUserProperty(FirebaseAnalytic.IS_BOUGHT_END, String.valueOf(settings.getBoolean(getString(R.string.settings_key_sort_is_bought), true)))
				.setUserProperty(FirebaseAnalytic.USE_CATEGORY, String.valueOf(settings.getBoolean(getString(R.string.settings_key_use_category), true)))
				.setUserProperty(FirebaseAnalytic.AUTO_COMPLETE_DATA, String.valueOf(settings.getBoolean(getString(R.string.settings_key_auto_complete_data), true)))
				.setUserProperty(FirebaseAnalytic.TRANSITION_TO_SETTINGS, String.valueOf(settings.getBoolean(getString(R.string.settings_key_transition), false)))
				.setUserProperty(FirebaseAnalytic.FAST_EDIT_CATALOGS, String.valueOf(settings.getBoolean(getString(R.string.settings_key_fast_edit), true)));
	}
}
