package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;

import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.AddItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.EditItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ItemFragment;

public class ItemActivity extends SingleFragmentActivity {
	public final static String EXTRA_ID_LIST = "idList";
	public final static String EXTRA_ITEM = "item";

	OnBackPressedListener mOnBackPressedListener;

	@Override
	protected Fragment getFragment() {
		return createFragment();
	}

	private Fragment createFragment() {
		Intent intent = getIntent();

		long idList = intent.getLongExtra(EXTRA_ID_LIST, -1);
		ShoppingList itemInList = (ShoppingList) intent.getSerializableExtra(EXTRA_ITEM);

		ItemFragment fragment;
		if (idList != -1) { //add item to list
			fragment = AddItemFragment.newInstance(idList);
		} else { //edit item in list
			fragment = EditItemFragment.newInstance(itemInList);
		}

		mOnBackPressedListener = fragment;
		
		return fragment;
	}

	@Override
	public void onBackPressed() {
		if (mOnBackPressedListener != null) {
			mOnBackPressedListener.onBackPressed();
		} else {
			super.onBackPressed();
		}
	}
}
