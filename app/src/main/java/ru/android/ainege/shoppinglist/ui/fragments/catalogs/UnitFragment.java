package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Unit;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.UnitDialogFragment;

public class UnitFragment extends CatalogFragment<Unit> {

	@Override
	public int getKey() {
		return R.string.catalogs_key_unit;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.catalogs_unit);
	}

	@Override
	protected RecyclerViewAdapter getAdapter() {
		return new UnitAdapter();
	}

	@Override
	protected CatalogDS getDS() {
		return new UnitsDS(getActivity());
	}

	@Override
	protected GeneralDialogFragment getDialog() {
		return new UnitDialogFragment();
	}

	private class UnitAdapter extends RecyclerViewAdapter<RecyclerViewAdapter.ViewHolder> {

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dictionaries_item, parent, false);
			return new ViewHolder(v);
		}
	}
}
