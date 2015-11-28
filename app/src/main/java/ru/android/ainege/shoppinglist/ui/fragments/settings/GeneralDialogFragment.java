package ru.android.ainege.shoppinglist.ui.fragments.settings;

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
import ru.android.ainege.shoppinglist.db.entities.Dictionary;

public abstract class GeneralDialogFragment<T extends Dictionary> extends DialogFragment {
	public static final String ITEM = "item";
	public static final String ID_ITEM = "idItem";

	protected TextInputLayout mNameInputLayout;
	protected EditText mName;

	protected T mEditItem;

	protected abstract String getTitle();
	protected abstract boolean saveData();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = setView();

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getTitle())
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

		return builder.create();
	}

	protected View setView() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_settings_currency, null);

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (EditText) v.findViewById(R.id.name);

		if (getArguments() != null) {
			setDataToView();
		}

		return v;
	}

	protected void setDataToView() {
		mEditItem = (T) getArguments().getSerializable(ITEM);

		mName.setText(mEditItem.getName());
		mName.setSelection(mName.getText().length());
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

	protected void sendResult(int resultCode, long id) {
		if (getTargetFragment() == null)
			return;

		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, new Intent().putExtra(ID_ITEM, id));
	}
}
