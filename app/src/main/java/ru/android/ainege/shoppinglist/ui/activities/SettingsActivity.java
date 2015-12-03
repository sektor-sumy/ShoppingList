package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.transition.Slide;
import android.view.Gravity;

import ru.android.ainege.shoppinglist.R;

public class SettingsActivity extends SingleFragmentActivity {

	@Override
	protected int getDefaultContainer() {
		return android.R.id.content;
	}

	@Override
	protected Fragment getFragment() {
		return new MyPreferenceFragment();
	}

	public static class MyPreferenceFragment extends PreferenceFragment {

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.settings);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
			} else {
				getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
			}

			Preference currencySettings = findPreference(getString(R.string.settings_key_currency));
			currencySettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), SettingsDictionaryActivity.class);
					i.putExtra(SettingsDictionaryActivity.EXTRA_TYPE, getString(R.string.settings_key_currency));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
					} else {
						startActivity(i);
					}
					return true;
				}
			});

			Preference unitSettings = findPreference(getString(R.string.setting_key_unit));
			unitSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), SettingsDictionaryActivity.class);
					i.putExtra(SettingsDictionaryActivity.EXTRA_TYPE, getString(R.string.setting_key_unit));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
					} else {
						startActivity(i);
					}
					return true;
				}
			});

		}

		@Override
		public void onPause() {
			super.onPause();
			getActivity().overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
		}
	}
}
