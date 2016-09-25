package ru.android.ainege.shoppinglist.ui.activities.Lists;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import ru.android.ainege.shoppinglist.ui.activities.ShoppingListActivity;
import ru.android.ainege.shoppinglist.ui.fragments.list.ListsFragment;

public class PhoneState implements StateInterface {
	private ListsActivity mListsActivity;
	private ListsFragment mListsFragment;

	public PhoneState(ListsActivity listsActivity) {
		mListsActivity = listsActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		boolean lists_select = mListsActivity.getIntent().getBooleanExtra(ListsActivity.LISTS_SELECT, false);

		if (savedInstanceState == null && !lists_select && mListsActivity.shouldOpenLastList()) {
			mListsActivity.openLastList();
		}

		if (savedInstanceState != null) {
			mListsFragment = (ListsFragment) mListsActivity.getSupportFragmentManager().findFragmentByTag(ListsActivity.LISTS_TAG);
			setListeners(mListsFragment);
		}
	}

	@Override
	public boolean onCreateViewListener(Fragment fragment, Toolbar toolbar) {
		return true;
	}

	@Override
	public ListsFragment getFragment() {
		return mListsFragment;
	}

	@Override
	public void setFragment(ListsFragment listsFragment) {
		mListsFragment = listsFragment;
		setListeners(mListsFragment);
	}

	@Override
	public void onMainSelected() {

	}

	@Override
	public void onLastListSelected() {
		mListsActivity.openLastList();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

	}

	@Override
	public boolean onBackPressed() {
		return true;
	}

	@Override
	public void onListClick(long id) {
		Intent i = new Intent(mListsActivity, ShoppingListActivity.class);
		i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mListsActivity.startActivity(i, ActivityOptions.makeSceneTransitionAnimation(mListsActivity).toBundle());
		} else {
			mListsActivity.startActivity(i);
		}
	}

	private void setListeners(ListsFragment fragment) {
		fragment.setOnListSelectListener(this);
	}
}
