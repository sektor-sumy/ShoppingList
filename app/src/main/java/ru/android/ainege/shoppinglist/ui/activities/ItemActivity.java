package ru.android.ainege.shoppinglist.ui.activities;

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

	private ItemFragment mItemFragment;
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
	protected ItemFragment createFragment() {
		Intent intent = getIntent();

		long idList = intent.getLongExtra(EXTRA_ID_LIST, -1);
		ShoppingList itemInList = (ShoppingList) intent.getSerializableExtra(EXTRA_ITEM);

		if (idList != -1) { //add item to list
			mItemFragment = AddItemFragment.newInstance(idList);
		} else { //edit item in list
			mItemFragment = EditItemFragment.newInstance(itemInList);
		}

		setListeners(mItemFragment);

		return mItemFragment;
	}

	@Override
	protected ItemFragment getFragment() {
		return mItemFragment;
	}

	@Override
	protected String getTag() {
		return FRAGMENT_TAG;
	}

	@Override
	protected void onLastListSelected() {
		//todo нужно сделать проверку на сохранение и закрыть
		superOnBackPressed();
	}

	@Override
	public void onBackPressed() {
		if (!closeDrawer() && mOnBackPressedListener == null || mOnBackPressedListener.onBackPressed()) {
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

	private void setListeners(ItemFragment fragment) {
		mOnBackPressedListener = fragment;
		fragment.setOnClickListener(this);
	}
}
