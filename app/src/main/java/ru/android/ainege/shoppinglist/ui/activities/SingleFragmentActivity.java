package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import ru.android.ainege.shoppinglist.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {
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
}
