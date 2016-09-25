package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import ru.android.ainege.shoppinglist.R;

public class QuestionDialogFragment extends DialogFragment {
	public static final String ID = "id";
	private static final String MESSAGE = "message";

	public static QuestionDialogFragment newInstance(String message, long id) {
		Bundle args = new Bundle();
		args.putString(MESSAGE, message);
		args.putLong(ID, id);

		QuestionDialogFragment fragment = new QuestionDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getArguments().getString(MESSAGE))
				.setCancelable(true)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						sendResult(Activity.RESULT_OK, new Intent().putExtra(ID, getArguments().getLong(ID)));
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						sendResult(Activity.RESULT_CANCELED, null);
					}
				});

		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		sendResult(Activity.RESULT_CANCELED, null);
	}

	private void sendResult(int resultCode, Intent intent) {
		if (getTargetFragment() == null)
			return;

		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
	}
}
