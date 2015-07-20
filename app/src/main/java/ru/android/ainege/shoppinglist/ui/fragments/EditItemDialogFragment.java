package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;

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
                        saveData();
                        sendResult(Activity.RESULT_OK);
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

        mName = (EditText) v.findViewById(R.id.new_item_name);
        mName.setText(getArguments().getString(ITEM_NAME));
        mAmount = (EditText) v.findViewById(R.id.new_amount_item);
        if(getArguments().getDouble(AMOUNT) != 0) {
            mAmount.setText(String.valueOf(getArguments().getDouble(AMOUNT)));
        }
        mPrice = (EditText) v.findViewById(R.id.new_item_price);
        mPrice.setText(String.valueOf(getArguments().getDouble(PRICE)));
        mIsBought = (CheckBox) v.findViewById(R.id.is_bought);
        mIsBought.setChecked(getArguments().getBoolean(IS_BOUGHT));

        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        mUnits.setAdapter(adapter);
        if (getArguments().getDouble(AMOUNT) == 0) {
            mUnits.setSelection(0);
        } else {
            mUnits.setSelection(getPosition(mUnits, getArguments().getString(UNIT)));
        }
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

    private void saveData() {
        String name = mName.getText().toString();
        Double amount = 0.0;
        long idUnit = 0;
        if(mAmount.getText().length() > 0) {
            amount = Double.parseDouble(mAmount.getText().toString());
            Cursor c = (Cursor) mUnits.getSelectedItem();
            idUnit = c.getLong(c.getColumnIndex(UnitsTable.COLUMN_ID));
        }
        double price = Double.parseDouble(mPrice.getText().toString());
        boolean isBought = mIsBought.isChecked();

        ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
        itemInListDS.setIsBought(isBought, getArguments().getLong(ID_ITEM), getArguments().getLong(ID_LIST));
        ItemDataSource itemDS = new ItemDataSource(getActivity());
        itemDS.update(name, amount, idUnit, price, getArguments().getLong(ID_ITEM));
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
    }
}