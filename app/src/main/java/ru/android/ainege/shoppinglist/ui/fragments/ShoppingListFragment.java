package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    public static final String ITEM_IN_LIST = "itemInList";
    public static final String ID_LIST = "idList";

    ListEntity mList;
    ListsDataSource mListDS;
    TextView mSpentMoney;
    TextView mTotalMoney;
    ItemAdapter mAdapter;

    //edit later
    int idList = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListDS = new ListsDataSource(getActivity().getApplicationContext());
        mList = mListDS.get(idList, true);

        mAdapter = new ItemAdapter(mList.getItemsInList());
        setListAdapter(mAdapter);
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
                    AddItemDialogFragment addItemDialog = new AddItemDialogFragment();
                    Bundle args = new Bundle();
                    args.putInt(ID_LIST, idList);
                    addItemDialog.setArguments(args);
                    addItemDialog.show(getFragmentManager(), "addItemDialog");
                }
                return false;
            }
        });

        mSpentMoney = (TextView) v.findViewById(R.id.spent_money);
        mTotalMoney = (TextView) v.findViewById(R.id.total_money);
        updateSums();

        return v;
    }

    private void updateSums() {
        mSpentMoney.setText(String.valueOf(mList.sumSpentMoney()));
        mTotalMoney.setText(String.valueOf(mList.sumTotalMoney()));
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        EditItemDialogFragment editItemDialog = new EditItemDialogFragment();
        Bundle args = new Bundle();
        ShoppingListEntity itemInList = mList.getItemsInList().get(position);
        args.putSerializable(ITEM_IN_LIST, itemInList);
        editItemDialog.setArguments(args);
        editItemDialog.show(getFragmentManager(), "editItemDialog");
    }
}
