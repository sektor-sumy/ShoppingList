package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.Validation;

public class AddItemDialogFragment extends Fragment implements SettingsDataItem {
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

    private String mAddedAmount = "";
    private String mAddedPrice = "";
    private int mAddedUnit = 0;

    private long mIdSelectedItem = -1;
    private long mIdExistItem = -1;
    private SimpleCursorAdapter completeTextAdapter;

    private boolean mIsAlwaysSave = false;
    private boolean mIsNotDefault = false;

    public static Fragment newInstance(long id, String dataSave) {
        Bundle args = new Bundle();
        args.putLong(ID_LIST, id);
        args.putString(DATA_SAVE, dataSave);

        AddItemDialogFragment fragment = new AddItemDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DATA_SAVE_ALWAYS.equals(getArguments().getString(DATA_SAVE))) {
            mIsAlwaysSave = true;
        }
        if (!DATA_NOT_DEFAULT.equals(getArguments().getString(DATA_SAVE))) {
            mIsNotDefault = true;
        }
        if (DATA_SAVE_BUTTON.equals(getArguments().getString(DATA_SAVE))) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_item, container, false);
        setData(v);
        return v;
    }

    private void setData(View v) {
        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        mUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mIdSelectedItem == -1) {
                    mAddedUnit = mUnits.getSelectedItemPosition();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                new UnitsDataSource(getActivity()).getAll(),
                new String[] {UnitsTable.COLUMN_NAME},
                new int[] {android.R.id.text1}, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUnits.setAdapter(spinnerAdapter);
        mUnits.setSelection(0);

        mName = (AutoCompleteTextView) v.findViewById(R.id.new_item_name);
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
                    if (mIdSelectedItem != -1 && mIsNotDefault) {
                        mAmount.setText(mAddedAmount);
                        mUnits.setSelection(mAddedUnit);
                        mPrice.setText(mAddedPrice);
                        mIdSelectedItem = -1;
                    }
                    //Cursor cursor = new ShoppingListDataSource(getActivity()).existItemInList(s.toString(), getArguments().getLong(ID_LIST));
                    Cursor cursor = ShoppingListDataSource.getInstance(getActivity()).existItemInList(s.toString(), getArguments().getLong(ID_LIST));
                    if (cursor != null) {
                        mInfo.setText(R.string.info_exit_item_in_list);
                        mInfo.setVisibility(View.VISIBLE);
                        mIdExistItem = cursor.getLong(cursor.getColumnIndex(ItemsTable.COLUMN_ID));
                    } else {
                        mInfo.setVisibility(View.GONE);
                        mIdExistItem = -1;
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
                if (mIsNotDefault) {
                    Cursor c = new ItemDataSource(getActivity()).get(mIdSelectedItem);
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
                if (s != null && s.length() > 0) {
                    if (!Validation.isValid(mAmount.getText().toString().trim(), false)) {
                        mAmount.setError(getResources().getText(R.string.error_value));
                    } else {
                        if (mIdSelectedItem == -1) {
                            mAddedAmount = String.valueOf(s);
                        }
                        if (mAmount.getError() != null) {
                            mAmount.setError(null);
                        }
                        if (mPrice.getText().length() > 0) {
                            setFinishPrice();
                        }
                    }
                } else {
                    mFinishPrice.setVisibility(View.GONE);
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
                if (s != null && s.length() > 0) {
                    if (!Validation.isValid(mPrice.getText().toString().trim(), true)) {
                        mPrice.setError(getResources().getText(R.string.error_value));
                    } else {
                        if (mIdSelectedItem == -1) {
                            mAddedPrice = String.valueOf(s);
                        }
                        if (mPrice.getError() != null) {
                            mPrice.setError(null);
                        }
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

        mIsBought = (CheckBox) v.findViewById(R.id.is_bought);
    }

    private void setFinishPrice() {
        double amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
        double price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
        mFinishPrice.setText(localValue(amount * price));
        mFinishPrice.setVisibility(View.VISIBLE);
    }

/*    @Override
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
                Boolean wantToCloseDialog = saveData(false);
                if (wantToCloseDialog) {
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
    }*/

    private boolean saveData(boolean isUpdateData) {
        boolean isSave = false;
        String name = mName.getText().toString().trim();
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
            if(isUpdateData) { //сохранение если выбрана настройка "кнопка" и нажата кнопка
                if (mIdSelectedItem != -1) {
                    //itemDS.update(name, amount, idUnit, price, mIdSelectedItem);
                    itemDS.update(new Item(name, amount, idUnit, price));
                } else {
                    //mIdSelectedItem = (int) itemDS.add(name, amount, idUnit, price);
                    mIdSelectedItem = (int) itemDS.add(new Item(name, amount, idUnit, price));
                }
            } else {
                long idItem;
                if (mIsAlwaysSave) { //сохранение если выбрана настройка "сохранять всегда"
                    if (mIdSelectedItem != -1) {
                        idItem = mIdSelectedItem;
                        itemDS.update(new Item(name, amount, idUnit, price));
                    } else {
                        idItem = (int) itemDS.add(new Item(name, amount, idUnit, price));
                    }
                } else { //сохранение если выбрана настройка "не сохранять"
                    if (mIdSelectedItem != -1) {
                        idItem = mIdSelectedItem;
                    } else {
                        idItem = (int) itemDS.add(new Item(name));
                    }
                }
                long idList = getArguments().getLong(ID_LIST);

                ShoppingListDataSource itemInListDS = ShoppingListDataSource.getInstance(getActivity());
                long id = mIdExistItem;
                if (mIdExistItem != -1) {
                    itemInListDS.update(new ShoppingList(mIdExistItem, idList, isBought, amount, idUnit, price,  new Date()));
                } else {
                    id = itemInListDS.add(new ShoppingList(idItem, idList, isBought, amount, idUnit, price, new Date(System.currentTimeMillis() / 1000L)));
                }
                sendResult(Activity.RESULT_OK, id);
            }
            isSave = true;
        }
        return isSave;
    }

    private void sendResult(int resultCode, long id) {
        if (getTargetFragment() == null)
            return;

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, new Intent().putExtra(ID_ITEM, id));
    }

    private String localValue(double value) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }
}
