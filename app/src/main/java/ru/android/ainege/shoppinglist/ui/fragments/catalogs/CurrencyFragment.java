package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.CurrencyDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;

public class CurrencyFragment extends CatalogFragment<Currency> {
	private void showCaseView() {
		CurrencyAdapter.CurrencyHolder holder;
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_CURRENCY);

		if (!sequence.hasFired()) {
			holder = (CurrencyAdapter.CurrencyHolder) mCatalogRV.findViewHolderForLayoutPosition(0);

			Showcase.createShowcase(getActivity(), holder.itemView,
					getString(R.string.showcase_default_currency))
					.withRectangleShape()
					.setShapePadding(2)
					.singleUse(Showcase.SHOT_CURRENCY)
					.show();
		}
	}

	@Override
	protected String getTitle() {
		return getString(R.string.catalogs_currency);
	}

	@Override
	protected View.OnClickListener getAddHandler() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralDialogFragment addItemDialog = new CurrencyDialogFragment();
				addItemDialog.setTargetFragment(CurrencyFragment.this, ADD);
				addItemDialog.show(getFragmentManager(), ADD_DATE);
			}
		};
	}

	@Override
	protected CatalogDS getDS() {
		return new CurrenciesDS(getActivity());
	}

	@Override
	protected RecyclerViewAdapter getAdapter() {
		return new CurrencyAdapter();
	}

	@Override
	protected boolean isEntityUsed(long idCurrency) {
		return getDS().isUsed(idCurrency);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case DATA_LOADER:
				if (mSaveListRotate != null && mSaveListRotate.size() > 0) {
					mCatalog = mSaveListRotate;
					mAdapterRV.notifyDataSetChanged();
				} else if (mSaveListRotate == null && data.moveToFirst()) {
					mCatalog = ((CurrenciesDS.CurrencyCursor) data).getEntities();
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
		GeneralDialogFragment editItemDialog = CurrencyDialogFragment.newInstance(mCatalog.get(position));
		editItemDialog.setTargetFragment(CurrencyFragment.this, EDIT);
		editItemDialog.show(getFragmentManager(), EDIT_DATE);
	}

	private void saveCurrencySetting(long id) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(getString(R.string.catalogs_key_currency), id);
		editor.apply();
	}

	public class CurrencyAdapter extends RecyclerViewAdapter<CurrencyAdapter.CurrencyHolder> {
		public long mIdOld;
		public long mIdSelected;
		private boolean mIsShowShowcase = true;

		public CurrencyAdapter() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			mIdSelected = prefs.getLong(getString(R.string.catalogs_key_currency), -1);
		}

		@Override
		public CurrencyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dictionaries_item, parent, false);
			return new CurrencyHolder(v);
		}

		@Override
		public void onBindViewHolder(CurrencyHolder holder, int position) {
			super.onBindViewHolder(holder, position);

			if (mIdSelected == mCatalog.get(position).getId()) {
				holder.setImageSelected();
				mIdOld = mCatalog.get(position).getId();
			} else {
				holder.setImageNotSelected();
			}
		}

		@Override
		public void removeItem(int position) {
			Currency c = mCatalog.get(position);

			if (c.getId() == mIdSelected) {
				saveCurrencySetting(-1);
			}

			super.removeItem(position);
		}

		@Override
		public void onViewAttachedToWindow(CurrencyHolder holder) {
			super.onViewAttachedToWindow(holder);

			if (mIsShowShowcase) {
				mIsShowShowcase = false;
				holder.mDefaultCurrency.post(new Runnable() {
					@Override
					public void run() {
						showCaseView();
					}
				});
			}
		}

		public class CurrencyHolder extends RecyclerViewAdapter<CurrencyHolder>.ViewHolder {
			public final ImageView mDefaultCurrency;

			public CurrencyHolder(View v) {
				super(v);

				mDefaultCurrency = (ImageView) v.findViewById(R.id.default_currency);
				mDefaultCurrency.setVisibility(View.VISIBLE);

				mDefaultCurrency.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						Currency currency = mCatalog.get(itemPosition);

						saveCurrencySetting(currency.getId());

						setImageSelected();
						mIdSelected = currency.getId();

						notifyItemChanged(getPosition(mCatalog, mIdOld));
						mIdOld = mIdSelected;
					}
				});

			}

			private void setImage(int image) {
				mDefaultCurrency.setImageResource(image);
			}

			private void setImageSelected() {
				setImage(R.drawable.ic_grade_orange_24dp);
			}

			private void setImageNotSelected() {
				setImage(R.drawable.ic_grade_grey_24dp);
			}

			private int getPosition(ArrayList<Currency> currencies, long idCurrency) {
				int index = 0;
				for (int i = 0; i < currencies.size(); i++) {
					long id = currencies.get(i).getId();
					if (id == idCurrency) {
						index = i;
						break;
					}
				}
				return index;
			}
		}
	}
}