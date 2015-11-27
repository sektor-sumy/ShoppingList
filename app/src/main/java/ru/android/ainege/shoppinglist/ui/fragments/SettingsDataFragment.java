package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.entities.Currency;

public class SettingsDataFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int CURRENCY_LOADER = 0;
	private static final String ADD_FRAGMENT_DATE = "addItemDialog";
	private static final String EDIT_FRAGMENT_DATE = "editItemDialog";
	public static final int ADD_FRAGMENT_CODE = 1;
	public static final int EDIT_FRAGMENT_CODE = 2;

	private RecyclerView mCurrencyRV;
	private RecyclerViewAdapter mAdapterRV;

	private ArrayList<Currency> mCurrency;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getLoaderManager().initLoader(CURRENCY_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_settings_data, container, false);

		Button add = (Button) v.findViewById(R.id.add);
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SettingsCurrencyDialogFragment addItemDialog = new SettingsCurrencyDialogFragment();
				addItemDialog.setTargetFragment(SettingsDataFragment.this, ADD_FRAGMENT_CODE);
				addItemDialog.show(getFragmentManager(), ADD_FRAGMENT_DATE);
			}
		});

		mCurrencyRV = (RecyclerView) v.findViewById(R.id.list);
		mCurrencyRV.setLayoutManager(new LinearLayoutManager(getActivity()));

		mAdapterRV = new RecyclerViewAdapter(getActivity());
		mCurrencyRV.setAdapter(mAdapterRV);

		return v;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> loader = null;
		switch (id) {
			case CURRENCY_LOADER:
				loader = new CurrencyCursorLoader(getActivity());
				break;
			default:
				break;
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case CURRENCY_LOADER:
				if (data.moveToFirst()) {
					mCurrency = ((CurrenciesDataSource.CurrencyCursor) data).getCurrencies();
					mAdapterRV.setData(mCurrency, true);
					ActionBar appBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
					if (appBar != null) {
						appBar.setTitle(getResources().getString(R.string.setting_currency));
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	private void updateData() {
		getLoaderManager().getLoader(CURRENCY_LOADER).forceLoad();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) return;

		switch(requestCode) {
			case ADD_FRAGMENT_CODE:
				updateData();
				break;
			case EDIT_FRAGMENT_CODE:
				updateData();
				break;
		}
	}

	public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		private Context mContext;
		private ArrayList<Currency> mCurrencies;

		public RecyclerViewAdapter(Context context) {
			mContext = context;
			mCurrencies = new ArrayList<>();
		}

		public void setData(ArrayList<Currency> currencies) {
			mCurrencies = currencies;
		}

		public void setData(ArrayList<Currency> items, boolean isNeedNotify) {
			setData(items);
			if (isNeedNotify) {
				notifyDataSetChanged();
			}
		}

		@Override
		public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._settings_currency, parent, false);
			ViewHolder vh = new ViewHolder(v);
			return vh;
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			Currency currency = mCurrencies.get(position);

			String s = currency.getSymbol() + " (" + currency.getName() + ")";
			holder.mName.setText(s);
		}

		@Override
		public int getItemCount() {
			return mCurrencies.size();
		}

		public void removeItem(int position) {
			mCurrencies.remove(position);
			notifyItemRemoved(position);
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public TextView mName;
			public ImageButton mEdit;
			public ImageButton mDelete;

			public ViewHolder(View v) {
				super(v);
				mName = (TextView) v.findViewById(R.id.name);
				mEdit = (ImageButton) v.findViewById(R.id.edit);
				mDelete = (ImageButton) v.findViewById(R.id.delete);

				mEdit.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						Currency currency = mCurrencies.get(itemPosition);

						SettingsCurrencyDialogFragment editItemDialog = SettingsCurrencyDialogFragment.newInstance(currency);
						editItemDialog.setTargetFragment(SettingsDataFragment.this, EDIT_FRAGMENT_CODE);
						editItemDialog.show(getFragmentManager(), EDIT_FRAGMENT_DATE);
					}
				});

				mDelete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						Currency currency = mCurrencies.get(itemPosition);

						if (mCurrencies.size() > 1) {
							CurrenciesDataSource ds = new CurrenciesDataSource(mContext);
							ds.delete(currency.getId());
							removeItem(itemPosition);
						} else {
							Toast.makeText(getActivity(), getResources().getString(R.string.error_one_item), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		}
	}

	private static class CurrencyCursorLoader extends CursorLoader {
		private Context mContext;

		public CurrencyCursorLoader(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public Cursor loadInBackground() {
			CurrenciesDataSource currenciesDS = new CurrenciesDataSource(mContext);

			return currenciesDS.getAll();
		}
	}
}
