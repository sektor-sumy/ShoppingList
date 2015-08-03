package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.Validation;

public class EditItemDialogFragment extends DialogFragment {
    public static final String ITEM_NAME = "itemName";
    public static final String AMOUNT = "amount";
    public static final String PRICE = "price";
    public static final String IS_BOUGHT = "isBought";
    public static final String UNIT = "nameUnit";
    public static final String ID_LIST = "idList";
    public static final String ID_ITEM = "idItem";

    private EditText mName;
    private EditText mAmount;
    private EditText mPrice;
    private CheckBox mIsBought;
    private Spinner mUnits;

    public static EditItemDialogFragment newInstance(String name, double amount, double price, boolean isBought, String nameUnit, long idItem, long idList) {
        Bundle args = new Bundle();
        args.putString(ITEM_NAME, name);
        args.putDouble(AMOUNT, amount);
        args.putDouble(PRICE, price);
        args.putBoolean(IS_BOUGHT, isBought);
        args.putString(UNIT, nameUnit);
        args.putLong(ID_ITEM, idItem);
        args.putLong(ID_LIST, idList);

        EditItemDialogFragment fragment = new EditItemDialogFragment();
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

        mIsBought = (CheckBox) v.findViewById(R.id.is_bought);
        mIsBought.setChecked(getArguments().getBoolean(IS_BOUGHT));

        mName = (EditText) v.findViewById(R.id.new_item_name);
        mName.setText(getArguments().getString(ITEM_NAME));
        mName.setSelection(mName.getText().length());
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mName.setError(getResources().getText(R.string.error_name));
                } else {
                    if (mName.getError() != null) {
                        mName.setError(null);
                    }
                }
            }
        });

        mAmount = (EditText) v.findViewById(R.id.new_amount_item);
        if(getArguments().getDouble(AMOUNT) != 0) {
            mAmount.setText(new DecimalFormat("#.######").format(getArguments().getDouble(AMOUNT)));
        }
        mAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s != null) {
                    if(!Validation.isValid(mAmount.getText().toString().trim(), false)) {
                        mAmount.setError(getResources().getText(R.string.error_value));
                    } else {
                        if(mAmount.getError() != null) { mAmount.setError(null); }
                    }
                }
            }
        });

        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        mUnits.setAdapter(adapter);
        if (getArguments().getDouble(AMOUNT) == 0) {
            mUnits.setSelection(0);
        } else {
            mUnits.setSelection(getPosition(mUnits, getArguments().getString(UNIT)));
        }

        mPrice = (EditText) v.findViewById(R.id.new_item_price);
        if(getArguments().getDouble(PRICE) != 0) {
           mPrice.setText(String.format("%.2f", getArguments().getDouble(PRICE)));
        }
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
                    if(!Validation.isValid(mPrice.getText().toString().trim(), true)) {
                        mPrice.setError(getResources().getText(R.string.error_value));
                    } else {
                        if(mPrice.getError() != null) { mPrice.setError(null); }
                    }
                }
            }
        });
    }

    private int getPosition(Spinner spinner, String name) {
        int index = 0;
        for(int i = 0; i < spinner.getCount(); i++) {
            if(spinner.getItemAtPosition(i).toString().equalsIgnoreCase(name)) {
                index = i;
                break;
            }
        }
        return index;
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
        if(mName.getError() == null && mAmount.getError() == null &&  mPrice.getError() == null) {
            String name = mName.getText().toString();
            double amount = 0.0;
            if (mAmount.getText().length() > 0) {
                amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
            }
            Cursor c = (Cursor) mUnits.getSelectedItem();
            long idUnit = c.getLong(c.getColumnIndex(UnitsTable.COLUMN_ID));
            double price = 0.0;
            if(mPrice.getText().length() > 0) {
                price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
            }
            boolean isBought = mIsBought.isChecked();

            ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
            itemInListDS.setIsBought(isBought, getArguments().getLong(ID_ITEM), getArguments().getLong(ID_LIST));
            ItemDataSource itemDS = new ItemDataSource(getActivity());
            itemDS.update(name, amount, idUnit, price, getArguments().getLong(ID_ITEM));
            sendResult(Activity.RESULT_OK);
            isSave = true;
        }
        return isSave;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
    }
}
