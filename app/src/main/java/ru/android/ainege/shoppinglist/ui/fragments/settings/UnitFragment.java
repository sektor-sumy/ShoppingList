package ru.android.ainege.shoppinglist.ui.fragments.settings;

import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.DictionaryDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.Unit;

public class UnitFragment extends DictionaryFragment<Unit> {

	@Override
	protected View.OnClickListener getAddHandler() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralDialogFragment addItemDialog = new UnitDialogFragment();
				addItemDialog.setTargetFragment(UnitFragment.this, ADD_FRAGMENT_CODE);
				addItemDialog.show(getFragmentManager(), ADD_FRAGMENT_DATE);
			}
		};
	}

	@Override
	protected DictionaryDataSource getDS() {
		return new UnitsDataSource(getActivity());
	}

	@Override
	protected RecyclerViewAdapter getAdapter() {
		return new UnitViewAdapter();
	}

	@Override
	protected boolean isEntityUsed(long idUnit) {
		return new ShoppingListDataSource(getActivity()).isUnitUsed(idUnit);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case DATA_LOADER:
				if (data.moveToFirst()) {
					mDictionary = ((UnitsDataSource.UnitCursor) data).getEntities();
					mAdapterRV.notifyDataSetChanged();

					ActionBar appBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
					if (appBar != null) {
						appBar.setTitle(getString(R.string.setting_title_unit));
					}
				}
				data.close();
				break;
			default:
				break;
		}
	}

	@Override
	protected void showEditDialog(int position) {
		GeneralDialogFragment editItemDialog = UnitDialogFragment.newInstance(mDictionary.get(position));
		editItemDialog.setTargetFragment(UnitFragment.this, EDIT_FRAGMENT_CODE);
		editItemDialog.show(getFragmentManager(), EDIT_FRAGMENT_DATE);
	}

	private class UnitViewAdapter extends RecyclerViewAdapter<RecyclerViewAdapter.ViewHolder> {

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._settings_currency, parent, false);
			return new ViewHolder(v);
		}
	}
}
