package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.math.BigDecimal;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;


public class ShoppingListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ADD_DIALOG_DATE = "addItemDialog";
    private static final String EDIT_DIALOG_DATE = "editItemDialog";
    private static final int ADD_DIALOG_CODE = 0;
    private static final int EDIT_DIALOG_CODE = 1;

    private static final int DATA_LOADER = 0;

    private Cursor mItemsInList;
    private ItemAdapter mAdapter;

    private TextView mSpentMoney, mTotalMoney, mEmptyText;
    private LinearLayout mListContainer;

    //edit later
    long idList = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(DATA_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        final TextView listName = (TextView) v.findViewById(R.id.list_name);
        Cursor listCursor = new ListsDataSource(getActivity()).get(idList);
        listName.setText(listCursor.getString(listCursor.getColumnIndex(ListsTable.COLUMN_NAME)));

        EditText newItem = (EditText) v.findViewById(R.id.new_item);
        newItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    AddItemDialogFragment addItemDialog = AddItemDialogFragment.newInstance(idList);
                    addItemDialog.setTargetFragment(ShoppingListFragment.this, ADD_DIALOG_CODE);
                    addItemDialog.show(getFragmentManager(), ADD_DIALOG_DATE);
                }
                return false;
            }
        });

        mSpentMoney = (TextView) v.findViewById(R.id.spent_money);
        mTotalMoney = (TextView) v.findViewById(R.id.total_money);

        mEmptyText = (TextView) v.findViewById(R.id.empty_list);
        mListContainer = (LinearLayout) v.findViewById(R.id.list_container);

        ListView list = (ListView) v.findViewById(android.R.id.list);
        registerForContextMenu(list);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mItemsInList.moveToPosition(position);

        String name = mItemsInList.getString(mItemsInList.getColumnIndex(ItemsTable.COLUMN_NAME));
        double amount = mItemsInList.getDouble(mItemsInList.getColumnIndex(ItemsTable.COLUMN_AMOUNT));
        String nameUnit = mItemsInList.getString(mItemsInList.getColumnIndex(UnitsTable.COLUMN_NAME));
        double price = mItemsInList.getDouble(mItemsInList.getColumnIndex(ItemsTable.COLUMN_PRICE));
        boolean isBought = mItemsInList.getInt(mItemsInList.getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) != 0;
        long idItem = mItemsInList.getLong(mItemsInList.getColumnIndex(ItemsTable.COLUMN_ID));

        EditItemDialogFragment editItemDialog = EditItemDialogFragment.newInstance(name, amount, price, isBought, nameUnit, idItem, idList);
        editItemDialog.setTargetFragment(ShoppingListFragment.this, EDIT_DIALOG_CODE);
        editItemDialog.show(getFragmentManager(), EDIT_DIALOG_DATE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_item_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                mItemsInList.moveToPosition(info.position);
                ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
                itemInListDS.delete(mItemsInList.getLong(mItemsInList.getColumnIndex(ItemsTable.COLUMN_ID)), idList);
                updateData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch(requestCode) {
            case ADD_DIALOG_CODE:
                updateData();
                break;
            case EDIT_DIALOG_CODE:
                updateData();
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;
        switch (id) {
            case DATA_LOADER:
                loader = new MyCursorLoader(getActivity(), idList);
                break;
            default:
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DATA_LOADER:
                mItemsInList = data;
                if(mItemsInList != null) {
                    mSpentMoney.setText(String.valueOf(sumSpentMoney()));
                    mTotalMoney.setText(String.valueOf(sumTotalMoney()));
                    mAdapter = new ItemAdapter(R.layout._shopping_list_item, mItemsInList);
                    setListAdapter(mAdapter);
                    mListContainer.setVisibility(View.VISIBLE);
                    mEmptyText.setVisibility(View.GONE);
                } else {
                    mListContainer.setVisibility(View.GONE);
                    mEmptyText.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case DATA_LOADER:
                mAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

    private void updateData() {
        getLoaderManager().getLoader(DATA_LOADER).forceLoad();
    }

    private double sumSpentMoney() {
        double sum = 0;
        mItemsInList.moveToFirst();
        do {
            if(mItemsInList.getInt(mItemsInList.getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) != 0) {
                sum += sum();
            }
        } while(mItemsInList.moveToNext());
        return sum;
    }

    private double sumTotalMoney() {
        double sum = 0;
        mItemsInList.moveToFirst();
        do {
            sum += sum();
        } while(mItemsInList.moveToNext());
        return sum;
    }

    private double sum() {
        double price = mItemsInList.getDouble(mItemsInList.getColumnIndex(ItemsTable.COLUMN_PRICE));
        double amount = mItemsInList.getDouble(mItemsInList.getColumnIndex(ItemsTable.COLUMN_AMOUNT));
        double sum = price * (amount == 0 ? 1 : amount);
        return new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private class ItemAdapter extends ResourceCursorAdapter {

        public ItemAdapter(int layout, Cursor c) {
            super(getActivity(), layout, c, 0);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {

            final long idItem =  cursor.getLong(cursor.getColumnIndex(ItemsTable.COLUMN_ID));

            CheckBox isBuy = (CheckBox) view.findViewById(R.id.is_item_bought);
            isBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
                    itemInListDS.setIsBought(cb.isChecked(), idItem, idList);
                    updateData();
                }
            });
            isBuy.setChecked(cursor.getInt(cursor.getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) != 0);
            TextView name = (TextView) view.findViewById(R.id.item_name);
            name.setText(cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_NAME)));
            TextView amount = (TextView) view.findViewById(R.id.item_amount);
            if (cursor.getDouble(cursor.getColumnIndex(ItemsTable.COLUMN_AMOUNT)) == 0) {
                amount.setText("-");
            } else {
                amount.setText(cursor.getDouble(cursor.getColumnIndex(ItemsTable.COLUMN_AMOUNT))
                        + " " + cursor.getString(cursor.getColumnIndex(UnitsTable.COLUMN_NAME))); //problem
            }
            TextView price = (TextView) view.findViewById(R.id.item_price);
            price.setText(cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_PRICE)));
        }
    }

    private static class MyCursorLoader extends CursorLoader {
        private Context mContext;
        private long mIdList;

        public MyCursorLoader(Context context, long idList) {
            super(context);
            mContext = context;
            mIdList = idList;
        }

        @Override
        public Cursor loadInBackground() {
            ListsDataSource mListDS = new ListsDataSource(mContext);
            return mListDS.getItemsInList(mIdList);
        }
    }
}
