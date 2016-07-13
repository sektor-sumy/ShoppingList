package ru.android.ainege.shoppinglist.ui.activities.lists;

import android.os.Bundle;

import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public interface IState extends ListsFragment.OnListSelectListener {
	void onCreate(Bundle savedInstanceState);
	void onSaveInstanceState(Bundle outState);
	boolean onBackPressed();
}
