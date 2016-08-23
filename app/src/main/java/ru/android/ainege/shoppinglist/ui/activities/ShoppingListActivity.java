package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;

import static ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment.*;

public class ShoppingListActivity extends SingleFragmentActivity implements OnClickListener, OnListChangedListener {
	public final static String EXTRA_ID_LIST = "idList";
	private static final String SHOPPING_LIST_TAG = "shopping_list_tag_activity";

	private ShoppingListFragment mShoppingListFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mShoppingListFragment = (ShoppingListFragment) getFragmentManager().findFragmentByTag(getTag());
			setListeners(mShoppingListFragment);
		}

		getDrawerLayout().addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);

				mShoppingListFragment.closeActionMode();
			}
		});
	}

	@Override
	protected ShoppingListFragment createFragment() {
		long idList = getIntent().getLongExtra(EXTRA_ID_LIST, -1);
		mShoppingListFragment = newInstance(idList);
		setListeners(mShoppingListFragment);

		return mShoppingListFragment;
	}

	@Override
	protected ShoppingListFragment getFragment() {
		return mShoppingListFragment;
	}

	@Override
	protected String getTag() {
		return SHOPPING_LIST_TAG;
	}

	@Override
	protected void onMainSelected() {
		superOnBackPressed();
	}

	@Override
	protected void onLastListSelected() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) return;

		switch (requestCode) {
			case ADD_ITEM:
			case EDIT_ITEM:
				mShoppingListFragment.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	public void onItemAdd(long id) {
		Intent i = new Intent(this, ItemActivity.class);
		i.putExtra(ItemActivity.EXTRA_ID_LIST, id);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivityForResult(i, ADD_ITEM, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivityForResult(i, ADD_ITEM);
		}
	}

	@Override
	public void onItemSelect(ShoppingList item) {
		Intent i = new Intent(this, ItemActivity.class);
		i.putExtra(ItemActivity.EXTRA_ITEM, item);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivityForResult(i, EDIT_ITEM, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
		} else {
			startActivityForResult(i, EDIT_ITEM);
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
