package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;

import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public class ListsActivity extends SingleFragmentActivity {

	@Override
	protected Fragment getFragment() {
		return new ListsFragment();
	}
}
