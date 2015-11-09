package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.Validation;

public class EditItemDialogFragment extends Fragment implements SettingsDataItem {
    public static final String ITEM_IN_LIST = "itemInList";
    public static final String DEFAULT_SAVE_DATA = "dataSave";

    private boolean mIsAlwaysSave = false;

    private TextInputLayout mNameInputLayout;
    private AutoCompleteTextView mName;
    private TextView mInfo;
    private TextInputLayout mAmountInputLayout;
    private EditText mAmount;
    private Spinner mUnits;
    private TextInputLayout mPriceInputLayout;
    private EditText mPrice;
    private TextView mCurrency;
    private TextView mFinishPrice;
    private EditText mComment;
    private ToggleButton mIsBought;

    private String mCurrencyList;

    private ShoppingList mItemInList;

    private boolean mIsSelected = false;

    private TextWatcher mNameChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                mNameInputLayout.setErrorEnabled(true);
                mNameInputLayout.setError(getString(R.string.error_name));
            } else {
                if (mNameInputLayout.getError() != null) {
                    mNameInputLayout.setError(null);
                    mNameInputLayout.setErrorEnabled(false);
                }
                //Check is the item in the list or catalog of items. If there is a warning display
                ShoppingListCursor cursor = ShoppingListDataSource.getInstance(getActivity()).
                        existItemInList(s.toString().trim(), mItemInList.getIdList());
                if (cursor.moveToFirst() && !cursor.getItem().getItem().getName().equals(mItemInList.getItem().getName())) {
                    mInfo.setText(R.string.info_exit_item);
                    mInfo.setVisibility(View.VISIBLE);
                } else {
                    mInfo.setVisibility(View.GONE);
                }
                if (mIsSelected) {
                    ItemDataSource.ItemCursor cursorItem = new ItemDataSource(getActivity()).getByName(s.toString().trim());
                    if (cursorItem.moveToFirst() && !cursorItem.getItem().getName().equals(mItemInList.getItem().getName())) {
                        mInfo.setText(R.string.info_exit_item);
                        mInfo.setVisibility(View.VISIBLE);
                    } else {
                        mInfo.setVisibility(View.GONE);
                    }
                }
            }
        }
    };
    private TextWatcher mAmountChangedListener = new TextWatcher() {
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
                    mAmountInputLayout.setError(getString(R.string.error_value));
                } else {
                    if (mAmountInputLayout.getError() != null) {
                        mAmountInputLayout.setError(null);
                        mAmountInputLayout.setErrorEnabled(false);
                    }
                    if (mPrice.getText().length() > 0) {
                        setFinishPrice();
                    }
                }
            } else {
                mFinishPrice.setVisibility(View.GONE);
            }
        }
    };
    private TextWatcher mPriceChangedListener = new TextWatcher() {
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
                    mPriceInputLayout.setErrorEnabled(true);
                    mPriceInputLayout.setError(getString(R.string.error_value));
                } else {
                    if (mPriceInputLayout.getError() != null) {
                        mPriceInputLayout.setError(null);
                        mPriceInputLayout.setErrorEnabled(false);
                    }
                    if (mAmount.getText().length() > 0) {
                        setFinishPrice();
                    }
                }
            } else {
                mFinishPrice.setVisibility(View.GONE);
            }
        }
    };

    public static EditItemDialogFragment newInstance(ShoppingList itemInList, String dataSave) {
        Bundle args = new Bundle();
        args.putSerializable(ITEM_IN_LIST, itemInList);
        args.putString(DEFAULT_SAVE_DATA, dataSave);

        EditItemDialogFragment fragment = new EditItemDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mItemInList = (ShoppingList) getArguments().getSerializable(ITEM_IN_LIST);
        mCurrencyList = "руб"; //TODO get from db

        if (ALWAYS_SAVE_DATA.equals(getArguments().getString(DEFAULT_SAVE_DATA))) {
            mIsAlwaysSave = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_item, container, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        ImageView appBarImage = (ImageView) v.findViewById(R.id.appbar_image);
        appBarImage.setImageResource(R.drawable.list); //TODO get image from db

        setData(v);
        setDataToView();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.item_menu, menu);
        if (SAVE_DATA_BUTTON.equals(getArguments().getString(DEFAULT_SAVE_DATA))) {
            menu.findItem(R.id.update_item).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.update_item:
                if (saveData(true)) {
                    Toast.makeText(getActivity(), R.string.data_is_save, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.save_item:
                Boolean wantToCloseDialog = saveData(false);
                if (wantToCloseDialog) {
                    getActivity().onBackPressed();
                } else {
                    Toast.makeText(getActivity(), R.string.wrong_value, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.take_photo:
                Toast.makeText(getActivity(), "new photo", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.select_from_gallery:
                Toast.makeText(getActivity(), "image from gallery", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.reset_image:
                Toast.makeText(getActivity(), "random image", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setData(View v) {
        mInfo = (TextView) v.findViewById(R.id.info);

        mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
        mName = (AutoCompleteTextView) v.findViewById(R.id.new_item_name);
        mName.addTextChangedListener(mNameChangedListener);
        mName.setAdapter(getCompleteTextAdapter());

        mFinishPrice = (TextView) v.findViewById(R.id.finish_price);

        mAmountInputLayout = (TextInputLayout) v.findViewById(R.id.amount_input_layout);
        mAmount = (EditText) v.findViewById(R.id.new_amount_item);
        mAmount.addTextChangedListener(mAmountChangedListener);

        mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
        mUnits.setAdapter(getSpinnerAdapter());
        mUnits.setSelection(0);

        mPriceInputLayout = (TextInputLayout) v.findViewById(R.id.price_input_layout);
        mPrice = (EditText) v.findViewById(R.id.new_item_price);
        mPrice.addTextChangedListener(mPriceChangedListener);

        mCurrency = (TextView) v.findViewById(R.id.currency);
        mCurrency.setText(mCurrencyList);

        mComment = (EditText) v.findViewById(R.id.comment);

        mIsBought = (ToggleButton) v.findViewById(R.id.is_bought);
    }

    private void setDataToView() {
        mName.setText(mItemInList.getItem().getName());
        mName.setSelection(mItemInList.getItem().getName().length());

        if(mItemInList.getAmount() != 0) {
            mAmount.setText(new DecimalFormat("#.######").format(mItemInList.getAmount()));
            mUnits.setSelection(getPosition(mUnits, mItemInList.getUnit().getName()));
        } else {
            mUnits.setSelection(0);
        }

        if(mItemInList.getPrice() != 0) {
            mPrice.setText(String.format("%.2f", mItemInList.getPrice()));
        }

        mComment.setText(mItemInList.getComment());

        mIsBought.setChecked(mItemInList.isBought());
    }

    private SimpleCursorAdapter getCompleteTextAdapter() {
        SimpleCursorAdapter completeTextAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                null,
                new String[]{ItemsTable.COLUMN_NAME},
                new int[] {android.R.id.text1}, 0);
        completeTextAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                Cursor managedCursor = null;
                if (!charSequence.equals(mItemInList.getItem().getName())) {
                    managedCursor = new ItemDataSource(getActivity()).getNames(charSequence.toString().trim());
                    if (managedCursor.moveToFirst()) {
                        mIsSelected = true;
                    }
                }
                return managedCursor;
            }
        });
        completeTextAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                return ((ItemDataSource.ItemCursor) cursor).getItem().getName();
            }
        });
        return completeTextAdapter;
    }

    private SimpleCursorAdapter getSpinnerAdapter(){
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                new UnitsDataSource(getActivity()).getAll(),
                new String[] {UnitsTable.COLUMN_NAME},
                new int[] {android.R.id.text1}, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return spinnerAdapter;
    }

    private void setFinishPrice() {
        double amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
        double price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
        String finalPriceText = getString(R.string.finish_price) + localValue(amount * price) + " " + mCurrencyList;
        mFinishPrice.setText(finalPriceText);
        mFinishPrice.setVisibility(View.VISIBLE);
    }

    private String localValue(double value) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }

    private boolean saveData(boolean isUpdateData) {
        boolean isSave = false;
        if (mInfo.getVisibility() == View.GONE && mName.getError() == null &&
                mAmount.getError() == null &&  mPrice.getError() == null) {
            String name = mName.getText().toString().trim();
            double amount = 0.0;
            if (mAmount.getText().length() > 0) {
                amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
            }
            long idUnit = ((UnitsDataSource.UnitCursor) mUnits.getSelectedItem()).getUnit().getId();
            double price = 0.0;
            if(mPrice.getText().length() > 0) {
                price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
            }
            String comment = mComment.getText().toString();
            Boolean isBought = mIsBought.isChecked();

            ItemDataSource itemDS = new ItemDataSource(getActivity());

            if (isUpdateData) { //Updating in the catalog if the item is selected or create a new
                itemDS.update(new Item(mItemInList.getIdItem(), name, amount, idUnit, price, comment));
            } else {  //Don`t save default data
                if (mIsAlwaysSave) {  //Always save default data
                    itemDS.update(new Item(mItemInList.getIdItem(), name, amount, idUnit, price, comment));
                } else {
                    itemDS.update(new Item (mItemInList.getIdItem(), name));
                }

                //Update item in list
                ShoppingListDataSource itemInListDS = ShoppingListDataSource.getInstance(getActivity());
                itemInListDS.update(new ShoppingList(mItemInList.getIdItem(), mItemInList.getIdList(), isBought, amount, idUnit, price, comment, new Date(System.currentTimeMillis() / 1000L)));
                sendResult(Activity.RESULT_OK);
            }
            isSave = true;
        }
        return isSave;
    }

    private int getPosition(Spinner spinner, String name) {
        int index = 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(name)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
    }
}
