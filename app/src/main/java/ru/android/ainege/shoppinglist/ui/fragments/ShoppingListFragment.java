package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.ui.RecyclerItemClickListener;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.ShoppingListTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;


public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ID_LIST = "idList";
    public static final String IS_BOUGHT_FIRST = "isBoughtFirst";
    public static final String DATA_SAVE = "dataSave";

    private static final String ADD_DIALOG_DATE = "addItemDialog";
    private static final String EDIT_DIALOG_DATE = "editItemDialog";
    private static final int ADD_DIALOG_CODE = 0;
    private static final int EDIT_DIALOG_CODE = 1;

    private static final int DATA_LOADER = 0;

    private Cursor mItemsInList;
    private MyAdapter mAdapter;

    private TextView mSpentMoney, mTotalMoney, mEmptyText;
    private LinearLayout mListContainer;
    private RecyclerView mList;
    private long mSaveItemId = -1;
    private Parcelable mListState;
    private double mSaveSpentMoney;
    private boolean mIsBoughtEndInList;

    private long mIdList;

    private List<Integer> selectedItems = new ArrayList<>();
    private android.view.ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.cab_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.move:
                    return true;
                case R.id.select_bought:
                    return true;
                case R.id.select_not_bought:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            mActionMode = null;
        }
    };

    public static ShoppingListFragment newInstance(long id, boolean isBoughtFirst, String dataSave) {
        Bundle args = new Bundle();
        args.putLong(ID_LIST, id);
        args.putBoolean(IS_BOUGHT_FIRST, isBoughtFirst);
        args.putString(DATA_SAVE, dataSave);

        ShoppingListFragment fragment = new ShoppingListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIdList = getArguments().getLong(ID_LIST);
        mIsBoughtEndInList = getArguments().getBoolean(IS_BOUGHT_FIRST);
        getLoaderManager().initLoader(DATA_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        //old version TODO: add new item to list
       /* EditText newItem = (EditText) v.findViewById(R.id.new_item);
        newItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    AddItemDialogFragment addItemDialog = AddItemDialogFragment.newInstance(mIdList, getArguments().getString(DATA_SAVE));
                    addItemDialog.setTargetFragment(ShoppingListFragment.this, ADD_DIALOG_CODE);
                    addItemDialog.show(getFragmentManager(), ADD_DIALOG_DATE);
                }
                return false;
            }
        });*/

        mSpentMoney = (TextView) v.findViewById(R.id.spent_money);
        mTotalMoney = (TextView) v.findViewById(R.id.total_money);

        mEmptyText = (TextView) v.findViewById(R.id.empty_list);
        mListContainer = (LinearLayout) v.findViewById(R.id.list_container);

        mList = (RecyclerView) v.findViewById(R.id.items_list);
        mList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mList.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mList, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (mActionMode != null) {
                            mAdapter.toggleSelection(position);
                        }
                        else {
                            mItemsInList.moveToPosition(position);

                            String name = mItemsInList.getString(mItemsInList.getColumnIndex(ItemsTable.COLUMN_NAME));
                            double amount = mItemsInList.getDouble(mItemsInList.getColumnIndex(ItemsTable.COLUMN_AMOUNT));
                            String nameUnit = mItemsInList.getString(mItemsInList.getColumnIndex(UnitsTable.COLUMN_NAME));
                            double price = mItemsInList.getDouble(mItemsInList.getColumnIndex(ItemsTable.COLUMN_PRICE));
                            boolean isBought = mItemsInList.getInt(mItemsInList.getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) != 0;
                            long idItem = mItemsInList.getLong(mItemsInList.getColumnIndex(ItemsTable.COLUMN_ID));

                            EditItemDialogFragment editItemDialog = EditItemDialogFragment.newInstance(name, amount, price, isBought, nameUnit, idItem, mIdList, getArguments().getString(DATA_SAVE));
                            editItemDialog.setTargetFragment(ShoppingListFragment.this, EDIT_DIALOG_CODE);
                            editItemDialog.show(getFragmentManager(), EDIT_DIALOG_DATE);

                            mSaveItemId = idItem;
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        if (mActionMode != null) {
                            return;
                        }
                        mActionMode = getActivity().startActionMode(mActionModeCallback);
                        mAdapter.toggleSelection(position);
                    }

                    @Override
                    public void onSwipeRight(View view, int position) {
                        if (mActionMode != null) {
                            return;
                        }

                        MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) mList.findViewHolderForAdapterPosition(position);
                        boolean isBought;
                        if (holder.mIsBought.getVisibility() == View.VISIBLE) {
                            holder.mIsBought.setVisibility(View.GONE);
                            isBought = false;
                        } else {
                            holder.mIsBought.setVisibility(View.VISIBLE);
                            isBought = true;
                        }

                        mItemsInList.moveToPosition(position);
                        ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
                        itemInListDS.setIsBought(isBought, mItemsInList.getLong(mItemsInList.getColumnIndex(ItemsTable.COLUMN_ID)), mIdList);

                        //if set bought items in the end of list - refresh list
                        //if it isn`t - try to update only spent sum
                        if (mIsBoughtEndInList) {
                            //mListState = mList.onSaveInstanceState();
                            updateData();
                        } else {
                            double sum = sumOneItem(mItemsInList);
                            if (mSaveSpentMoney != 0) {
                                if (isBought) {
                                    mSaveSpentMoney += sum;
                                } else {
                                    mSaveSpentMoney -= sum;
                                }
                                updateSpentSum(mSaveSpentMoney);
                            } else {
                                //mListState = mList.onSaveInstanceState();
                                 updateData();
                            }
                        }
                    }
                })
        );

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                Toast.makeText(getActivity(), swipeDir + " swipe for delete", Toast.LENGTH_SHORT).show();
            }

        });
        itemTouchHelper.attachToRecyclerView(mList);

        return v;
    }

    //old version TODO delete item
    /*public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete:
                mItemsInList.moveToPosition(info.position);
                ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
                itemInListDS.delete(mItemsInList.getLong(mItemsInList.getColumnIndex(ItemsTable.COLUMN_ID)), mIdList);
                //mListState = mList.onSaveInstanceState(); //doesn't use in rw
                updateData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch(requestCode) {
            case ADD_DIALOG_CODE:
                mSaveItemId = data.getLongExtra(AddItemDialogFragment.ID_ITEM, -1);
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
                loader = new MyCursorLoader(getActivity(), mIdList);
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
                    updateSpentSum(sumSpentMoney());
                    mTotalMoney.setText(localValue(sumTotalMoney()));
                    mAdapter = new MyAdapter(mItemsInList);
                    mList.setAdapter(mAdapter);
                    //TODO: check it
                   /* if (mListState != null) {
                        //mList.onRestoreInstanceState(mListState); //doesn't use in rw
                        mListState = null;
                    } else if (mSaveItemId != -1) {
                        //mList.setSelection(getPosition(mSaveItemId));
                        mSaveItemId = -1;
                    }
                    mListContainer.setVisibility(View.VISIBLE);
                    mEmptyText.setVisibility(View.GONE);*/
                } else {
                    /*mListContainer.setVisibility(View.GONE);
                    mEmptyText.setVisibility(View.VISIBLE);*/
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
                if (mAdapter != null) {
                    //mAdapter.swapCursor(null);
                }
                break;
            default:
                break;
        }
    }

    private void updateData() {
        getLoaderManager().getLoader(DATA_LOADER).forceLoad();
    }

    private void updateSpentSum(double newSum) {
        mSpentMoney.setText(localValue(newSum));
    }

    private double sumSpentMoney() {
        double sum = 0;
        mItemsInList.moveToFirst();
        do {
            if(mItemsInList.getInt(mItemsInList.getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) != 0) {
                sum += sumOneItem(mItemsInList);
            }
        } while(mItemsInList.moveToNext());
        mSaveSpentMoney = sum;
        return mSaveSpentMoney;
    }

    private double sumTotalMoney() {
        double sum = 0;
        mItemsInList.moveToFirst();
        do {
            sum += sumOneItem(mItemsInList);
        } while(mItemsInList.moveToNext());
        return sum;
    }

    private double sumOneItem(Cursor c) {
        double price = c.getDouble(c.getColumnIndex(ItemsTable.COLUMN_PRICE));
        double amount = c.getDouble(c.getColumnIndex(ItemsTable.COLUMN_AMOUNT));
        double sum = price * (amount == 0 ? 1 : amount);
        return new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private static String localValue(double value) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Cursor mDataset;
        private List<Integer> selectedItems = new ArrayList<>();

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public View mView;
            public ImageView mImage;
            public TextView mTextView;
            public TextView mAmount;
            public TextView mPrice;
            public View mIsBought;

            public ViewHolder(View v) {
                super(v);
                mView = v;
                mImage = (ImageView) v.findViewById(R.id.item_image_list);
                mTextView = (TextView) v.findViewById(R.id.item_name_list);
                mAmount = (TextView) v.findViewById(R.id.item_amount_list);
                mPrice = (TextView) v.findViewById(R.id.item_price_list);
                mIsBought = v.findViewById(R.id.is_bought_list);
            }
        }

        public MyAdapter(Cursor myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._shopping_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mView.setSelected(selectedItems.contains(position));

            mDataset.moveToPosition(position);

            holder.mImage.setImageResource(R.drawable.food);
            holder.mTextView.setText(mDataset.getString(mDataset.getColumnIndex(ItemsTable.COLUMN_NAME)));
            if (mDataset.getDouble(mDataset.getColumnIndex(ItemsTable.COLUMN_AMOUNT)) == 0) {
                holder.mAmount.setText("-");
            } else {
                String amount = NumberFormat.getInstance().format(mDataset.getDouble(mDataset.getColumnIndex(ItemsTable.COLUMN_AMOUNT)))
                        + " " + mDataset.getString(mDataset.getColumnIndex(UnitsTable.COLUMN_NAME));
                holder.mAmount.setText(amount);
            }
            holder.mPrice.setText(localValue(mDataset.getDouble(mDataset.getColumnIndex(ItemsTable.COLUMN_PRICE))));
            int visibility = View.GONE;
            if (mDataset.getInt(mDataset.getColumnIndex(ShoppingListTable.COLUMN_IS_BOUGHT)) == 1){
                visibility = View.VISIBLE;
            }
            holder.mIsBought.setVisibility(visibility);
        }

        @Override
        public int getItemCount() {
            return mDataset.getCount();
        }

        public void toggleSelection(int pos) {
            if (selectedItems.contains(pos)) {
                for (int i = 0; i < selectedItems.size(); i++) {
                    if (selectedItems.get(i) == pos) {
                        selectedItems.remove(i);
                        break;
                    }
                }
            } else {
                selectedItems.add(pos);
            }
            notifyItemChanged(pos);
        }

        public void clearSelections() {
            selectedItems.clear();
            notifyDataSetChanged();
        }
    }

    private static class MyCursorLoader extends CursorLoader {
        private long mIdList;

        public MyCursorLoader(Context context, long idList) {
            super(context);
            mIdList = idList;
        }

        @Override
        public Cursor loadInBackground() {
            ListsDataSource mListDS = ListsDataSource.getInstance();
            return mListDS.getItemsInList(mIdList);
        }
    }

    ////doesn't use yet
    /*private int getPosition(long id) {
        int index = 0;
        for (int i = 0; i < mList.getCount(); i++) {
            if (mList.getItemIdAtPosition(i) == id) {
                index = i;
                break;
            }
        }
        return index;
    }*/
}
