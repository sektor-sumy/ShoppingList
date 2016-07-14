package ru.android.ainege.shoppinglist.ui.activities.lists;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.ui.activities.ShoppingListActivity;

public class PhoneState implements StateInterface {
	private ListsActivity mListsActivity;

	public PhoneState(ListsActivity listsActivity) {
		mListsActivity = listsActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null && mListsActivity.shouldOpenLastList()) {
			openLastList();
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
}
