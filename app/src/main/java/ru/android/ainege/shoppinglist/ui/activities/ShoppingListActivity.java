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
	protected int getLayout() {
		return R.layout.activity_main;
	}

	@Override
	protected Fragment getFragment() {
		Intent intent = getIntent();
		long idList = intent.getLongExtra(EXTRA_ID_LIST, -1);

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
	public void onAddItem(long id) {
		if (findViewById(R.id.list_fragment_container) == null) {
			Intent i = new Intent(this, ItemActivity.class);
			i.putExtra(ItemActivity.EXTRA_ID_LIST, id);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				startActivityForResult(i, ShoppingListFragment.ADD_ITEM, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
			} else {
				startActivityForResult(i, ShoppingListFragment.ADD_ITEM);
			}
		}
	}

	@Override
	public void onItemSelected(ShoppingList item) {
		if (findViewById(R.id.list_fragment_container) == null) {
			Intent i = new Intent(this, ItemActivity.class);
			i.putExtra(ItemActivity.EXTRA_ITEM, item);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				startActivityForResult(i, ShoppingListFragment.EDIT_ITEM, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
			} else {
				startActivityForResult(i, ShoppingListFragment.EDIT_ITEM);
			}
		}
	}

	@Override
	public void onDeleteShoppingList() {

	}

	@Override
	public void onShoppingListUpdate() {

	}
}
