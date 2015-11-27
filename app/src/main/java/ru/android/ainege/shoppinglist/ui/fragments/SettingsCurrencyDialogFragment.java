package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.entities.Currency;

public class SettingsCurrencyDialogFragment extends DialogFragment {
	public static final String CURRENCY = "currency";
	public static final String ID_CURRENCY = "idCurrency";

	private TextInputLayout mSymbolInputLayout;
	private EditText mSymbol;
	private TextInputLayout mNameInputLayout;
	private EditText mName;

	private Currency mEditCurrency;

	public static SettingsCurrencyDialogFragment newInstance(Currency currency) {
		Bundle args = new Bundle();
		args.putSerializable(CURRENCY, currency);

		SettingsCurrencyDialogFragment fragment = new SettingsCurrencyDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_settings_currency, null);

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (EditText) v.findViewById(R.id.name);

		mSymbolInputLayout = (TextInputLayout) v.findViewById(R.id.symbol_input_layout);
		mSymbol = (EditText) v.findViewById(R.id.symbol);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.currency_info))
				.setView(v)
				.setCancelable(true)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		if (getArguments() != null) {
			setDataToView();
		}

		return builder.create();
	}

	private void setDataToView() {
		mEditCurrency = (Currency) getArguments().getSerializable(CURRENCY);

		mName.setText(mEditCurrency.getName());
		mName.setSelection(mName.getText().length());

		mSymbol.setText(mEditCurrency.getSymbol());
	}

	@Override
	public void onStart() {
		super.onStart();

		if (getDialog() == null) {
			return;
		}

		final AlertDialog dialog = (AlertDialog) getDialog();

		Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Boolean wantToCloseDialog = saveData();
				if (wantToCloseDialog) {
					dialog.dismiss();
				}
			}
		});
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
				id = mEditCurrency.getId();
				currencyDS.update(new Currency(id, name, symbol));
			}

			sendResult(Activity.RESULT_OK, id);

			isSave = true;
		}

		return isSave;
	}

	private void sendResult(int resultCode, long id) {
		if (getTargetFragment() == null)
			return;

		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, new Intent().putExtra(ID_CURRENCY, id));
	}
}
