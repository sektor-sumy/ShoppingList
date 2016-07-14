package ru.android.ainege.shoppinglist.ui.fragments.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.TableInterface.UnitsInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.CurrenciesInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.CategoriesInterface;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.DictionaryDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.db.entities.Dictionary;
import ru.android.ainege.shoppinglist.db.entities.Unit;

public class DeleteDialogFragment extends DialogFragment {
	public static final String POSITION = "position";
	public static final String REPLACEMENT = "replacement";
	private static final String DICTIONARY = "dictionary";

	private Spinner mSpinner;

	public static DeleteDialogFragment newInstance(Dictionary dictionary, int position) {
		Bundle args = new Bundle();
		args.putSerializable(DICTIONARY, dictionary);
		args.putInt(POSITION, position);

		DeleteDialogFragment fragment = new DeleteDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = setupView();

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(v)
				.setMessage(getString(R.string.ask_delete_item))
				.setCancelable(false)
				.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendResult(((DictionaryDS.DictionaryCursor<Dictionary>) mSpinner.getSelectedItem()).getEntity());
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		return builder.create();
	}

	protected View setupView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_delete_dictionary, null);

		mSpinner = (Spinner) v.findViewById(R.id.spinner);

		Dictionary dictionary = (Dictionary) getArguments().getSerializable(DICTIONARY);

		if (dictionary instanceof Unit) {
			mSpinner.setAdapter(getUnitsAdapter(dictionary.getId()));
		} else if (dictionary instanceof Category) {
			mSpinner.setAdapter(getCategoriesAdapter(dictionary.getId()));
		} else if (dictionary instanceof Currency) {
			mSpinner.setAdapter(getCurrenciesAdapter(dictionary.getId()));
		}

		return v;
	}

	private SimpleCursorAdapter getUnitsAdapter(long id) {
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item,
				new UnitsDS(getActivity()).getAll(id),
				new String[]{UnitsInterface.COLUMN_NAME},
				new int[]{android.R.id.text1}, 0);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spinnerAdapter;
	}

	private SimpleCursorAdapter getCategoriesAdapter(long id) {
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item,
				new CategoriesDS(getActivity()).getAll(id),
				new String[]{CategoriesInterface.COLUMN_NAME},
				new int[]{android.R.id.text1}, 0);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spinnerAdapter;
	}

	private SimpleCursorAdapter getCurrenciesAdapter(long id) {
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item,
				new CurrenciesDS(getActivity()).getAll(id),
				new String[]{CurrenciesInterface.COLUMN_NAME},
				new int[]{android.R.id.text1}, 0);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spinnerAdapter;
	}

	protected void sendResult(Dictionary replacement) {
		if (getTargetFragment() == null)
			return;

		getTargetFragment().onActivityResult(getTargetRequestCode(), android.app.Activity.RESULT_OK,
				new Intent().putExtra(REPLACEMENT, replacement).putExtra(POSITION, getArguments().getInt(POSITION)));
	}

}
