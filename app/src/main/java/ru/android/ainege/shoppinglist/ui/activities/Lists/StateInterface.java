package ru.android.ainege.shoppinglist.ui.activities.Lists;

import android.app.Fragment;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public interface StateInterface extends OnBackPressedListener, ListsFragment.OnListSelectListener {
	void onCreate(Bundle savedInstanceState);
	void onSaveInstanceState(Bundle outState);

	Fragment getFragment();
	void setFragment(ListsFragment listsFragment);

	void onMainSelected();
	void onLastListSelected();
}
