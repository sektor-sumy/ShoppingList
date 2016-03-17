package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.R;

public class QuestionDialogFragment extends DialogFragment {
	private static final String MESSAGE = "message";
	private static final String OK_CLICK = "okClick";

	public static QuestionDialogFragment newInstance(String message) {
		Bundle args = new Bundle();
		args.putString(MESSAGE, message);

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
									Activity.RESULT_OK, null);
						}
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (getTargetFragment() != null) {
							getTargetFragment().onActivityResult(getTargetRequestCode(),
									Activity.RESULT_CANCELED, null);
						}
					}
				});

		return builder.create();
	}
}
