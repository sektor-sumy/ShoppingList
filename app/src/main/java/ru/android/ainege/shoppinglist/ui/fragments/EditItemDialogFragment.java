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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.Validation;

public class EditItemDialogFragment extends DialogFragment implements SettingsDataItem {
    public static final String ITEM_NAME = "itemName";
    public static final String AMOUNT = "amount";
    public static final String PRICE = "price";
    public static final String IS_BOUGHT = "isBought";
    public static final String UNIT = "nameUnit";
    public static final String ID_LIST = "idList";
    public static final String ID_ITEM = "idItem";
    public static final String DATA_SAVE = "dataSave";

    private AutoCompleteTextView mName;
    private EditText mAmount;
    private EditText mPrice;
    private CheckBox mIsBought;
    private Spinner mUnits;
    private TextView mInfo;
    private TextView mFinishPrice;

    private long mIdSelectedItem = -1;
    private SimpleCursorAdapter completeTextAdapter;

    private boolean mIsAlwaysSave = false;

    public static EditItemDialogFragment newInstance(String name, double amount, double price, boolean isBought, String nameUnit, long idItem, long idList, String dataSave) {
        Bundle args = new Bundle();
        args.putString(ITEM_NAME, name);
        args.putDouble(AMOUNT, amount);
        args.putDouble(PRICE, price);
        args.putBoolean(IS_BOUGHT, isBought);
        args.putString(UNIT, nameUnit);
        args.putLong(ID_ITEM, idItem);
        args.putLong(ID_LIST, idList);
        args.putString(DATA_SAVE, dataSave);

        EditItemDialogFragment fragment = new EditItemDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (DATA_SAVE_ALWAYS.equals(getArguments().getString(DATA_SAVE))) {
            mIsAlwaysSave = true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_item, null);
        builder.setView(v)
                .setCancelable(false)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        if (DATA_SAVE_BUTTON.equals(getArguments().getString(DATA_SAVE))) {
            builder.setNeutralButton(R.string.update, null);
        }
        setData(v);
        return builder.create();
    }

    private void setData(View v) {
        mIsBought = (CheckBox) v.findViewById(R.id.is_bought);
        mIsBought.setChecked(getArguments().getBoolean(IS_BOUGHT));

        mName = (AutoCompleteTextView) v.findViewById(R.id.new_item_name);
        mName.setText(getArguments().getString(ITEM_NAME));
        mName.setSelection(mName.getText().length());
        mInfo = (TextView) v.findViewById(R.id.info);
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
                    if (mIdSelectedItem != -1) {
                        if (getArguments().getDouble(PRICE) != 0) {
                            mPrice.setText(String.format("%.2f", getArguments().getDouble(PRICE)));
                        }
                        mIdSelectedItem = -1;
                    }
                    Cursor cursor = new ShoppingListDataSource(getActivity()).existItemInList(s.toString(), getArguments().getLong(ID_LIST));
                    if (cursor != null &&
                            !cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_NAME)).equals(getArguments().getString(ITEM_NAME))) {
                        mInfo.setText(R.string.info_exit_item_in_list_dont_save);
                        mInfo.setVisibility(View.VISIBLE);
                    } else {
                        mInfo.setVisibility(View.GONE);
                    }
                    if (mName.getError() != null) {
                        mName.setError(null);
                    }
                }
            }
        });
        mName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mIdSelectedItem = l;
                if (!DATA_NOT_DEFAULT.equals(getArguments().getString(DATA_SAVE))) {
                    Cursor c = new ItemDataSource(getActivity()).getItem(mIdSelectedItem);
                    double price = c.getDouble(c.getColumnIndex(ItemsTable.COLUMN_PRICE));
                    if (price > 0) {
                        mPrice.setText(String.format("%.2f", price));
                    }
                }
            }
        });
        completeTextAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                null,
                new String[]{ItemsTable.COLUMN_NAME},
                new int[] {android.R.id.text1}, 0);
        completeTextAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_NAME));
            }
        });
        completeTextAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                Cursor managedCursor = new ItemDataSource(getActivity()).getNames((charSequence != null ? charSequence.toString() : null));
                return managedCursor;
            }
        });
        mName.setAdapter(completeTextAdapter);

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
                if (s != null && s.length() > 0) {
                    if(!Validation.isValid(mAmount.getText().toString().trim(), false)) {
                        mAmount.setError(getResources().getText(R.string.error_value));
                    } else {
                        if(mAmount.getError() != null) { mAmount.setError(null); }
                        if (mPrice.getText().length() > 0) {
                            setFinishPrice();
                        }
                    }
                } else {
                    mFinishPrice.setVisibility(View.GONE);
                }
            }
        });

        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                new UnitsDataSource(getActivity()).getAll(),
                new String[] {UnitsTable.COLUMN_NAME},
                new int[] {android.R.id.text1}, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUnits.setAdapter(spinnerAdapter);
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
                if (s != null && s.length() > 0) {
                    if(!Validation.isValid(mPrice.getText().toString().trim(), true)) {
                        mPrice.setError(getResources().getText(R.string.error_value));
                    } else {
                        if (mPrice.getError() != null) { mPrice.setError(null); }
                        if (mAmount.getText().length() > 0) {
                            setFinishPrice();
                        }
                    }
                } else {
                    mFinishPrice.setVisibility(View.GONE);
                }
            }
        });

        mFinishPrice = (TextView) v.findViewById(R.id.finishPrice);
        if (mAmount.getText().length() > 0 && mPrice.getText().length() > 0) {
            setFinishPrice();
        }
    }

    private void setFinishPrice() {
        double amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
        double price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
        mFinishPrice.setText(localValue(amount * price));
        mFinishPrice.setVisibility(View.VISIBLE);
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
                Boolean wantToCloseDialog = saveData(false);
                if(wantToCloseDialog) {
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), R.string.wrong_value, Toast.LENGTH_LONG).show();
                }
            }
        });
        Button neutralButton = dialog.getButton(Dialog.BUTTON_NEUTRAL);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveData(true)) {
                    Toast.makeText(getActivity(), R.string.data_is_save, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean saveData(boolean isUpdateData) {
        boolean isSave = false;
        if(mName.getError() == null && mAmount.getError() == null &&  mPrice.getError() == null) {
            String name = mName.getText().toString().trim();
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

            ItemDataSource itemDS = new ItemDataSource(getActivity());
            long idItem = getArguments().getLong(ID_ITEM);
            long idItemNew = idItem;
            if (mIdSelectedItem != -1) {
                idItemNew = mIdSelectedItem;
            }
            if(isUpdateData) { //сохранение если выбрана настройка "кнопка" и нажата кнопка
                itemDS.update(name, amount, idUnit, price, idItemNew);
            } else {
                if (mIsAlwaysSave) { //сохранение если выбрана настройка "сохранять всегда"
                    itemDS.update(name, amount, idUnit, price, idItemNew);
                } else { //сохранение если выбрана настройка "не сохранять"
                    itemDS.updateName(name, idItemNew);
                }
                ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
                if (mInfo.getVisibility() == View.GONE) {
                    itemInListDS.update(isBought, amount, idUnit, price, idItemNew, idItem, getArguments().getLong(ID_LIST));
                }
                sendResult(Activity.RESULT_OK);
            }
            isSave = true;
        }
        return isSave;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
    }

    private String localValue(double value) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }
}
