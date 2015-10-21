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
import java.util.Collections;
import java.util.List;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.RecyclerItemClickListener;


public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ID_LIST = "idList";
    public static final String IS_BOUGHT_END_IN_LIST = "isBoughtEndInList";
    public static final String DATA_SAVE = "dataSave";

    private static final int DATA_LOADER = 0;

    private static final String ADD_DIALOG_DATE = "addItemDialog";
    private static final String EDIT_DIALOG_DATE = "editItemDialog";
    private static final int ADD_DIALOG_CODE = 0;
    private static final int EDIT_DIALOG_CODE = 1;

    private long mIdList;
    private RecyclerView mItemsListRV;
    private RecyclerViewAdapter mAdapterRV;
    private ArrayList<ShoppingList> mItemsInList;

    private TextView mSpentMoney, mTotalMoney, mEmptyText;
    private double mSaveSpentMoney;
    private LinearLayout mListContainer;
    private long mSaveItemId = -1;
    private Parcelable mListState;
    private boolean mIsBoughtEndInList;


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
                    deleteSelectedItems();
                    return true;
                case R.id.move:
                    return true;
                case R.id.select_bought:
                    mAdapterRV.selectItems(true);
                    return true;
                case R.id.select_not_bought:
                    mAdapterRV.selectItems(false);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapterRV.clearSelections();
            mActionMode = null;
        }
    };

    public static ShoppingListFragment newInstance(long id, boolean isBoughtEndInList, String dataSave) {
        Bundle args = new Bundle();
        args.putLong(ID_LIST, id);
        args.putBoolean(IS_BOUGHT_END_IN_LIST, isBoughtEndInList);
        args.putString(DATA_SAVE, dataSave);

        ShoppingListFragment fragment = new ShoppingListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIdList = getArguments().getLong(ID_LIST);
        mIsBoughtEndInList = getArguments().getBoolean(IS_BOUGHT_END_IN_LIST);
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

        mItemsListRV = (RecyclerView) v.findViewById(R.id.items_list);
        mItemsListRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapterRV = new RecyclerViewAdapter();
        mItemsListRV.setAdapter(mAdapterRV);
        mItemsListRV.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mItemsListRV, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (mActionMode != null) {
                            mAdapterRV.toggleSelection(position);
                            return;
                        }

                        ShoppingList item = mItemsInList.get(position);

                        String name = item.getItem().getName();
                        double amount = item.getAmount();
                        String nameUnit = item.getUnit().getName();
                        double price = item.getPrice();
                        boolean isBought = item.isBought();
                        long idItem = item.getIdItem();

                        EditItemDialogFragment editItemDialog = EditItemDialogFragment.newInstance(name, amount, price, isBought, nameUnit, idItem, mIdList, getArguments().getString(DATA_SAVE));
                        editItemDialog.setTargetFragment(ShoppingListFragment.this, EDIT_DIALOG_CODE);
                        editItemDialog.show(getFragmentManager(), EDIT_DIALOG_DATE);

                        mSaveItemId = idItem;
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        if (mActionMode != null) {
                            return;
                        }

                        mActionMode = getActivity().startActionMode(mActionModeCallback);
                        mAdapterRV.toggleSelection(position);
                    }

                    @Override
                    public void onSwipeRight(View view, int position) {
                        if (mActionMode != null) {
                            return;
                        }

                        RecyclerViewAdapter.ViewHolder holder = (RecyclerViewAdapter.ViewHolder) mItemsListRV.findViewHolderForAdapterPosition(position);
                        boolean isBought;
                        if (holder.mIsBought.getVisibility() == View.VISIBLE) {
                            holder.mIsBought.setVisibility(View.GONE);
                            isBought = false;
                        } else {
                            holder.mIsBought.setVisibility(View.VISIBLE);
                            isBought = true;
                        }

                        ShoppingListDataSource itemsInListDS;
                        try {
                            itemsInListDS = ShoppingListDataSource.getInstance();
                        } catch (NullPointerException e) {
                            itemsInListDS = ShoppingListDataSource.getInstance(getActivity());
                        }
                        ShoppingList item = mItemsInList.get(position);
                        itemsInListDS.setIsBought(isBought, item.getIdItem(), mIdList);

                        //if set bought items in the end of list - refresh list
                        //if it isn`t - try to update only spent sum
                        if (mIsBoughtEndInList) {
                            //mListState = mItemsListRV.onSaveInstanceState();
                            updateData();
                        } else {
                            double sum = sumOneItem(item);
                            if (mSaveSpentMoney != 0) {
                                if (isBought) {
                                    mSaveSpentMoney += sum;
                                } else {
                                    mSaveSpentMoney -= sum;
                                }
                                updateSpentSum(mSaveSpentMoney);
                            } else {
                                //mListState = mItemsListRV.onSaveInstanceState();
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
        itemTouchHelper.attachToRecyclerView(mItemsListRV);

        return v;
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
                if (data != null) {
                    mItemsInList = ((ShoppingListCursor) data).getItemsAsList();
                    mAdapterRV.setData(mItemsInList);       //update data in adapter
                    updateSums();
                    //TODO: check it
                   /* if (mListState != null) {
                        //mItemsListRV.onRestoreInstanceState(mListState); //doesn't use in rw
                        mListState = null;
                    } else if (mSaveItemId != -1) {
                        //mItemsListRV.setSelection(getPosition(mSaveItemId));
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
                if (mAdapterRV != null) {
                    //mAdapterRV.swapCursor(null);
                }
                break;
            default:
                break;
        }
    }

    private void updateData() {
        getLoaderManager().getLoader(DATA_LOADER).forceLoad();
    }

    private void deleteSelectedItems() {
        List<Integer> items = mAdapterRV.getSelectedItems();
        Collections.sort(items);

        ShoppingListDataSource itemInListDS = ShoppingListDataSource.getInstance();
        for (int i = items.size() - 1; i >= 0; i--) {
            int position = items.get(i);
            itemInListDS.delete(mItemsInList.get(position).getIdItem(), mIdList);
            //mListState = mList.onSaveInstanceState(); //doesn't use in rw
            mAdapterRV.removeItem(position);
            updateSums();
        }
    }

    private double sumMoney(boolean onlyBought) {
        double sum = 0;
        for (ShoppingList item : mItemsInList) {
            if ((onlyBought && item.isBought()) || !onlyBought) {
                sum += sumOneItem(item);
            }
        }
        if (onlyBought) {
            mSaveSpentMoney = sum;
        }
        return sum;
    }

    private double sumOneItem(ShoppingList item) {
        double price = item.getPrice();
        double amount = item.getAmount();
        double sum = price * (amount == 0 ? 1 : amount);
        return new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private void updateSums() {
        updateSpentSum(sumMoney(true));        //update spent money
        mTotalMoney.setText(localValue(sumMoney(false)));       //update total money
    }

    private void updateSpentSum(double newSum) {
        mSpentMoney.setText(localValue(newSum));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case ADD_DIALOG_CODE:
                mSaveItemId = data.getLongExtra(AddItemDialogFragment.ID_ITEM, -1);
                updateData();
                break;
            case EDIT_DIALOG_CODE:
                updateData();
                break;
        }
    }

    ////doesn't use yet
    /*private int getPosition(long id) {
        int index = 0;
        for (int i = 0; i < mItemsListRV.getCount(); i++) {
            if (mItemsListRV.getItemIdAtPosition(i) == id) {
                index = i;
                break;
            }
        }
        return index;
    }*/

    private static String localValue(double value) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }

    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private ArrayList<ShoppingList> mItems;
        private List<Integer> mSelectedItems = new ArrayList<>();

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

        public RecyclerViewAdapter() {
            mItems = new ArrayList<>();
        }

        public void setData(ArrayList<ShoppingList> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._shopping_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ShoppingList itemInList = mItems.get(position);

            holder.mView.setSelected(mSelectedItems.contains(position));

            holder.mImage.setImageResource(R.drawable.food);
            holder.mTextView.setText(itemInList.getItem().getName());
            if (itemInList.getAmount() == 0) {
                holder.mAmount.setText("-");
            } else {
                String amount = NumberFormat.getInstance().format(itemInList.getAmount())
                        + " " + itemInList.getUnit().getName();
                holder.mAmount.setText(amount);
            }
            holder.mPrice.setText(localValue(itemInList.getPrice()));
            int visibility = View.GONE;
            if (itemInList.isBought()) {
                visibility = View.VISIBLE;
            }
            holder.mIsBought.setVisibility(visibility);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void removeItem(int position) {
            mItems.remove(position);
            if (mSelectedItems.contains(position)) {
                mSelectedItems.remove(mSelectedItems.indexOf(position));
            }
            notifyItemRemoved(position);
        }

        public void toggleSelection(int position) {
            if (mSelectedItems.contains(position)) {
                for (int i = 0; i < mSelectedItems.size(); i++) {
                    if (mSelectedItems.get(i) == position) {
                        mSelectedItems.remove(i);
                        break;
                    }
                }
            } else {
                mSelectedItems.add(position);
            }
            notifyItemChanged(position);
        }

        public void clearSelections() {
            mSelectedItems.clear();
            notifyDataSetChanged();
        }

        public void selectItems(boolean isBought) {
            for (ShoppingList item : mItems){
                if (item.isBought() == isBought) {
                    int position = mItems.indexOf(item);
                    if (!mSelectedItems.contains(position)) {
                        mSelectedItems.add(position);
                        notifyItemChanged(position);
                    }
                }
            }
        }

        public List<Integer> getSelectedItems() {
            return mSelectedItems;
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
            ShoppingListDataSource mItemsInListDS;
            try {
                mItemsInListDS = ShoppingListDataSource.getInstance();
            } catch (NullPointerException e) {
                mItemsInListDS = ShoppingListDataSource.getInstance(mContext);
            }

            return mItemsInListDS.getItemsInList(mIdList);
        }
    }
}
