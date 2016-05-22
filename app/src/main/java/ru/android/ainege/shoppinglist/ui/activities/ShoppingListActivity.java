package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;

public class ShoppingListActivity extends SingleFragmentActivity implements ShoppingListFragment.OnListChangeListener, ShoppingListFragment.OnUpdateListListener {
	public final static String EXTRA_ID_LIST = "idList";

	@Override
	protected Fragment getFragment() {
		long idList = getIntent().getLongExtra(EXTRA_ID_LIST, -1);

		return ShoppingListFragment.newInstance(idList);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;

		switch (requestCode) {
			case ShoppingListFragment.ADD_ITEM:
			case ShoppingListFragment.EDIT_ITEM:
				ShoppingListFragment fragment = (ShoppingListFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
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
	public void onItemUpdate(ShoppingList item) {

	}

	@Override
	public void onItemDelete() {

	}

	@Override
	public long getLastSelectedItemId() {
		return 0;
	}

	@Override
	public void onListDelete() {
		onBackPressed();
	}

	@Override
	public void onListUpdate() {

	}
}
