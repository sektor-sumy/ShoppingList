package ru.android.ainege.shoppinglist.ui.view.spinners;

import android.app.Fragment;
import android.widget.SimpleCursorAdapter;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.adapter.SpinnerColorAdapter;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.CategoryDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;

public class CategorySpinner extends GeneralSpinner {
	public static final int CATEGORY_ADD = 402;
	protected static final String CATEGORY_ADD_DATE = "addCategoryDialog";

	public CategorySpinner(Fragment fragment) {
		super(fragment);
	}

	@Override
	public Category getSelected() {
		return (Category) super.getSelected();
	}

	@Override
	protected int getCode() {
		return CATEGORY_ADD;
	}

	@Override
	protected String getTeg() {
		return CATEGORY_ADD_DATE;
	}

	@Override
	protected SimpleCursorAdapter getAdapter() {
		SimpleCursorAdapter spinnerAdapter = new SpinnerColorAdapter(mFragment.getActivity(),
				R.layout.spinner_color_item,
				getData());
		spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_color_item);
		return spinnerAdapter;
	}

	@Override
	protected CatalogDS getDS() {
		return new CategoriesDS(mFragment.getActivity());
	}

	@Override
	protected GeneralDialogFragment getDialog() {
		return new CategoryDialogFragment();
	}
}
