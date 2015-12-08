package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;

import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;

public class ShoppingListActivity extends SingleFragmentActivity {
	public final static String EXTRA_ID_LIST = "idList";

	@Override
	protected Fragment getFragment() {
		Intent intent = getIntent();
		long idList = intent.getLongExtra(EXTRA_ID_LIST, -1);

		return ShoppingListFragment.newInstance(idList);
	}
}
