package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import java.text.DecimalFormat;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.activities.CatalogsActivity;
import ru.android.ainege.shoppinglist.ui.activities.SingleFragmentActivity;
import ru.android.ainege.shoppinglist.util.Image;

import static java.lang.String.format;

public class EditItemFragment extends ItemFragment {
	private static final String ITEM_IN_LIST = "itemInList";

	public static EditItemFragment newInstance(ShoppingList itemInList) {
		Bundle args = new Bundle();
		args.putSerializable(ITEM_IN_LIST, itemInList);

		EditItemFragment fragment = new EditItemFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mOriginalItem = new ShoppingList((ShoppingList) getArguments().getSerializable(ITEM_IN_LIST));
		mItemInList = new ShoppingList((ShoppingList) getArguments().getSerializable(ITEM_IN_LIST));
	}

	@Override
	protected void setupView(View v, Bundle savedInstanceState) {
		super.setupView(v, savedInstanceState);

		if (savedInstanceState == null) {
			setDataToView(savedInstanceState);
		} else {
			loadImage(savedInstanceState.getString(STATE_IMAGE_PATH));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case SingleFragmentActivity.CATALOGS:
			case SingleFragmentActivity.SETTINGS:
				HashMap<Integer, Long> modifyCatalog = (HashMap<Integer, Long>) data.getSerializableExtra(CatalogsActivity.LAST_EDIT);

				if (modifyCatalog != null) {
					ShoppingListCursor cursor = mItemsInListDS.get(mOriginalItem.getIdList(), mOriginalItem.getIdItem());

					if (cursor.moveToFirst()) {
						mOriginalItem = cursor.getEntity();
					}

					cursor.close();
				}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onBackPressed() {
		refreshItem();
		boolean result = mItemInList.equals(mOriginalItem);

		if (result && mIsUpdateSL) {
			sendResult(-1);
		}

		return result || super.onBackPressed();
	}

	@Override
	protected SimpleCursorAdapter getCompleteTextAdapter() {
		return super.getCompleteTextAdapter(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence charSequence) {
				Cursor managedCursor = null;

				if (charSequence != null && !charSequence.toString().trim().equals(mItemInList.getItem().getName())) {
					managedCursor = mItemDS.getNames(charSequence.toString().trim());

					if (managedCursor.moveToFirst()) {
						mIsProposedItem = true;
					}
				}

				return managedCursor;
			}
		});
	}

	@Override
	protected void fillItemFromAutoComplete(Item item) {
		if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_amount_edit), false) && item.getAmount() != 0) {
			mAmountEditText.setText(new DecimalFormat("#.######").format(item.getAmount()));
		}

		if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_unit_edit), false)) {
			mUnitSpinner.setSelected(item.getIdUnit());
		}

		if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_price_edit), false) && item.getPrice() != 0) {
			mPriceEditText.setText(String.format("%.2f", item.getPrice()));
		}

		if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_comment_edit), false)) {
			mCommentEditText.setText(item.getComment());
		}
	}

	@Override
	protected long getIdList() {
		return mItemInList.getIdList();
	}

	@Override
	protected boolean saveData() {
		boolean isSave = false;

		if (isValidData()) {
			refreshItem();
			boolean isItemChanged = mOriginalItem.getIdItem() != mItemInList.getIdItem();
			mItemInList.updateItem(getActivity());

			if (isItemChanged) {
				if (mInfoTextView.getVisibility() == View.VISIBLE) {        //if item in list - update it and delete original
					mItemsInListDS.update(mItemInList);
					mItemsInListDS.delete(mOriginalItem.getIdItemData());
				} else {                                            //if item was changed - update with new one
					mItemsInListDS.update(mItemInList, mOriginalItem.getIdItem());
				}
			} else {                                                //if item was not changed - update it
				mItemsInListDS.update(mItemInList);
			}

			deleteOriginalImage();
			sendResult(mItemInList.getIdItem());

			mOriginalItem = new ShoppingList(mItemInList);
			isSave = true;
		}
		return isSave;
	}

	@Override
	protected ShoppingList refreshItem() {
		//if item was changed for new one (not from db) - update item
		if (!getName().trim().equals(mOriginalItem.getItem().getName()) && mOriginalItem.getIdItem() == mItemInList.getIdItem()) {
			mItemInList.setItem(new Item(getName(), mItemInList.getItem().getImagePath(), mItemInList.getItem().getImagePath()));
		}

		return super.refreshItem();
	}

	@Override
	public void resetImage() {
		if (!mItemInList.getItem().getImagePath().equals(mItemInList.getItem().getDefaultImagePath())) {
			loadImage(mItemInList.getItem().getDefaultImagePath());
		}
	}

	@Override
	public boolean isDeleteImage(String newPath) {
		String imagePath = mItemInList.getItem().getImagePath();
		String defaultImagePath  = mItemInList.getItem().getDefaultImagePath();

		return !imagePath.contains(Image.ASSETS_IMAGE_PATH) &&
				!imagePath.equals(newPath) &&
				!imagePath.equals(mOriginalItem.getItem().getImagePath()) &&
				!imagePath.equals(defaultImagePath);
	}

	private void setDataToView(Bundle savedInstanceState) {
		loadImage(mItemInList.getItem().getImagePath());

		mNameEditText.setText(mItemInList.getItem().getName());

		if (mItemInList.getAmount() != 0) {
			mAmountEditText.setText(new DecimalFormat("#.######").format(mItemInList.getAmount()));
		}

		mUnitSpinner.setSelected(mItemInList.getUnit().getId());

		if (mItemInList.getPrice() != 0) {
			mPriceEditText.setText(format("%.2f", mItemInList.getPrice()));
		}

		if (savedInstanceState != null && !mIsUseCategory) {
			mCategorySpinner.setSelected(savedInstanceState.getLong(STATE_CATEGORY_ID));
		} else {
			mCategorySpinner.setSelected(mItemInList.getCategory().getId());
		}

		mCommentEditText.setText(mItemInList.getComment());

		setIsBoughtButton(mItemInList.isBought());
	}

	private boolean isValidData() {
		boolean isValid = true;

		if (mNameEditText.length() < 3) {
			mNameInputLayout.setError(getString(R.string.error_length_name));
			isValid = false;
		}

		if (mNameInputLayout.isErrorEnabled() || mAmountInputLayout.isErrorEnabled() ||	mPriceInputLayout.isErrorEnabled()) {
			isValid = false;
		}

		return isValid;
	}

	private void deleteOriginalImage() {
		String originImagePath = mOriginalItem.getItem().getImagePath();

		if (!originImagePath.contains(Image.ASSETS_IMAGE_PATH) &&
				!originImagePath.equals(mItemInList.getItem().getImagePath()) &&
				!originImagePath.equals(mItemInList.getItem().getDefaultImagePath())) {
			Image.deleteFile(originImagePath);
		}
	}
}
