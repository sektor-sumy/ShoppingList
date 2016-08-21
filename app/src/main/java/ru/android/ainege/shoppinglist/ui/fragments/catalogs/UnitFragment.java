package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.content.Loader;
import android.database.Cursor;
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
	protected String getTitle() {
		return getString(R.string.catalogs_unit);
	}

	@Override
	protected View.OnClickListener getAddHandler() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralDialogFragment addItemDialog = new UnitDialogFragment();
				addItemDialog.setTargetFragment(UnitFragment.this, ADD);
				addItemDialog.show(getFragmentManager(), ADD_DATE);
			}
		};
	}

	@Override
	protected CatalogDS getDS() {
		return new UnitsDS(getActivity());
	}

	@Override
	protected RecyclerViewAdapter getAdapter() {
		return new UnitAdapter();
	}

	@Override
	protected boolean isEntityUsed(long idUnit) {
		return getDS().isUsed(idUnit);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case DATA_LOADER:
				if (mSaveListRotate != null && mSaveListRotate.size() > 0) {
					mCatalog = mSaveListRotate;
					mAdapterRV.notifyDataSetChanged();
				} else if (mSaveListRotate == null && data.moveToFirst()) {
					mCatalog = ((UnitsDS.UnitCursor) data).getEntities();
					mAdapterRV.notifyDataSetChanged();

					if (mLastEditId != -1) {
						mCatalogRV.scrollToPosition(getPosition(mLastEditId));
					}
				}

				break;
			default:
				break;
		}
	}

	@Override
	protected void showEditDialog(int position) {
		GeneralDialogFragment editItemDialog = UnitDialogFragment.newInstance(mCatalog.get(position));
		editItemDialog.setTargetFragment(UnitFragment.this, EDIT);
		editItemDialog.show(getFragmentManager(), EDIT_DATE);
	}

	@Override
	public int getKey() {
		return R.string.catalogs_key_unit;
	}

	private class UnitAdapter extends RecyclerViewAdapter<RecyclerViewAdapter.ViewHolder> {

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dictionaries_item, parent, false);
			return new ViewHolder(v);
		}
	}
}
