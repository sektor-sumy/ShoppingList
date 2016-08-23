package ru.android.ainege.shoppinglist.ui.activities.Lists;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public interface StateInterface extends OnBackPressedListener, ListsFragment.OnListSelectListener {
	void onCreate(Bundle savedInstanceState);
	boolean onCreateViewListener(Fragment fragment, Toolbar toolbar);
	void onSaveInstanceState(Bundle outState);

	Fragment getFragment();
	void setFragment(ListsFragment listsFragment);

	void onMainSelected();
	void onLastListSelected();
}
