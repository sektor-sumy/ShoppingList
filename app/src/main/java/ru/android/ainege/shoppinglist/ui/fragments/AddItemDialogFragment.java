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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.Validation;

public class AddItemDialogFragment extends DialogFragment implements SettingsDataItem {
    public static final String ID_LIST = "idList";
    public static final String ID_ITEM = "idItem";
    public static final String DATA_SAVE = "dataSave";

    private AutoCompleteTextView mName;
    private EditText mAmount;
    private EditText mPrice;
    private CheckBox mIsBought;
    private Spinner mUnits;

    private long mIdSelectedItem = -1;
    private SimpleCursorAdapter completeTextAdapter;

    private boolean mIsAlwaysSave = false;

    public static AddItemDialogFragment newInstance(long id, String dataSave) {
        Bundle args = new Bundle();
        args.putLong(ID_LIST, id);
        args.putString(DATA_SAVE, dataSave);

        AddItemDialogFragment fragment = new AddItemDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments().getString(DATA_SAVE).equals(DATA_SAVE_ALWAYS)) {
            mIsAlwaysSave = true;
        }
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
        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                new UnitsDataSource(getActivity()).getAll(),
                new String[] {UnitsTable.COLUMN_NAME},
                new int[] {android.R.id.text1}, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUnits.setAdapter(spinnerAdapter);
        mUnits.setSelection(0);

        mName = (AutoCompleteTextView) v.findViewById(R.id.new_item_name);
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
                        mAmount.setText("");
                        mUnits.setSelection(0);
                        mPrice.setText("");
                        mIdSelectedItem = -1;
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
                Cursor c = new ItemDataSource(getActivity()).getItem(mIdSelectedItem);
                double amount = c.getDouble(c.getColumnIndex(ItemsTable.COLUMN_AMOUNT));
                if (amount > 0) {
                    mAmount.setText(new DecimalFormat("#.######").format(amount));
                    mUnits.setSelection(c.getInt(c.getColumnIndex(ItemsTable.COLUMN_ID_UNIT)));
                }
                double price = c.getDouble(c.getColumnIndex(ItemsTable.COLUMN_PRICE));
                if (price > 0) {
                    mPrice.setText(String.format("%.2f", price));
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
                mIdSelectedItem = -1;
                Cursor managedCursor = new ItemDataSource(getActivity()).getNames((charSequence != null ? charSequence.toString() : null));
                return managedCursor;
            }
        });
        mName.setAdapter(completeTextAdapter);

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
                if (s != null) {
                    if (!Validation.isValid(mPrice.getText().toString().trim(), true)) {
                        mPrice.setError(getResources().getText(R.string.error_value));
                    } else {
                        if (mPrice.getError() != null) {
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
            long idItem;
            if (mIsAlwaysSave) { //сохранение если выбрана настройка "сохранять всегда"
                if (mIdSelectedItem != -1) {
                    idItem = mIdSelectedItem;
                    itemDS.update(name, amount, idUnit, price, idItem);
                } else {
                    idItem = (int) itemDS.add(name, amount, idUnit, price);
                }
            } else { //сохранение если выбрана настройка "не сохранять"
                if (mIdSelectedItem != -1) {
                    idItem = mIdSelectedItem;
                } else {
                    idItem = (int) itemDS.add(name);
                }
            }
            long idList = getArguments().getLong(ID_LIST);

            ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
            long id = itemInListDS.add(idItem, idList, isBought, amount, idUnit, price);
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
