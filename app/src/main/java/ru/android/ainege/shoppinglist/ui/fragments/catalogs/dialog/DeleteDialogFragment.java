package ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog;

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

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.TableInterface.UnitsInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.CurrenciesInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.CategoriesInterface;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Currency;
import ru.android.ainege.shoppinglist.db.entities.Catalog;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.entities.Unit;

public class DeleteDialogFragment extends DialogFragment {
	public static final String POSITION = "position";
	public static final String CATALOG = "catalog";
	private static final String LISTS = "lists";
	public static final String OLD_ID = "old_id";
	public static final String NEW_ID = "new_id";

	private Catalog mCatalog;
	private Spinner mSpinner;

	public static DeleteDialogFragment newInstance(int position, Catalog catalog, ArrayList lists) {
		Bundle args = new Bundle();
		args.putInt(POSITION, position);
		args.putSerializable(CATALOG, catalog);
		args.putSerializable(LISTS, lists);

		DeleteDialogFragment fragment = new DeleteDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = setupView();
		String text;

		if (mCatalog instanceof Item) {
			text = getString(R.string.ask_delete_item);

			for (List list : (ArrayList<List>) getArguments().getSerializable(LISTS)) {
				text += "\n\"" + list.getName() + "\"";
			}
		} else {
			text = getString(R.string.ask_delete_catalog);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(v)
				.setMessage(text)
				.setCancelable(false)
				.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mSpinner.getVisibility() == View.VISIBLE) {
							sendResult(((CatalogDS.CatalogCursor<Catalog>) mSpinner.getSelectedItem()).getEntity().getId());
						} else {
							sendResult(-1);
						}
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
		View v = inflater.inflate(R.layout.dialog_delete_catalog, null);

		mCatalog = (Catalog) getArguments().getSerializable(CATALOG);
		mSpinner = (Spinner) v.findViewById(R.id.spinner);

		if (mCatalog instanceof Item) {
			mSpinner.setVisibility(View.GONE);
		} else if (mCatalog instanceof Unit) {
			mSpinner.setAdapter(getUnitsAdapter(mCatalog.getId()));
		} else if (mCatalog instanceof Category) {
			mSpinner.setAdapter(getCategoriesAdapter(mCatalog.getId()));
		} else if (mCatalog instanceof Currency) {
			mSpinner.setAdapter(getCurrenciesAdapter(mCatalog.getId()));
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

	protected void sendResult(long newID) {
		if (getTargetFragment() == null)
			return;

		getTargetFragment().onActivityResult(getTargetRequestCode(), android.app.Activity.RESULT_OK,
				new Intent().putExtra(POSITION, getArguments().getInt(POSITION))
						.putExtra(OLD_ID, mCatalog.getId())
						.putExtra(NEW_ID, newID));
	}

}
