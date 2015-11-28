package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Item;

public class AddItemFragment extends ItemFragment {
    public static final String ID_LIST = "idList";

    private boolean mIsUseDefaultData = false;

    private String mAddedAmount = "";
    private int mAddedUnit = 0;
    private String mAddedPrice = "";
    private String mAddedComment = "";

    private long mIdSelectedItem = -1;
    private boolean mIsSelectsdItem = false;

    public static AddItemFragment newInstance(long id, String dataSave) {
        Bundle args = new Bundle();
        args.putLong(ID_LIST, id);
        args.putString(DEFAULT_SAVE_DATA, dataSave);

        AddItemFragment fragment = new AddItemFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!NOT_USE_DEFAULT_DATA.equals(getArguments().getString(DEFAULT_SAVE_DATA))) {
            mIsUseDefaultData = true;
        }

        getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
    }

    @Override
    protected void setView(View v) {
        super.setView(v);
        mName.setOnItemClickListener(getOnNameClickListener());
        mCollapsingToolbarLayout.setTitle(getString(R.string.buy));
    }

    @Override
    protected TextWatcher getNameChangedListener() {
        return new TextWatcher() {
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
                    disableError(mNameInputLayout);
                    //If selected a existent item and default data are used,
                    //when changing item, fill in the data that have been previously introduced
                    if (mIsUseDefaultData && mIsSelectsdItem) {
                        mAmount.setText(mAddedAmount);
                        mUnits.setSelection(mAddedUnit);
                        mPrice.setText(mAddedPrice);
                        mComment.setText(mAddedComment);
                        mIdSelectedItem = -1;
                    }
                    //Check is the item in the list. If there is a warning display
                    //If it isn`t, check is it in the catalog of items. If there is select it
                    ShoppingListCursor cursor = ShoppingListDataSource.getInstance(getActivity()).
                            existItemInList(s.toString().trim(), getIdList());
                    if (cursor.moveToFirst()) {
                        mInfo.setText(R.string.info_exit_item_in_list);
                        mInfo.setVisibility(View.VISIBLE);
                        mIdSelectedItem = cursor.getEntity().getIdItem();
                    } else {
                        mInfo.setVisibility(View.GONE);
                        if (mIsProposedItem) {
                            ItemDataSource.ItemCursor cursorItem = new ItemDataSource(getActivity()).getByName(s.toString().trim());
                            if (cursorItem.moveToFirst()) {
                                mIdSelectedItem = cursorItem.getEntity().getId();
                            }
                        } else {
                            mIdSelectedItem = -1;
                        }
                    }
                }
            }
        };
    }

    @Override
    protected SimpleCursorAdapter getCompleteTextAdapter() {
        return super.getCompleteTextAdapter(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                Cursor managedCursor = new ItemDataSource(getActivity()).getNames(charSequence != null ? charSequence.toString().trim() : null);
                if (managedCursor.moveToFirst()) {
                    mIsProposedItem = true;
                }
                return managedCursor;
            }
        });
    }

    private AdapterView.OnItemClickListener getOnNameClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mIsSelectsdItem = true;
                mIdSelectedItem = l;
                //If default data are used, they fill in the fields
                if (mIsUseDefaultData) {
                    mAddedAmount = mAmount.getText().toString();
                    mAddedUnit = mUnits.getSelectedItemPosition();
                    mAddedPrice = mPrice.getText().toString();
                    mAddedComment = mComment.getText().toString();

                    ItemDataSource.ItemCursor c = new ItemDataSource(getActivity()).get(mIdSelectedItem);
                    c.moveToFirst();
                    Item item = c.getEntity();

                    double amount = item.getAmount();
                    if (amount > 0) {
                        mAmount.setText(new DecimalFormat("#.######").format(amount));
                        mUnits.setSelection((int) item.getIdUnit());
                    }
                    double price = item.getPrice();
                    if (price > 0) {
                        mPrice.setText(String.format("%.2f", price));
                    }
                    mComment.setText(item.getComment());
                }
            }
        };
    }

    @Override
    protected boolean saveData(boolean isUpdateData) {
        boolean isSave = false;

        String name = getName();
        if (name.length() == 0) {
            mNameInputLayout.setError(getString(R.string.error_value));
        }

        if (!mNameInputLayout.isErrorEnabled() && !mAmountInputLayout.isErrorEnabled() &&
                !mPriceInputLayout.isErrorEnabled() ) {
            Item item = getItem();
            ItemDataSource itemDS = new ItemDataSource(getActivity());

            if (isUpdateData) { //Updating in the catalog if the item is selected or create a new
                updateItem(item);
            } else {
                long idItem;
                if (mIsAlwaysSave) { //Always save default data
                    updateItem(item);
                    idItem = mIdSelectedItem;
                } else { //Don`t save default data
                    if (mIdSelectedItem != -1) {
                        idItem = mIdSelectedItem;
                    } else {
                        idItem = (int) itemDS.add(new Item(name));
                    }
                }
                item.setId(idItem);

                //Save item to list
                ShoppingListDataSource itemInListDS = ShoppingListDataSource.getInstance(getActivity());
                if (mInfo.getVisibility() == View.VISIBLE) { //If item in list, update it
                    itemInListDS.update(getItemInList(item));
                } else { //Add new item to list
                    idItem = itemInListDS.add(getItemInList(item));
                }
                sendResult(Activity.RESULT_OK, idItem);
            }
            isSave = true;
        }
        return isSave;
    }

    @Override
    protected long getIdList(){
        return getArguments().getLong(ID_LIST);
    }

    private void updateItem(Item item) {
        ItemDataSource itemDS = new ItemDataSource(getActivity());
        if (mIdSelectedItem != -1) {
            item.setId(mIdSelectedItem);
            itemDS.update(item);
        } else {
            mIdSelectedItem = (int) itemDS.add(item);
        }
    }
}
