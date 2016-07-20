package ru.android.ainege.shoppinglist.ui.fragments.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.ui.fragments.RetainedFragment;
import ru.android.ainege.shoppinglist.util.Image;

public class AddListDialogFragment extends ListDialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = createDialog(savedInstanceState);

		if (mDataFragment == null || savedInstanceState == null) {
			mDataFragment = new RetainedFragment(getActivity());
			getFragmentManager().beginTransaction().add(mDataFragment, RETAINED_FRAGMENT).commit();

			setRandomImage();
		} else {
			loadImage(mDataFragment.getImagePath());
		}

		return builder.create();
	}

	@Override
	protected long save(ListsDS listDS, String name, long idCurrency) {
		return listDS.add(new List(name, idCurrency, mImagePath));
	}

	@Override
	protected boolean isDeleteImage(String newPath) {
		return super.isDeleteImage(newPath) &&
				!mImagePath.contains(Image.ASSETS_IMAGE_PATH) &&
				!newPath.equals(mImagePath);
	}
}
