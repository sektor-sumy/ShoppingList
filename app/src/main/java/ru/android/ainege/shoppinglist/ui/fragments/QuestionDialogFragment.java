package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.R;

public class QuestionDialogFragment extends DialogFragment {
	private static final String MESSAGE = "message";
	public static final String ID = "id";
	public static final String POSITION = "position";
	private static final String OK_CLICK = "okClick";

	public static QuestionDialogFragment newInstance(String message) {
		Bundle args = new Bundle();
		args.putString(MESSAGE, message);

		QuestionDialogFragment fragment = new QuestionDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	public static QuestionDialogFragment newInstance(String message, long id, int position) {
		Bundle args = new Bundle();
		args.putString(MESSAGE, message);
		args.putLong(ID, id);
		args.putInt(POSITION, position);

		QuestionDialogFragment fragment = new QuestionDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getArguments().getString(MESSAGE))
				.setCancelable(false)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (getTargetFragment() != null) {
							getTargetFragment().onActivityResult(getTargetRequestCode(),
									Activity.RESULT_OK,
									new Intent().putExtra(OK_CLICK, true).
											putExtra(ID, getArguments().getLong(ID)).
											putExtra(POSITION, getArguments().getInt(POSITION)));
						}
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});

		return builder.create();
	}
}
