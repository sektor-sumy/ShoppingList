package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.tables.CurrencyTable;

public class ListDialogFragment extends DialogFragment {
	public static final String ID_LIST = "idList";
	public static final String LIST = "list";

	private ImageView mImageList;
	private TextInputLayout mNameInputLayout;
	private EditText mName;
	private Spinner mCurrency;

	private List mEditList;

	public static ListDialogFragment newInstance(List list) {
		Bundle args = new Bundle();
		args.putSerializable(LIST, list);

		ListDialogFragment fragment = new ListDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_list, null);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.list_menu);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.take_photo:
						Toast.makeText(getActivity(), "new photo", Toast.LENGTH_SHORT).show();
						return true;
					case R.id.select_from_gallery:
						Toast.makeText(getActivity(), "image from gallery", Toast.LENGTH_SHORT).show();
						return true;
					case R.id.reset_image:
						Toast.makeText(getActivity(), "random image", Toast.LENGTH_SHORT).show();
						return true;
					default:
						return false;
				}
			}
		});

		mImageList = (ImageView) v.findViewById(R.id.image);
		mImageList.setImageResource(R.drawable.list);

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (EditText) v.findViewById(R.id.name);
		mCurrency = (Spinner) v.findViewById(R.id.currency);
		mCurrency.setAdapter(getSpinnerAdapter());
		mCurrency.setSelection(0);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(v)
				.setCancelable(true)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Boolean wantToCloseDialog = saveData();
						if (wantToCloseDialog) {
							dialog.dismiss();
						}
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

	private SimpleCursorAdapter getSpinnerAdapter(){
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.spinner_currency,
				new CurrenciesDataSource(getActivity()).getAll(),
				new String[] {CurrencyTable.COLUMN_SYMBOL, CurrencyTable.COLUMN_NAME},
				new int[] {R.id.currency_symbol, R.id.currency_name}, 0);
		spinnerAdapter.setDropDownViewResource(R.layout.spinner_currency_drop);
		return spinnerAdapter;
	}

	private void setDataToView() {

	}

	protected boolean saveData() {
		boolean isSave = false;
		String name = mName.getText().toString().trim();

		if (name.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_value));
		} else {
			mNameInputLayout.setError(null);
			mNameInputLayout.setErrorEnabled(false);
		}

		if (!mNameInputLayout.isErrorEnabled()) {
			long idCurrency = ((CurrenciesDataSource.CurrencyCursor) mCurrency.getSelectedItem()).getCurrency().getId();

			ListsDataSource listDS = new ListsDataSource(getActivity());

			long id;
			if (getArguments() == null) {
				id = listDS.add(new List(name, idCurrency));
			} else {
				id = mEditList.getId();
				listDS.update(new List(id, name, idCurrency));
			}

			sendResult(Activity.RESULT_OK, id);

			isSave = true;
		}

		return isSave;
	}

	private void sendResult(int resultCode, long id) {
		if (getTargetFragment() == null)
			return;

		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, new Intent().putExtra(ID_LIST, id));
	}
}