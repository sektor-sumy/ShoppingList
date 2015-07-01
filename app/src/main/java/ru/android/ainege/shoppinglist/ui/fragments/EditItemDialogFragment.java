package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.ShoppingListEntity;
import ru.android.ainege.shoppinglist.db.entities.UnitEntity;

public class EditItemDialogFragment extends DialogFragment {
    ShoppingListEntity mItem;
    EditText mName;
    EditText mAmount;
    EditText mPrice;
    CheckBox mIsBought;
    Spinner mUnits;

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
        Bundle args = getArguments();
        mItem = (ShoppingListEntity) args.getSerializable(ShoppingListFragment.ITEM_IN_LIST);

        UnitsDataSource unitDS = new UnitsDataSource(getActivity());
        ArrayAdapter<UnitEntity> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, unitDS.getAll());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mName = (EditText) v.findViewById(R.id.new_item_name);
        mName.setText(mItem.getItem().getName());
        mAmount = (EditText) v.findViewById(R.id.new_amount_item);
        if(mItem.getItem().getAmount() != 0) {
            mAmount.setText(String.valueOf(mItem.getItem().getAmount()));
        }
        mPrice = (EditText) v.findViewById(R.id.new_item_price);
        mPrice.setText(String.valueOf(mItem.getItem().getPrice()));
        mIsBought = (CheckBox) v.findViewById(R.id.is_bought);
        mIsBought.setChecked(mItem.isBought());

        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        mUnits.setAdapter(adapter);
        if(mItem.getItem().getAmount() == 0) {
            mUnits.setSelection(0);
        } else {
            mUnits.setSelection(getPosition(mUnits, mItem.getItem().getUnit().getName()));
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
        mItem.getItem().setName(mName.getText().toString());
        if(mAmount.getText().length() > 0) {
            mItem.getItem().setAmount(Double.parseDouble(mAmount.getText().toString()));
            mItem.getItem().setUnit((UnitEntity) mUnits.getSelectedItem());
        } else {
            mItem.getItem().setAmount(0);
        }
        mItem.getItem().setPrice(Double.parseDouble(mPrice.getText().toString()));
        mItem.setBought(mIsBought.isChecked());

        ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
        itemInListDS.update(mItem, true);
    }
}
