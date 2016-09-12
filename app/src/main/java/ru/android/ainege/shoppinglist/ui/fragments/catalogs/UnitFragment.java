package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.database.Cursor;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

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
	protected String getTitle(Toolbar toolbar) {
		return getString(R.string.catalogs_unit);
	}

	@Override
	protected CatalogAdapter getAdapter() {
		return new UnitAdapter();
	}

	@Override
	protected CatalogDS getDS() {
		return new UnitsDS(getActivity());
	}

	@Override
	protected GeneralDialogFragment getAddDialog() {
		return new UnitDialogFragment();
	}

	@Override
	protected GeneralDialogFragment getEditDialog() {
		return new UnitDialogFragment();
	}

	@Override
	protected ArrayList getCatalog(Cursor data) {
		return ((CatalogDS.CatalogCursor<Unit>) data).getEntities();
	}

	private class UnitAdapter extends RecyclerViewAdapter<RecyclerViewAdapter.ViewHolder> {

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dictionaries_item, parent, false);
			return new ViewHolder(v);
		}
	}
}
