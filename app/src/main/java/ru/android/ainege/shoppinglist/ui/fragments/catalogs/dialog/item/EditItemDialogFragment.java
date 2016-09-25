package ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.item;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.util.Image;

public class EditItemDialogFragment extends ItemDialogFragment{
	private Item mOriginalItem;

	@Override
	public boolean isDeleteImage(String newPath) {
		String imagePath = mEditItem.getImagePath();
		String defaultImagePath  = mEditItem.getDefaultImagePath();

		return !imagePath.contains(Image.ASSETS_IMAGE_PATH) &&
				!imagePath.equals(newPath) &&
				!imagePath.equals(mOriginalItem.getImagePath()) &&
				!imagePath.equals(defaultImagePath);
	}

	@Override
	public void resetImage() {
		if (!mEditItem.getImagePath().equals(mEditItem.getDefaultImagePath())) {
			loadImage(mEditItem.getDefaultImagePath());
		}
	}

	@Override
	protected void setDataToView(Bundle savedInstanceState) {
		mEditItem = new Item((Item) getArguments().getSerializable(ITEM));
		mOriginalItem = new Item(mEditItem);

		if (savedInstanceState == null) {
			loadImage(mEditItem.getImagePath());
			mName.setText(mEditItem.getName());
			mUnitSpinner.setSelected(mEditItem.getUnit().getId());
			mCategorySpinner.setSelected(mEditItem.getCategory().getId());
		} else {
			loadImage(savedInstanceState.getString(STATE_IMAGE_PATH));
		}
	}

	@Override
	protected TextWatcher getNameChangedListener() {
		return new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0 && s.toString().equals(s.toString().trim()) &&
						!mOriginalItem.getName().equals(s.toString().trim())) {
					mNameInputLayout.setError(null);
					mNameInputLayout.setErrorEnabled(false);

					//check the item in the catalog. If there is - set error
					ItemDS.ItemCursor cursorItem = new ItemDS(getActivity()).getWithData(s.toString().trim());

					if (cursorItem.moveToFirst()) {
						mNameInputLayout.setError(getString(R.string.error_edit_exist_item));
					}

					cursorItem.close();
				}
			}
		};
	}

	@Override
	protected boolean saveData() {
		boolean isSaveData = super.saveData();

		if (isSaveData) {
			deleteOriginalImage();
		}

		return isSaveData;
	}

	@Override
	protected void refreshItem() {
		super.refreshItem();

		String defaultImage = mEditItem.getDefaultImagePath();
		int firstCharCode = (int) mName.getText().toString().trim().toUpperCase().charAt(0);

		if (defaultImage.contains(Image.CHARACTER_IMAGE_PATH) &&
				!defaultImage.contains(String.valueOf(firstCharCode))) {
			String image = Image.CHARACTER_IMAGE_PATH + firstCharCode + ".png";

			mEditItem.setImagePath(image);
			mEditItem.setDefaultImagePath(image);
		}
	}

	private void deleteOriginalImage() {
		String originImagePath = mOriginalItem.getImagePath();

		if (!originImagePath.contains(Image.ASSETS_IMAGE_PATH) &&
				!originImagePath.equals(mEditItem.getImagePath()) &&
				!originImagePath.equals(mEditItem.getDefaultImagePath())) {
			Image.deleteFile(originImagePath);
		}
	}
}
