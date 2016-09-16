package ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.item;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;
import ru.android.ainege.shoppinglist.util.Image;

public class AddItemDialogFragment extends ItemDialogFragment {

	@Override
	public boolean isDeleteImage(String newPath) {
		boolean result = true;

		String imagePath = mEditItem.getImagePath();
		String defaultImagePath  = mEditItem.getDefaultImagePath();

		if (imagePath != null) {
			result = !imagePath.contains(Image.ASSETS_IMAGE_PATH);
		}

		if (defaultImagePath != null) {
			result = result && !newPath.equals(imagePath) && !defaultImagePath.equals(imagePath);
		}

		return result;
	}

	@Override
	public void resetImage() {
		String path;

		if (!mEditItem.isNew() && mEditItem.getDefaultImagePath() != null) {
			path = mEditItem.getDefaultImagePath();
		} else {
			path = Image.getPathFromResource(getActivity(), R.drawable.no_image);
		}

		loadImage(path);
	}

	@Override
	protected void setDataToView(Bundle savedInstanceState) {
		mEditItem = new Item();
		mEditItem.setImagePath(Image.getPathFromResource(getActivity(), R.drawable.no_image));

		if (savedInstanceState == null) {
			mUnitSpinner.setSelected(getActivity().getResources().getStringArray(R.array.units)[0]);
			mCategorySpinner.setSelected(getActivity().getResources().getStringArray(R.array.categories)[0].split("—")[0]);
		} else {
			loadImage(mPictureView.getImagePath());
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
				if (s.length() > 0 && s.toString().equals(s.toString().trim())) {
					mNameInputLayout.setError(null);
					mNameInputLayout.setErrorEnabled(false);
					mEditItem.setId(0);

					//check the item in the catalog. If there is - select it
					ItemDS.ItemCursor cursorItem = new ItemDS(getActivity()).getWithData(s.toString().trim());

					if (cursorItem.moveToFirst()) {
						mInfoTextView.setText(getString(R.string.error_add_exist_item));
						mInfoTextView.setTextColor(Color.RED);
						mInfoTextView.setVisibility(View.VISIBLE);

						Item item = cursorItem.getEntity();
						mEditItem.setId(item.getId());
						mEditItem.setIdItemData(item.getIdItemData());
					}

					cursorItem.close();
				}
			}
		};
	}

	@Override
	protected long addItem() {
		long id = new ItemDS(getActivity()).add(mEditItem);

		FirebaseAnalytic.getInstance(getActivity(), FirebaseAnalytic.NEW_ITEM)
				.putString(FirebaseAnalytic.NAME, mEditItem.getName())
				.putString(FirebaseAnalytic.UNIT, mEditItem.getUnit().getName())
				.putString(FirebaseAnalytic.CATEGORY, mEditItem.getCategory().getName())
				.addEvent();

		return id;
	}

	@Override
	protected void refreshItem() {
		super.refreshItem();

		if (mEditItem.isNew()) {
			String image = mEditItem.getImagePath();
			String defaultImage = mEditItem.getDefaultImagePath();

			if (image.equals(Image.getPathFromResource(getActivity(), R.drawable.no_image))) {
				String name = mName.getText().toString().trim();
				defaultImage = image = Image.CHARACTER_IMAGE_PATH + (int) name.toUpperCase().charAt(0) + ".png";
			}

			mEditItem.setImagePath(image);
			mEditItem.setDefaultImagePath(defaultImage == null ? image : defaultImage);
		}
	}
}
