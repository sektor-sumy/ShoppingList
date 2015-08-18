package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;

public class ListDialogFragment extends DialogFragment {
    public static final String ID_LIST = "idList";
    public static final String NAME = "name";

    private EditText mName;
    private long mIdNewList;

    public interface List {
        void updateResult(long idList);
    }

    public static ListDialogFragment newInstance(long idList, String name) {
        Bundle args = new Bundle();
        args.putLong(ID_LIST, idList);
        args.putString(NAME, name);

        ListDialogFragment fragment = new ListDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_list, null);
        builder.setView(v)
                .setCancelable(false)
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
        setData(v);
        return builder.create();
    }

    private void setData(View v) {
        mName = (EditText) v.findViewById(R.id.new_list_name);
        if (getArguments() != null) {
            mName.setText(getArguments().getString(NAME));
            mName.setSelection(mName.getText().length());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if(getDialog() == null) {
            return;
        }

        final AlertDialog dialog = (AlertDialog) getDialog();
        Window window = dialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = saveData();
                if(wantToCloseDialog) {
                    dialog.dismiss();
                }
            }
        });
    }

    private boolean saveData() {
        boolean isSave = false;
        String name = mName.getText().toString();
        if(name.equals("")) {
            mName.setError(getResources().getText(R.string.error_name));
        } else {
            mName.setError(null);
        }
        if (mName.getError() == null) {
            ListsDataSource listDS = ListsDataSource.getInstance();

            if (getArguments() != null) {
                mIdNewList = getArguments().getLong(ID_LIST);
                listDS.update(mIdNewList, name);
            } else {
                mIdNewList = (int) listDS.add(name);
            }
            List l = (List) getActivity();
            l.updateResult(mIdNewList);
            isSave = true;
        }
        return isSave;
    }
}
