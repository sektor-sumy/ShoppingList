package ru.android.ainege.shoppinglist.ui.fragments.settings;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.entities.Currency;

public class CurrencyDialogFragment extends GeneralDialogFragment<Currency> {
	private TextInputLayout mSymbolInputLayout;
	private EditText mSymbol;

	public static CurrencyDialogFragment newInstance(Currency currency) {
		Bundle args = new Bundle();
		args.putSerializable(ITEM, currency);

		CurrencyDialogFragment fragment = new CurrencyDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	protected View setupView() {
		View v = super.setupView();

		mSymbolInputLayout = (TextInputLayout) v.findViewById(R.id.symbol_input_layout);
		mSymbolInputLayout.setVisibility(View.VISIBLE);
		mSymbol = (EditText) v.findViewById(R.id.symbol);

		return v;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.currency_info);
	}

	@Override
	protected void setDataToView() {
		super.setDataToView();

		mSymbol.setText(mEditItem.getSymbol());
	}

	protected boolean saveData() {
		boolean isSave = false;
		String name = mName.getText().toString().trim();

		if (name.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_name));
		} else {
			mNameInputLayout.setError(null);
			mNameInputLayout.setErrorEnabled(false);
		}

		String symbol = mSymbol.getText().toString().trim();

		if (symbol.length() == 0) {
			mSymbolInputLayout.setError(getString(R.string.error_empty));
		} else {
			mSymbolInputLayout.setError(null);
			mSymbolInputLayout.setErrorEnabled(false);
		}

		if (!mNameInputLayout.isErrorEnabled() && !mSymbolInputLayout.isErrorEnabled()) {
			CurrenciesDataSource currencyDS = new CurrenciesDataSource(getActivity());

			long id;
			if (getArguments() == null) {
				id = currencyDS.add(new Currency(name, symbol));
			} else {
				id = mEditItem.getId();
				currencyDS.update(new Currency(id, name, symbol));
			}

			sendResult(id);

			isSave = true;
		}

		return isSave;
	}
}
