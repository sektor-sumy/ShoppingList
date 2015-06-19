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


public class ShoppingListFragment extends ListFragment {
    ArrayList<String> list;
    ListView mItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<>();
        list.add("Мясо1");
        list.add("Мясо2");
        list.add("Мясо2");
        list.add("Мясо3");

        setListAdapter(new ItemAdapter(list));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

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

        mItems = (ListView) v.findViewById(android.R.id.list);
        return v;
    }

    private class ItemAdapter extends ArrayAdapter<String> {

        public ItemAdapter(ArrayList<String> items) {
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout._shopping_list_item, null);
            }

            String s = getItem(position);

            CheckBox buyCheckBox = (CheckBox) convertView.findViewById(R.id.is_item_bought);
            TextView nameTextView = (TextView) convertView.findViewById(R.id.item_name);
            nameTextView.setText(s);
            TextView numberTextView = (TextView) convertView.findViewById(R.id.item_amount);
            numberTextView.setText("2 шт.");
            TextView priceTextView = (TextView) convertView.findViewById(R.id.item_price);
            priceTextView.setText("150");

            return convertView;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //TODO edit item
        Toast.makeText(getActivity(), "Редактирование: " + getListAdapter().getItem(position), Toast.LENGTH_SHORT).show();
    }
}
