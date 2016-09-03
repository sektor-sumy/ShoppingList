package ru.android.ainege.shoppinglist.ui.view.spinners;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.dataSources.GenericDS;
import ru.android.ainege.shoppinglist.db.entities.Catalog;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;

public abstract class GeneralSpinner {
	public static final int ID_ADD_CATALOG = -1;

	protected final Fragment mFragment;
	protected final SharedPreferences mPrefs;

	protected Spinner mSpinner;
	protected long mIdSelectedItem;
	protected boolean mIsUseNewItemInSpinner;

	protected abstract int getCode();
	protected abstract String getTeg();
	protected abstract SimpleCursorAdapter getAdapter();
	protected abstract CatalogDS getDS();
	protected abstract GeneralDialogFragment getDialog();

	public GeneralSpinner(Fragment fragment) {
		mFragment = fragment;

		mPrefs = PreferenceManager.getDefaultSharedPreferences(mFragment.getActivity());
		mIsUseNewItemInSpinner = mPrefs.getBoolean(mFragment.getActivity().getString(R.string.settings_key_fast_edit), false);
	}

	public Spinner getSpinner() {
		return mSpinner;
	}

	public void setSpinner(View spinner) {
		mSpinner = (Spinner) spinner;
		mSpinner.setAdapter(getAdapter());

		if (mIsUseNewItemInSpinner) {
			mSpinner.post(new Runnable() {
				public void run() {
					mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
							if (position == 0 && id == ID_ADD_CATALOG) {
								openAddItemDialog(getCode(), getTeg());
							} else {
								mIdSelectedItem = ((CatalogDS.CatalogCursor<Catalog>) mSpinner.getSelectedItem()).getEntity().getId();
							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {

						}
					});
				}
			});
		}
	}

	public void updateSpinner(long id, boolean updateSetting) {
		if (updateSetting) {
			mIsUseNewItemInSpinner = mPrefs.getBoolean(mFragment.getActivity().getString(R.string.settings_key_fast_edit), true);
		}

		mSpinner.setAdapter(getAdapter());
		setSelected(id);
	}

	protected Catalog getSelected() {
		return ((CatalogDS.CatalogCursor<Catalog>) mSpinner.getSelectedItem()).getEntity();
	}

	public void setSelected(String name) {
		mSpinner.setSelection(getPosition(name));
		mIdSelectedItem = ((CatalogDS.CatalogCursor<Catalog>) mSpinner.getSelectedItem()).getEntity().getId();
	}

	public void setSelected(long id) {
		if (id != ID_ADD_CATALOG) {
			mIdSelectedItem = id;
		}

		mSpinner.setSelection(getPosition(mIdSelectedItem));
	}

	protected Cursor getData() {
		Cursor cursor;

		if (mIsUseNewItemInSpinner) {
			cursor = getDS().getAllForSpinner();
		} else {
			cursor = getDS().getAll();
		}

		return cursor;
	}

	private void openAddItemDialog (int requestCode, String tag) {
		GeneralDialogFragment addItemDialog = getDialog();
		addItemDialog.setTargetFragment(mFragment, requestCode);
		addItemDialog.show(mFragment.getFragmentManager(), tag);

		FirebaseAnalytic.getInstance(mFragment.getActivity(), FirebaseAnalytic.ADD_CATALOG_FROM_SPINNER)
				.putString(FirebaseAnalytic.CONTENT_TYPE, tag)
				.addEvent();
	}

	private int getPosition(long id) {
		return getPosition(id, null);
	}

	private int getPosition(String name) {
		return getPosition(ID_ADD_CATALOG, name);
	}

	private int getPosition(long id, String name) {
		int index = ID_ADD_CATALOG;
		boolean byName = name != null;

		for (int i = 0; i < mSpinner.getCount(); i++) {
			Catalog catalog = ((GenericDS.EntityCursor<Catalog>) mSpinner.getItemAtPosition(i)).getEntity();

			if (byName ? catalog.getName().equals(name) : catalog.getId() == id) {
				index = i;
				break;
			}
		}

		if (index == ID_ADD_CATALOG) {
			index = mIsUseNewItemInSpinner ? 1 : 0;
		}

		return index;
	}
}
