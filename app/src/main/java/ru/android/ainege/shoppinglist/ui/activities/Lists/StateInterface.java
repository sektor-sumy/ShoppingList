package ru.android.ainege.shoppinglist.ui.activities.lists;

import android.os.Bundle;

import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public interface StateInterface extends OnBackPressedListener, ListsFragment.OnListSelectListener {
	void onCreate(Bundle savedInstanceState);
	void onSaveInstanceState(Bundle outState);
}
