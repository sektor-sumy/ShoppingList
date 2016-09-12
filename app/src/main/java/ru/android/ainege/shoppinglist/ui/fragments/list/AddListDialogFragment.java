package ru.android.ainege.shoppinglist.ui.fragments.list;

import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.entities.List;

public class AddListDialogFragment extends ListDialogFragment {

	@Override
	protected void setDataToView() {
		setRandomImage();
	}

	@Override
	protected long save(ListsDS listDS, String name, long idCurrency) {
		return listDS.add(new List(name, idCurrency, mImagePath));
	}
}
