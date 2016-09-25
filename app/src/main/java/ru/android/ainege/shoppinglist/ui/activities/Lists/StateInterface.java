package ru.android.ainege.shoppinglist.ui.activities.Lists;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.list.ListsFragment;

interface StateInterface extends OnBackPressedListener, ListsFragment.OnListSelectListener {
	void onCreate(Bundle savedInstanceState);
	boolean onCreateViewListener(Fragment fragment, Toolbar toolbar);
	void onSaveInstanceState(Bundle outState);

	Fragment getFragment();
	void setFragment(ListsFragment listsFragment);

	void onMainSelected();
	void onLastListSelected();
}
