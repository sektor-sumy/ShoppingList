package ru.android.ainege.shoppinglist.ui.fragments;

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
import ru.android.ainege.shoppinglist.db.entities.ListEntity;
import ru.android.ainege.shoppinglist.db.entities.ShoppingListEntity;


public class ShoppingListFragment extends ListFragment {
    ListEntity mListEntity;
    ListsDataSource mListsDataSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //edit later
        int idList = 1;

        mListsDataSource = new ListsDataSource(getActivity().getApplicationContext());
        mListEntity = mListsDataSource.get(idList, true);

        setListAdapter(new ItemAdapter(mListEntity.getItemsInList()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        TextView listName = (TextView) v.findViewById(R.id.list_name);
        listName.setText(mListEntity.getName());

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

        TextView spentMoney = (TextView) v.findViewById(R.id.spent_money);
        spentMoney.setText(String.valueOf(mListEntity.sumSpentMoney()));
        TextView totalMoney = (TextView) v.findViewById(R.id.total_money);
        totalMoney.setText(String.valueOf(mListEntity.sumTotalMoney()));

        return v;
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

            ShoppingListEntity s = getItem(position);

            CheckBox buyCheckBox = (CheckBox) convertView.findViewById(R.id.is_item_bought);
            buyCheckBox.setChecked(s.isBought());
            TextView nameTextView = (TextView) convertView.findViewById(R.id.item_name);
            nameTextView.setText(s.getItem().getName());
            TextView numberTextView = (TextView) convertView.findViewById(R.id.item_amount);
            if(s.getItem().getUnit() == null){
                numberTextView.setText("-");
            }else{
                numberTextView.setText(s.getItem().getAmount() + " " + s.getItem().getUnit().getName());
            }
            TextView priceTextView = (TextView) convertView.findViewById(R.id.item_price);
            priceTextView.setText(String.valueOf(s.getItem().getPrice()));

            return convertView;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //TODO edit item
        Toast.makeText(getActivity(), "Редактирование: " + getListAdapter().getItem(position), Toast.LENGTH_SHORT).show();
    }
}
