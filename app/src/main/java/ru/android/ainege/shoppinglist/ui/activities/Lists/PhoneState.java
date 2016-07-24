package ru.android.ainege.shoppinglist.ui.activities.lists;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.ui.activities.ShoppingListActivity;
import ru.android.ainege.shoppinglist.ui.fragments.ListsFragment;

public class PhoneState implements StateInterface {
	private ListsActivity mListsActivity;

	public PhoneState(ListsActivity listsActivity) {
		mListsActivity = listsActivity;

		if (mListsActivity.getListsFragment() != null) {
			setListeners(mListsActivity.getListsFragment());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null && mListsActivity.shouldOpenLastList()) {
			openLastList();
		}

		if (savedInstanceState != null) {
			ListsFragment listFragment = (ListsFragment) mListsActivity.getFragmentManager().findFragmentByTag(ListsActivity.LISTS_TAG);
			setListeners(listFragment);
		}
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

	private boolean openLastList() {
		long id = mListsActivity.getSaveListId();
		boolean result = false;

		if (id != -1) {
			Intent i = new Intent(mListsActivity, ShoppingListActivity.class);
			i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, id);
			mListsActivity.startActivity(i);
			result = true;
		}

		return result;
	}

	private void setListeners(ListsFragment fragment) {
		fragment.setOnListSelectListener(this);
	}
}
