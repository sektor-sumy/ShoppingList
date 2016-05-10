package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.AddItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.EditItemFragment;

public class ShoppingListActivity extends SingleFragmentActivity implements ShoppingListFragment.OnListChangeListener, ShoppingListFragment.OnUpdateListListener {
	public final static String EXTRA_ID_LIST = "idList";
	public final static String EXTRA_ITEM = "item";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ShoppingList item = (ShoppingList) getIntent().getSerializableExtra(EXTRA_ITEM);

		if (savedInstanceState == null && findViewById(R.id.right_fragment_container) != null) {
			Fragment newDetail;

			if (item != null) {
				newDetail = EditItemFragment.newInstance(item);
			} else {
				newDetail = AddItemFragment.newInstance(getIntent().getLongExtra(EXTRA_ID_LIST, -1));
			}

			injectFragment(newDetail, R.id.right_fragment_container);
		}
	}

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
		if (findViewById(R.id.right_fragment_container) == null) {
			Intent i = new Intent(this, ItemActivity.class);
			i.putExtra(ItemActivity.EXTRA_ID_LIST, id);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				startActivityForResult(i, ShoppingListFragment.ADD_ITEM, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
			} else {
				startActivityForResult(i, ShoppingListFragment.ADD_ITEM);
			}
		} else {
			Fragment newDetail = AddItemFragment.newInstance(id);
			injectFragment(newDetail, R.id.right_fragment_container);
		}
	}

	@Override
	public void onItemSelected(ShoppingList item) {
		if (findViewById(R.id.right_fragment_container) == null) {
			Intent i = new Intent(this, ItemActivity.class);
			i.putExtra(ItemActivity.EXTRA_ITEM, item);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				startActivityForResult(i, ShoppingListFragment.EDIT_ITEM, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
			} else {
				startActivityForResult(i, ShoppingListFragment.EDIT_ITEM);
			}
		} else {
			Fragment newDetail = EditItemFragment.newInstance(item);
			injectFragment(newDetail, R.id.right_fragment_container);
		}
	}

	@Override
	public void onUpgradeList() {

	}
}
