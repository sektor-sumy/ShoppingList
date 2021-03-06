package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ru.android.ainege.shoppinglist.R;

public class MainPreferenceFragment extends android.preference.PreferenceFragment {
	private static final String STATE_SCREEN = "state_screen";

	private PreferenceScreen mNestedScreen;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Fade());
		} else {
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}

		setHasOptionsMenu(true);

		nestedScreen(getString(R.string.settings_key_auto_complete_screen));
		nestedScreen(getString(R.string.settings_key_text_selection_screen));

		if (savedInstanceState != null) {
			if (savedInstanceState.get(STATE_SCREEN) != null) {
				mNestedScreen = (PreferenceScreen) findPreference(savedInstanceState.get(STATE_SCREEN).toString());
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		addToolbar(v);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mNestedScreen != null) {
			addToolbarToNestedScreen(mNestedScreen);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mNestedScreen != null) {
			outState.putString(STATE_SCREEN, mNestedScreen.getKey());
		}
	}

	private void nestedScreen(String key) {
		final PreferenceScreen categorySettings = (PreferenceScreen) findPreference(key);
		categorySettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				mNestedScreen = ((PreferenceScreen) preference);
				addToolbarToNestedScreen(mNestedScreen);
				return true;
			}
		});
	}

	private void addToolbar(View view) {
		LinearLayout root = (LinearLayout) view.findViewById(android.R.id.list).getParent();
		Toolbar toolbar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar, root, false);
		root.addView(toolbar, 0); // insert at top

		toolbar.setTitle(getString(R.string.settings));
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
	}

	private void addToolbarToNestedScreen(PreferenceScreen preference) {
		final Dialog dialog = preference.getDialog();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mNestedScreen = null;
			}
		});

		LinearLayout root;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent();
		} else {
			root = (LinearLayout) dialog.findViewById(android.R.id.content).getParent();
		}

		Toolbar toolbar = (Toolbar) LayoutInflater.from(getActivity()).inflate(R.layout.toolbar, root, false);
		root.addView(toolbar, 0); // insert at top

		toolbar.setTitle(preference.getTitle().toString());
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}
}
