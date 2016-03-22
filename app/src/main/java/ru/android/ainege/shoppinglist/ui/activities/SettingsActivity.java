package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import ru.android.ainege.shoppinglist.R;

public class SettingsActivity extends SingleFragmentActivity {
	private static final String IS_OPENED_DICTIONARY = "isOpenedDictionary";

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
				getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			}

			setHasOptionsMenu(true);
			ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

			if (actionBar != null) {
				actionBar.setHomeButtonEnabled(true);
				actionBar.setDisplayHomeAsUpEnabled(true);
			}

			startSettingByKey(getString(R.string.settings_key_currency));
			startSettingByKey(getString(R.string.settings_key_unit));
			startSettingByKey(getString(R.string.settings_key_category));

		}

		@Override
		public void onPause() {
			super.onPause();
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
				case android.R.id.home:
					getActivity().finish();
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		}

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
			super.onPreferenceTreeClick(preferenceScreen, preference);

			// If the user has clicked on a preference screen, set up the screen
			if (preference instanceof PreferenceScreen) {
				setUpNestedScreen((PreferenceScreen) preference);
			}

			return false;
		}

		public void setUpNestedScreen(PreferenceScreen preferenceScreen) {
			final Dialog dialog = preferenceScreen.getDialog();

			Toolbar toolbar;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				LinearLayout root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent();
				toolbar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar, root, false);
				root.addView(toolbar, 0); // insert at top
			} else {
				ViewGroup root = (ViewGroup) dialog.findViewById(android.R.id.content);
				ListView content = (ListView) root.getChildAt(0);

				root.removeAllViews();

				toolbar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar, root, false);

				int height;
				TypedValue tv = new TypedValue();
				if (getActivity().getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
					height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
				}else{
					height = toolbar.getHeight();
				}

				content.setPadding(0, height, 0, 0);

				root.addView(content);
				root.addView(toolbar);
			}


			toolbar.setTitle(preferenceScreen.getTitle());
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		}

		private void startSettingByKey(final String key) {
			Preference categorySettings = findPreference(key);
			categorySettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent i = new Intent(getActivity(), SettingsDictionaryActivity.class);
					i.putExtra(SettingsDictionaryActivity.EXTRA_TYPE, key);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
					} else {
						startActivity(i);
					}

					sendResult(true);
					return true;
				}
			});
		}

		void sendResult(boolean isOpenedDictionary) {
			getActivity().setResult(android.app.Activity.RESULT_OK, new Intent().putExtra(IS_OPENED_DICTIONARY, isOpenedDictionary));
		}
	}
}
