package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.item.AddItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.EditItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;

public class ItemActivity extends SingleFragmentActivity implements ItemFragment.OnClickListener {
	public final static String EXTRA_ID_LIST = "idList";
	public final static String EXTRA_ITEM = "item";
	private final static String FRAGMENT_TAG = "item_activity_tag";

	private OnBackPressedListener mOnBackPressedListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			ItemFragment itemFragment = (ItemFragment) getFragmentManager().findFragmentByTag(getTag());
			setListeners(itemFragment);
		}
	}

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
		if (mOnBackPressedListener == null || (mOnBackPressedListener != null && mOnBackPressedListener.onBackPressed())) {
			super.onBackPressed();
		}
	}

	@Override
	public void onItemSave(long id, boolean isAdded, boolean isClose) {
		super.onBackPressed();
	}

	@Override
	public void onImageClick() {

	}

	@Override
	public void onNotSave() {
		super.onBackPressed();
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

		setListeners(fragment);

		return fragment;
	}

	private void setListeners(ItemFragment fragment) {
		mOnBackPressedListener = fragment;
		fragment.setOnClickListener(this);
	}
}
