package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.ItemEntity;
import ru.android.ainege.shoppinglist.db.entities.ShoppingListEntity;
import ru.android.ainege.shoppinglist.db.entities.UnitEntity;

public class AddItemDialogFragment extends DialogFragment {
    public static final String ID_LIST = "idList";

    private EditText mName;
    private EditText mAmount;
    private EditText mPrice;
    private CheckBox mIsBought;
    private Spinner mUnits;

    public static AddItemDialogFragment newInstance(int id) {
        Bundle args = new Bundle();
        args.putInt(ID_LIST, id);

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
        ArrayAdapter<UnitEntity> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, unitDS.getAll());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        mUnits.setAdapter(adapter);
        mUnits.setSelection(0);

        mName = (EditText) v.findViewById(R.id.new_item_name);
        mAmount = (EditText) v.findViewById(R.id.new_amount_item);
        mPrice = (EditText) v.findViewById(R.id.new_item_price);
        mIsBought = (CheckBox) v.findViewById(R.id.is_bought);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }

        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP);
        window.getAttributes().y = 30;
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        int dw = (int) (displaymetrics.widthPixels);
        window.setLayout(dw, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void saveData() {
        String name = mName.getText().toString();
        Double amount = 0.0;
        Double price = 0.0;
        UnitEntity unit = null;
        if(mAmount.getText().length() !=0) {
            amount = Double.parseDouble(mAmount.getText().toString());
            unit = (UnitEntity) mUnits.getSelectedItem();
        }
        if(mPrice.getText().length() != 0) {
            price = Double.parseDouble(mPrice.getText().toString());
        }
        Boolean isBought = mIsBought.isChecked();

        ItemEntity item = new ItemEntity(name, amount, unit, price);
        ItemDataSource itemDS = new ItemDataSource(getActivity());

        int idItem = (int) itemDS.add(item);

        int idList = getArguments().getInt(ID_LIST);
        ShoppingListEntity itemInList = new ShoppingListEntity(idItem, idList, isBought);
        ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
        itemInListDS.add(itemInList);
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
    }
}
