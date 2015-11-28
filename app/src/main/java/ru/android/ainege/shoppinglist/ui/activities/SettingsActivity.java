package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

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

			Preference currencySettings = findPreference(getString(R.string.settings_key_currency));
			currencySettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), SettingsDataActivity.class);
					i.putExtra(SettingsDataActivity.EXTRA_TYPE, getString(R.string.settings_key_currency));
					startActivity(i);
					return true;
				}
			});

			Preference unitSettings = findPreference(getString(R.string.setting_key_unit));
			unitSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), SettingsDataActivity.class);
					i.putExtra(SettingsDataActivity.EXTRA_TYPE, getString(R.string.setting_key_unit));
					startActivity(i);
					return true;
				}
			});

		}
	}
}
