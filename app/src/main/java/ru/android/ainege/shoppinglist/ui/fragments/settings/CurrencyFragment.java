package ru.android.ainege.shoppinglist.ui.fragments.settings;

import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.DictionaryDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.entities.Currency;

public class CurrencyFragment extends DictionaryFragment<Currency> {

	@Override
	protected View.OnClickListener getAddHandler() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralDialogFragment addItemDialog = new CurrencyDialogFragment();
				addItemDialog.setTargetFragment(CurrencyFragment.this, ADD_FRAGMENT_CODE);
				addItemDialog.show(getFragmentManager(), ADD_FRAGMENT_DATE);
			}
		};
	}

	@Override
	protected DictionaryDataSource getDS() {
		return new CurrenciesDataSource(getActivity());
	}

	@Override
	protected RecyclerViewAdapter getAdapter() {
		return new CurrencyViewAdapter();
	}

	@Override
	protected boolean isEntityUsed(long idCurrency) {
		return new ListsDataSource(getActivity()).isCurrencyUsed(idCurrency);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case DATA_LOADER:
				if (data.moveToFirst()) {
					mDictionary = ((CurrenciesDataSource.CurrencyCursor) data).getEntities();
					mAdapterRV.notifyDataSetChanged();

					ActionBar appBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
					if (appBar != null) {
						appBar.setTitle(getString(R.string.setting_currency));
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
		GeneralDialogFragment editItemDialog = CurrencyDialogFragment.newInstance(mDictionary.get(position));
		editItemDialog.setTargetFragment(CurrencyFragment.this, EDIT_FRAGMENT_CODE);
		editItemDialog.show(getFragmentManager(), EDIT_FRAGMENT_DATE);
	}

	private void saveCurrencySetting(long id) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(getString(R.string.settings_key_currency), id);
		editor.apply();
	}

	public class CurrencyViewAdapter extends RecyclerViewAdapter<CurrencyViewAdapter.CurrencyViewHolder> {
		public long mIdOld;
		public long mIdSelected;

		public CurrencyViewAdapter() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			mIdSelected = prefs.getLong(getString(R.string.settings_key_currency), -1);
		}

		@Override
		public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._settings_currency, parent, false);
			return new CurrencyViewHolder(v);
		}

		@Override
		public void onBindViewHolder(CurrencyViewHolder holder, int position) {
			super.onBindViewHolder(holder, position);

			if (mIdSelected == mDictionary.get(position).getId()) {
				holder.setImageSelected();
				mIdOld = mDictionary.get(position).getId();
			} else {
				holder.setImageNotSelected();
			}
		}

		@Override
		public void removeItem(int position) {
			super.removeItem(position);

			saveCurrencySetting(-1);
		}

		public class CurrencyViewHolder extends RecyclerViewAdapter<CurrencyViewHolder>.ViewHolder {
			public final ImageView mDefaultCurrency;

			public CurrencyViewHolder(View v) {
				super(v);

				mDefaultCurrency = (ImageView) v.findViewById(R.id.default_value);

				mDefaultCurrency.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						Currency currency = mDictionary.get(itemPosition);

						saveCurrencySetting(currency.getId());

						setImageSelected();
						mIdSelected = currency.getId();

						notifyItemChanged(getPosition(mDictionary, mIdOld));
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
