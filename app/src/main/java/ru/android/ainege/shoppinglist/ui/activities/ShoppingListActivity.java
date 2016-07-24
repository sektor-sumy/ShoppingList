package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;

public class ShoppingListActivity extends SingleFragmentActivity implements ShoppingListFragment.OnClickListener, ShoppingListFragment.OnListChangedListener {
	public final static String EXTRA_ID_LIST = "idList";
	private static final String SHOPPING_LIST_TAG = "shopping_list_tag_activity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			ShoppingListFragment shoppingListFragment = (ShoppingListFragment) getFragmentManager().findFragmentByTag(getTag());
			setListeners(shoppingListFragment);
		}
	}

	@Override
	protected Fragment getFragment() {
		long idList = getIntent().getLongExtra(EXTRA_ID_LIST, -1);
		ShoppingListFragment fragment = ShoppingListFragment.newInstance(idList);
		setListeners(fragment);

		return fragment;
	}

	@Override
	protected String getTag() {
		return SHOPPING_LIST_TAG;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;

		switch (requestCode) {
			case ShoppingListFragment.ADD_ITEM:
			case ShoppingListFragment.EDIT_ITEM:
				ShoppingListFragment fragment = (ShoppingListFragment) getFragmentManager().findFragmentByTag(SHOPPING_LIST_TAG);
				fragment.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	public void onItemAdd(long id) {
		Intent i = new Intent(this, ItemActivity.class);
		i.putExtra(ItemActivity.EXTRA_ID_LIST, id);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivityForResult(i, ShoppingListFragment.ADD_ITEM, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivityForResult(i, ShoppingListFragment.ADD_ITEM);
		}
	}

	@Override
	public void onItemSelect(ShoppingList item) {
		Intent i = new Intent(this, ItemActivity.class);
		i.putExtra(ItemActivity.EXTRA_ITEM, item);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivityForResult(i, ShoppingListFragment.EDIT_ITEM, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivityForResult(i, ShoppingListFragment.EDIT_ITEM);
		}
	}

	@Override
	public void onListUpdated() {

	}

	@Override
	public void onListDeleted(long idDeletedList) {
		super.onBackPressed();
	}

	private void setListeners(ShoppingListFragment fragment) {
		fragment.setListeners(this, this);
	}
}
