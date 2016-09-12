package ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;

public class UnitDialogFragment extends GeneralDialogFragment<Unit> {

	@Override
	protected View setupView(Bundle savedInstanceState) {
		View v = super.setupView(savedInstanceState);

		mName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
		mName.setRawInputType(InputType.TYPE_CLASS_TEXT);

		if (mIsEditDialog) {
			setDataToView(savedInstanceState);
		}

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

			sendResult(Activity.RESULT_OK, new Intent().putExtra(ID_ITEM, id));
			isSave = true;
		}

		return isSave;
	}
}
