package ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog;

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
import ru.android.ainege.shoppinglist.db.entities.Catalog;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;

public abstract class GeneralDialogFragment<T extends Catalog> extends DialogFragment {
	protected static final String ITEM = "item";
	public static final String ID_ITEM = "idItem";

	protected TextInputLayout mNameInputLayout;
	protected EditText mName;

	protected T mEditItem;
	protected boolean mIsEditDialog = false;

	protected abstract String getTitle();
	protected abstract boolean saveData();

	public static GeneralDialogFragment newInstance(GeneralDialogFragment fragment, Catalog catalog) {
		Bundle args = new Bundle();
		args.putSerializable(ITEM, catalog);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getTitle())
				.setView(setupView(savedInstanceState))
				.setCancelable(true)
				.setPositiveButton(R.string.save, null)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.cancel();
					}
				});

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		final AlertDialog dialog = (AlertDialog) getDialog();

		if (dialog != null) {
			Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (saveData()) {
						dialog.dismiss();
					}
				}
			});
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		sendResult(Activity.RESULT_CANCELED, null);
	}

	protected View setupView(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_catalogs, null);

		if (getArguments() != null) {
			mIsEditDialog = true;
			mEditItem = (T) getArguments().getSerializable(ITEM);
		}

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (EditText) v.findViewById(R.id.name);

		return v;
	}

	protected void setDataToView(Bundle savedInstanceState) {
		if (mIsEditDialog) {
			mEditItem = (T) getArguments().getSerializable(ITEM);
		}

		if (savedInstanceState == null) {
			mName.setText(mEditItem != null ? mEditItem.getName() : null);
		}

		mName.setSelection(mName.getText().length());
	}

	protected void sendResult(int resultCode, Intent intent) {
		if (getTargetFragment() != null) {
			getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
		}
	}

	protected void addAnalytics(String catalog, String name) {
		FirebaseAnalytic.getInstance(getActivity(), catalog)
				.putString(FirebaseAnalytic.NAME, name)
				.addEvent();
	}
}
