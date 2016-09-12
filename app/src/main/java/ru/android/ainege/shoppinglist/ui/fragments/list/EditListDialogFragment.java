package ru.android.ainege.shoppinglist.ui.fragments.list;

import android.app.Dialog;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.util.Image;

public class EditListDialogFragment extends ListDialogFragment {
	private static final String LIST = "list";

	private List mOriginalList;
	private List mEditList;

	public static ListDialogFragment newInstance(List list) {
		Bundle args = new Bundle();
		args.putSerializable(LIST, list);

		ListDialogFragment fragment = new EditListDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mOriginalList = new List((List) getArguments().getSerializable(LIST));
		mEditList = new List((List) getArguments().getSerializable(LIST));

		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	protected void setDataToView() {
		loadImage(mEditList.getImagePath());

		mNameEditText.setText(mEditList.getName());
		mNameEditText.setSelection(mNameEditText.getText().length());

		mCurrencySpinner.setSelection(getPosition(mCurrencySpinner, mEditList.getIdCurrency()));
	}

	@Override
	protected long save(ListsDS listDS, String name, long idCurrency) {
		long id = mEditList.getId();
		listDS.update(new List(id, name, idCurrency, mImagePath));

		if (!mOriginalList.getImagePath().contains(Image.ASSETS_IMAGE_PATH) && !mOriginalList.getImagePath().equals(mImagePath)) {
			Image.deleteFile(mOriginalList.getImagePath());
		}

		return id;
	}

	@Override
	public boolean isDeleteImage(String newPath) {
		return super.isDeleteImage(newPath) &&
				!mImagePath.equals(mOriginalList.getImagePath());
	}
}
