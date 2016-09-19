package ru.android.ainege.shoppinglist.ui.view.spinners;

import android.app.Fragment;
import android.widget.SimpleCursorAdapter;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.TableInterface;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.UnitDialogFragment;

public class UnitSpinner extends GeneralSpinner {
	public static final int UNIT_ADD = 403;
	protected static final String UNIT_ADD_DATE = "addUnitDialog";

	public UnitSpinner(Fragment fragment) {
		super(fragment);
	}

	@Override
	public Unit getSelected() {
		return (Unit) super.getSelected();
	}

	@Override
	protected int getCode() {
		return UNIT_ADD;
	}

	@Override
	protected String getTeg() {
		return UNIT_ADD_DATE;
	}

	@Override
	protected SimpleCursorAdapter getAdapter() {
		int layout;

		if (mIsCenterGravity) {
			layout = R.layout.spinner_unit;
		} else {
			layout = android.R.layout.simple_spinner_item;
		}

		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(mFragment.getActivity(),
				layout,
				getData(),
				new String[]{TableInterface.UnitsInterface.COLUMN_NAME},
				new int[]{android.R.id.text1}, 0);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spinnerAdapter;
	}

	@Override
	protected CatalogDS getDS() {
		return new UnitsDS(mFragment.getActivity());
	}

	@Override
	protected GeneralDialogFragment getDialog() {
		return new UnitDialogFragment();
	}
}
