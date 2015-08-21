package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.Validation;

public class AddItemDialogFragment extends DialogFragment {
    public static final String ID_LIST = "idList";
    public static final String ID_ITEM = "idItem";

    private EditText mName;
    private EditText mAmount;
    private EditText mPrice;
    private CheckBox mIsBought;
    private Spinner mUnits;

    public static AddItemDialogFragment newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(ID_LIST, id);

        AddItemDialogFragment fragment = new AddItemDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_item, null);
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
        UnitsDataSource unitDS = new UnitsDataSource(getActivity());

        String[] from = new String[] {UnitsTable.COLUMN_NAME};
        int[] to = new int[] {android.R.id.text1};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, unitDS.getAll(), from, to, 0);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        mUnits.setAdapter(adapter);
        mUnits.setSelection(0);

        mName = (EditText) v.findViewById(R.id.new_item_name);
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    mName.setError(getResources().getText(R.string.error_name));
                } else {
                    if(mName.getError() != null) {
                        mName.setError(null);
                    }
                }
            }
        });

        mAmount = (EditText) v.findViewById(R.id.new_amount_item);
        mAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    if(!Validation.isValid(mAmount.getText().toString().trim(), false)) {
                        mAmount.setError(getResources().getText(R.string.error_value));
                    } else {
                        if(mAmount.getError() != null) {
                            mAmount.setError(null);
                        }
                    }
                }
            }
        });

        mPrice = (EditText) v.findViewById(R.id.new_item_price);
        mPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s != null) {
                    if (!Validation.isValid(mPrice.getText().toString().trim(), true)) {
                        mPrice.setError(getResources().getText(R.string.error_value));
                    } else {
                        if(mPrice.getError() != null) {
                            mPrice.setError(null);
                        }
                    }
                }
            }
        });

        mIsBought = (CheckBox) v.findViewById(R.id.is_bought);
    }

    @Override
    public void onStart() {
        super.onStart();

        if(getDialog() == null) {
            return;
        }

        final AlertDialog dialog = (AlertDialog) getDialog();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.TOP);
        window.getAttributes().y = 30;
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        int dw = displaymetrics.widthPixels;
        window.setLayout(dw, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean wantToCloseDialog = saveData();
                if(wantToCloseDialog) {
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), R.string.wrong_value, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean saveData() {
        boolean isSave = false;
        String name = mName.getText().toString();
        if(name.equals("")) {
            mName.setError(getResources().getText(R.string.error_value));
        }
        if (mName.getError() == null && mAmount.getError() == null && mPrice.getError() == null) {
            double amount = 0.0;
            double price = 0.0;
            if (mAmount.getText().length() != 0) {
                amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
            }
            Cursor c = (Cursor) mUnits.getSelectedItem();
            long idUnit = c.getLong(c.getColumnIndex(UnitsTable.COLUMN_ID));
            if (mPrice.getText().length() != 0) {
                price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
            }
            Boolean isBought = mIsBought.isChecked();

            ItemDataSource itemDS = new ItemDataSource(getActivity());
            long idItem = (int) itemDS.add(name, amount, idUnit, price);
            long idList = getArguments().getLong(ID_LIST);

            ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
            long id = itemInListDS.add(idItem, idList, isBought);
            sendResult(Activity.RESULT_OK, id);
            isSave = true;
        }
        return isSave;
    }

    private void sendResult(int resultCode, long id) {
        if (getTargetFragment() == null)
            return;

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, new Intent().putExtra(ID_ITEM, id));
    }
}
