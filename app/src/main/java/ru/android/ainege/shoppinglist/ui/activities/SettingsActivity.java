package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.os.Bundle;
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
		}
	}
}
