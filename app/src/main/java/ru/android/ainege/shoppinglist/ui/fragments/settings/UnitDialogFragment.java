package ru.android.ainege.shoppinglist.ui.fragments.settings;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.Unit;

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
		mName.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });
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
			UnitsDataSource unitDS = new UnitsDataSource(getActivity());

			long id;
			if (getArguments() == null) {
				id = unitDS.add(new Unit(name));
			} else {
				id = mEditItem.getId();
				unitDS.update(new Unit(id, name));
			}

			sendResult(Activity.RESULT_OK, id);

			isSave = true;
		}

		return isSave;
	}
}
