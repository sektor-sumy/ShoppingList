package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.util.Image;

public class AddItemFragment extends ItemFragment {
	private static final String ID_LIST = "idList";

	public static AddItemFragment newInstance(long id) {
		Bundle args = new Bundle();
		args.putLong(ID_LIST, id);

		AddItemFragment fragment = new AddItemFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mIsAddMode = true;

		mItemInList = new ShoppingList(getIdList());
		mItemInList.getItem().setImagePath(Image.getPathFromResource(getActivity(), R.drawable.no_image));
	}

	@Override
	protected void setupView(View v, Bundle savedInstanceState) {
		super.setupView(v, savedInstanceState);

		if (savedInstanceState != null) {
			loadImage(savedInstanceState.getString(STATE_IMAGE_PATH));

			if (!mIsUseCategory) {
				mCategorySpinner.setSelected(savedInstanceState.getLong(STATE_CATEGORY_ID));
			}
		} else {
			mUnitSpinner.setSelected(getActivity().getResources().getStringArray(R.array.units)[0]);
			mCategorySpinner.setSelected(getActivity().getResources().getStringArray(R.array.categories)[0].split("—")[0]);
		}

		mCollapsingToolbarLayout.setTitle(getString(R.string.add));
		mCollapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
	}

	@Override
	protected SimpleCursorAdapter getCompleteTextAdapter() {
		return super.getCompleteTextAdapter(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence charSequence) {
				Cursor managedCursor = mItemDS.getNames(charSequence != null ? charSequence.toString().trim() : null);

				if (managedCursor.moveToFirst()) {
					mIsProposedItem = true;
				}

				return managedCursor;
			}
		});
	}

	@Override
	protected void fillItemFromAutoComplete(Item item) {
		if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_amount_add), true) && item.getAmount() != 0) {
			mAmountEditText.setText(new DecimalFormat("#.######").format(item.getAmount()));
		}

		if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_unit_add), true)) {
			mUnitSpinner.setSelected(item.getIdUnit());
		}

		if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_price_add), true) && item.getPrice() != 0) {
			mPriceEditText.setText(String.format("%.2f", item.getPrice()));
		}

		if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_comment_add), true)) {
			mCommentEditText.setText(item.getComment());
		}
	}

	@Override
	protected long getIdList() {
		return getArguments().getLong(ID_LIST);
	}

	@Override
	protected boolean saveData() {
		boolean isSave = false;

		if (isValidData()) {
			refreshItem();
			mItemInList.updateItem(getActivity());

			if (mInfoTextView.getVisibility() == View.VISIBLE) { //If item in list, update it
				mItemsInListDS.update(mItemInList);
			} else { //Add new item to list
				mItemsInListDS.add(mItemInList);
			}

			sendResult(mItemInList.getIdItem());
			isSave = true;
		}

		return isSave;
	}

	@Override
	protected ShoppingList refreshItem() {
		if (mItemInList.getItem().isNew()) {
			String image = mItemInList.getItem().getImagePath();
			String defaultImage = mItemInList.getItem().getDefaultImagePath();

			if (image == null || image.equals(Image.getPathFromResource(getActivity(), R.drawable.no_image))) {
				defaultImage = image = Image.CHARACTER_IMAGE_PATH + (int) getName().toUpperCase().charAt(0) + ".png";
			}

			mItemInList.setItem(new Item(getName(), defaultImage == null ? image : defaultImage, image));
		} else {
			mItemInList.getItem().setName(getName());
		}

		return super.refreshItem();
	}

	@Override
	public void resetImage() {
		String path;

		if (!mItemInList.getItem().isNew() && mItemInList.getItem().getDefaultImagePath() != null) {
			path = mItemInList.getItem().getDefaultImagePath();
		} else {
			path = Image.getPathFromResource(getActivity(), R.drawable.no_image);
			mCollapsingToolbarLayout.setTitle(getString(R.string.add));
		}

		loadImage(path);
	}

	@Override
	public boolean isDeleteImage(String newPath) {
		boolean result = true;

		String imagePath = mItemInList.getItem().getImagePath();
		String defaultImagePath  = mItemInList.getItem().getDefaultImagePath();

		if (imagePath != null) {
			result = !imagePath.contains(Image.ASSETS_IMAGE_PATH);
		}

		if (defaultImagePath != null) {
			result = result && !imagePath.equals(newPath) && !imagePath.equals(defaultImagePath);
		}

		return result;
	}

	private boolean isValidData() {
		boolean isValid = true;

		if (mNameEditText.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_value));
			isValid = false;
		} else if (mNameEditText.length() < 3) {
			mNameInputLayout.setError(getString(R.string.error_length_name));
			isValid = false;
		}

		if (mNameInputLayout.isErrorEnabled() || mAmountInputLayout.isErrorEnabled() || mPriceInputLayout.isErrorEnabled()) {
			isValid = false;
		}

		return isValid;
	}
}
