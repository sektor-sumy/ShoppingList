package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.entities.ListEntity;
import ru.android.ainege.shoppinglist.db.entities.ShoppingListEntity;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;


public class ShoppingListFragment extends ListFragment {
    private static final String ADD_DIALOG_DATE = "addItemDialog";
    private static final String EDIT_DIALOG_DATE = "editItemDialog";
    private static final int ADD_DIALOG_CODE = 0;
    private static final int EDIT_DIALOG_CODE = 1;

    private ListsDataSource mListDS;
    private ListEntity mList;
    private ArrayList<ShoppingListEntity> mItemsInList;
    private ItemAdapter mAdapter;
    private TextView mSpentMoney, mTotalMoney;

    //edit later
    int idList = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListDS = new ListsDataSource(getActivity());

        mItemsInList = getItemsInList();
        mAdapter = new ItemAdapter(mItemsInList);
        setListAdapter(mAdapter);
    }

    private ArrayList<ShoppingListEntity> getItemsInList() {
        mList = mListDS.get(idList, true);
        return mList.getItemsInList();
    }

    private class ItemAdapter extends ArrayAdapter<ShoppingListEntity> {
        public ItemAdapter(ArrayList<ShoppingListEntity> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout._shopping_list_item, null);
            }
            final ShoppingListEntity itemInList = getItem(position);

            CheckBox isBuy = (CheckBox) convertView.findViewById(R.id.is_item_bought);
            isBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    itemInList.setBought(cb.isChecked());
                    ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
                    itemInListDS.update(itemInList);
                    updateSums();
                }
            });
            isBuy.setChecked(itemInList.isBought());
            TextView name = (TextView) convertView.findViewById(R.id.item_name);
            name.setText(itemInList.getItem().getName());
            TextView amount = (TextView) convertView.findViewById(R.id.item_amount);
            if (itemInList.getItem().getIdUnit() == UnitsTable.ID_NULL || itemInList.getItem().getAmount() == 0) {
                amount.setText("-");
            } else {
                amount.setText(itemInList.getItem().getAmount() + " " + itemInList.getItem().getUnit().getName());
            }
            TextView price = (TextView) convertView.findViewById(R.id.item_price);
            price.setText(String.valueOf(itemInList.getItem().getPrice()));

            return convertView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        final TextView listName = (TextView) v.findViewById(R.id.list_name);
        listName.setText(mList.getName());

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
        updateSums();

        ListView list = (ListView) v.findViewById(android.R.id.list);
        registerForContextMenu(list);
        return v;
    }

    private void updateSums() {
        mSpentMoney.setText(String.valueOf(mList.sumSpentMoney()));
        mTotalMoney.setText(String.valueOf(mList.sumTotalMoney()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch(requestCode) {
            case ADD_DIALOG_CODE:
                updateData();
                mAdapter.notifyDataSetChanged();
                break;
            case EDIT_DIALOG_CODE:
                mAdapter.notifyDataSetChanged();
                updateSums();
                break;
        }
    }

    private void updateData() {
        mItemsInList.clear();
        mItemsInList.addAll(getItemsInList());
        mAdapter.notifyDataSetChanged();
        updateSums();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        EditItemDialogFragment editItemDialog = EditItemDialogFragment.newInstance(getItem(position));
        editItemDialog.setTargetFragment(ShoppingListFragment.this, EDIT_DIALOG_CODE);
        editItemDialog.show(getFragmentManager(), EDIT_DIALOG_DATE);
    }

    private ShoppingListEntity getItem(int position) {
        return mList.getItemsInList().get(position);
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
                ShoppingListDataSource itemInListDS = new ShoppingListDataSource(getActivity());
                itemInListDS.delete(getItem((int) info.id));

                updateData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


}
