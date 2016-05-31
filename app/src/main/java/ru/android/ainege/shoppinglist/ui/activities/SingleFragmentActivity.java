package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import ru.android.ainege.shoppinglist.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {
	protected abstract Fragment getFragment();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());

		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		if (savedInstanceState == null) {
			injectFragment(getDefaultContainer(), getFragment());
		}
	}

	protected int getLayout() {
		return R.layout.activity_fragment;
	}

	protected int getDefaultContainer() {
		return R.id.fragment_container;
	}

	protected void injectFragment(Integer container, Fragment fragment) {
		FragmentManager fm = getFragmentManager();
		fm.beginTransaction().replace(container, fragment).commit();
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
}
