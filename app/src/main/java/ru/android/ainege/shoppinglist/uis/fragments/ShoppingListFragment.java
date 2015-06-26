package ru.android.ainege.shoppinglist.uis.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ShoppingListSQLiteHelper;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.entities.ListEntity;
import ru.android.ainege.shoppinglist.db.entities.ShoppingListEntity;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;


public class ShoppingListFragment extends ListFragment {
    public static final String itemInListSK = "itemInList";

    ListEntity mList;
    ListsDataSource mListDS;
    TextView mSpentMoney;
    TextView mTotalMoney;
    ItemAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //edit later
        int idList = 1;

        mListDS = new ListsDataSource(getActivity().getApplicationContext());
        mList = mListDS.get(idList, true);

        mAdapter = new ItemAdapter(mList.getItemsInList());
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        TextView listName = (TextView) v.findViewById(R.id.list_name);
        listName.setText(mList.getName());

        EditText newItemName = (EditText) v.findViewById(R.id.new_item_name);
        newItemName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    //Toast.makeText(getActivity(), "Добавление нового товара", Toast.LENGTH_SHORT).show();
                    ShoppingListSQLiteHelper helper = new ShoppingListSQLiteHelper(getActivity().getApplicationContext());
                    helper.getReadableDatabase();

                    Toast.makeText(getActivity(), "БД создана", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSpentMoney = (TextView) v.findViewById(R.id.spent_money);
        mTotalMoney = (TextView) v.findViewById(R.id.total_money);
        updateSums();

        return v;
    }

    private void updateSums(){
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
            if(itemInList.getItem().getIdUnit() == UnitsTable.ID_NULL || itemInList.getItem().getAmount() == 0) {
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
        EditItemDialogFragment dialog = new EditItemDialogFragment();
        Bundle args = new Bundle();
        ShoppingListEntity itemInList =  mList.getItemsInList().get(position);
        args.putSerializable(itemInListSK, itemInList);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "editItemDialog");
    }
}
