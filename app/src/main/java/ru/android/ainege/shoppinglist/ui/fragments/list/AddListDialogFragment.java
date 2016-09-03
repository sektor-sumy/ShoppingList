package ru.android.ainege.shoppinglist.ui.fragments.list;

import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.util.Image;

public class AddListDialogFragment extends ListDialogFragment {

	@Override
	protected void setDataToView() {
		setRandomImage();
	}

	@Override
	protected long save(ListsDS listDS, String name, long idCurrency) {
		return listDS.add(new List(name, idCurrency, mImagePath));
	}

	@Override
	public boolean isDeleteImage(String newPath) {
		return super.isDeleteImage(newPath) &&
				!mImagePath.contains(Image.ASSETS_IMAGE_PATH) &&
				!newPath.equals(mImagePath);
	}
}
