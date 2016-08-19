package ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;

public class UnitDialogFragment extends GeneralDialogFragment<Unit> {

	public static UnitDialogFragment newInstance(Unit unit) {
		Bundle args = new Bundle();
		args.putSerializable(ITEM, unit);

		UnitDialogFragment fragment = new UnitDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	protected View setupView() {
		View v = super.setupView();
		mName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
		mName.setRawInputType(InputType.TYPE_CLASS_TEXT);
		return v;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.unit_info);
	}

	@Override
	protected boolean saveData() {
		boolean isSave = false;
		String name = mName.getText().toString().trim();

		if (name.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_name));
		} else {
			mNameInputLayout.setError(null);
			mNameInputLayout.setErrorEnabled(false);
		}

		if (!mNameInputLayout.isErrorEnabled()) {
			UnitsDS unitDS = new UnitsDS(getActivity());

			long id;
			if (getArguments() == null) {
				id = unitDS.add(new Unit(name));
				addAnalytics(FirebaseAnalytic.ADD_UNIT, name);
			} else {
				id = mEditItem.getId();
				unitDS.update(new Unit(id, name));
			}

			sendResult(id);
			isSave = true;
		}

		return isSave;
	}
}
