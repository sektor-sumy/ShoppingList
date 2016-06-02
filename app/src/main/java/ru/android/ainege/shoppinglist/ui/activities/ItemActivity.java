package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;

import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.item.AddItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.EditItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;

public class ItemActivity extends SingleFragmentActivity {
	public final static String EXTRA_ID_LIST = "idList";
	public final static String EXTRA_ITEM = "item";
	private final static String FRAGMENT_TAG = "item_activity_tag";

	private OnBackPressedInterface mOnBackPressedListener;

	@Override
	protected Fragment getFragment() {
		return createFragment();
	}

	@Override
	protected String getTag() {
		return FRAGMENT_TAG;
	}

	@Override
	public void onBackPressed() {
		if (mOnBackPressedListener != null) {
			mOnBackPressedListener.onBackPressed();
		} else {
			super.onBackPressed();
		}
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

	public interface OnBackPressedInterface {
		void onBackPressed();
	}
}
