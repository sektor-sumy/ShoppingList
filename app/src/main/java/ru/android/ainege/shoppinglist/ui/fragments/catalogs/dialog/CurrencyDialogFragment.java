package ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;

public class CurrencyDialogFragment extends GeneralDialogFragment<Currency> {
	private TextInputLayout mSymbolInputLayout;
	private EditText mSymbol;

	@Override
	protected View setupView(Bundle savedInstanceState) {
		View v = super.setupView(savedInstanceState);

		mSymbolInputLayout = (TextInputLayout) v.findViewById(R.id.symbol_input_layout);
		mSymbolInputLayout.setVisibility(View.VISIBLE);
		mSymbol = (EditText) v.findViewById(R.id.symbol);

		if (mIsEditDialog) {
			setDataToView(savedInstanceState);
		}

		return v;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.currency_info);
	}

	@Override
	protected void setDataToView(Bundle savedInstanceState) {
		super.setDataToView(savedInstanceState);

		if (savedInstanceState == null) {
			mSymbol.setText(mEditItem.getSymbol());
		}
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
			CurrenciesDS currencyDS = new CurrenciesDS(getActivity());

			long id;
			if (getArguments() == null) {
				id = currencyDS.add(new Currency(name, symbol));
				addAnalytics(FirebaseAnalytic.ADD_CURRENCY, name);
			} else {
				id = mEditItem.getId();
				currencyDS.update(new Currency(id, name, symbol));
			}

			sendResult(Activity.RESULT_OK, new Intent().putExtra(ID_ITEM, id));
			isSave = true;
		}

		return isSave;
	}
}
