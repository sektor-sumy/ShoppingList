package ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.item;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.io.File;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.ui.view.PictureView;
import ru.android.ainege.shoppinglist.ui.view.spinners.CategorySpinner;
import ru.android.ainege.shoppinglist.ui.view.spinners.GeneralSpinner;
import ru.android.ainege.shoppinglist.ui.view.spinners.UnitSpinner;
import ru.android.ainege.shoppinglist.util.Image;

public abstract class ItemDialogFragment extends GeneralDialogFragment<Item> implements PictureView.PictureInterface{
	private static final String STATE_FILE = "state_file";

	protected PictureView mPictureView;
	protected TextView mInfoTextView;
	protected UnitSpinner mUnitSpinner;
	protected CategorySpinner mCategorySpinner;

	protected abstract void setDataToView(Bundle savedInstanceState);
	protected abstract TextWatcher getNameChangedListener();

	@Override
	protected View setupView(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_item_catalog, null);

		mPictureView = new PictureView(this, savedInstanceState, this);
		mUnitSpinner = new UnitSpinner(this);
		mCategorySpinner = new CategorySpinner(this);

		mPictureView.setImage(v.findViewById(R.id.image));
		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (EditText) v.findViewById(R.id.name);
		mName.addTextChangedListener(getNameChangedListener());
		mInfoTextView = (TextView) v.findViewById(R.id.info);
		mUnitSpinner.setSpinner(v.findViewById(R.id.unit));
		mCategorySpinner.setSpinner(v.findViewById(R.id.category));

		if (savedInstanceState != null) {
			mPictureView.setFile((File) savedInstanceState.getSerializable(STATE_FILE));
		}

		setDataToView(savedInstanceState);

		return v;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.catalogs_items);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (isDeleteImage(null)) {
			Image.deleteFile(mEditItem.getImagePath());
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPictureView.setImagePath(mEditItem.getImagePath());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(STATE_FILE, mPictureView.getFile());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mPictureView.onCreateContextMenu(menu, R.id.default_image, getString(R.string.item_image));

		// TODO: 03.09.2016 android bug
		MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onContextItemSelected(item);
				return true;
			}
		};

		for (int i = 0, n = menu.size(); i < n; i++)
			menu.getItem(i).setOnMenuItemClickListener(listener);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return mPictureView.onContextItemSelected(item, null) || super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case PictureView.TAKE_PHOTO:
					mPictureView.takePhotoResult(mEditItem.getImagePath());
					break;
				case PictureView.FROM_GALLERY:
					mPictureView.fromGalleryResult(mEditItem.getImagePath(), data.getData());
					break;
				case UnitSpinner.UNIT_ADD:
					mUnitSpinner.updateSpinner(data.getLongExtra(GeneralDialogFragment.ID_ITEM, 1), false);
				case CategorySpinner.CATEGORY_ADD:
					mCategorySpinner.updateSpinner(data.getLongExtra(GeneralDialogFragment.ID_ITEM, 1), false);
					break;
			}
		} else {
			switch (requestCode) {
				case UnitSpinner.UNIT_ADD:
					mUnitSpinner.setSelected(GeneralSpinner.ID_ADD_CATALOG);
					break;
				case CategorySpinner.CATEGORY_ADD:
					mCategorySpinner.setSelected(GeneralSpinner.ID_ADD_CATALOG);
					break;
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		mPictureView.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	protected boolean saveData() {
		boolean isSave = false;

		if (isValidData()) {
			refreshItem();
			long id;

			if (mEditItem.isNew()) {
				id = addItem();
			} else {
				id = mEditItem.getId();
				new ItemDS(getActivity()).update(mEditItem);
			}

			sendResult(Activity.RESULT_OK, new Intent().putExtra(ID_ITEM, id));
			isSave = true;
		}

		return isSave;
	}

	@Override
	public void loadImage(String path) {
		mPictureView.loadImage(path, mEditItem.getImagePath());
		mEditItem.setImagePath(path);
	}

	protected long addItem() {
		FirebaseCrash.report(new Exception("Catched exception: has entered in ItemDialogFragment.addItem(). Edit mode."));
		return 0;
	}

	protected void refreshItem() {
		mEditItem.setName(mName.getText().toString().trim());
		mEditItem.setUnit(mUnitSpinner.getSelected());
		mEditItem.setCategory(mCategorySpinner.getSelected());
	}

	protected boolean isValidData() {
		boolean isValid = true;

		if (mName.getText().toString().trim().length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_name));
			isValid = false;
		}

		if (mNameInputLayout.isErrorEnabled()) {
			isValid = false;
		}

		return isValid;
	}
}
