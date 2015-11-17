package ru.android.ainege.shoppinglist.ui.activities;

import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.fragments.ListDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.QuestionDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;


public class ShoppingListActivity extends SingleFragmentActivity implements QuestionDialogFragment.DialogListener {
    private static final String ADD_DIALOG_DATE = "addListDialog";

    private long mId = -1;

    private boolean mIsBoughtFirst;
    private String mDataSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView appBarImage = (ImageView) findViewById(R.id.appbar_image);
        appBarImage.setImageResource(R.drawable.list);

        FloatingActionButton addItemFAB = (FloatingActionButton) findViewById(R.id.add_fab);
        addItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ShoppingListActivity.this, ItemActivity.class);
                i.putExtra(ItemActivity.EXTRA_ID_LIST, mId);
                i.putExtra(ItemActivity.EXTRA_DATA_SAVE, mDataSave);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(i, ActivityOptions.makeSceneTransitionAnimation(ShoppingListActivity.this).toBundle());
                } else {
                    startActivity(i);
                }
            }
        });

        fragment(ShoppingListFragment.newInstance(mId, mIsBoughtFirst, mDataSave));
    }

    @Override
    public void onResume() {
        super.onResume();
        getSettings();
    }

    private void getSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mIsBoughtFirst = prefs.getBoolean(getString(R.string.settings_key_sort_is_bought), true);
        String sortType = null;
        String regular = prefs.getString(getString(R.string.settings_key_sort_type), "");
        if (regular.contains(getResources().getString(R.string.sort_order_alphabet))) {
            sortType = ShoppingListDataSource.ALPHABET;
        } else if (regular.contains(getResources().getString(R.string.sort_order_up_price))) {
            sortType = ShoppingListDataSource.UP_PRICE;
        } else if (regular.contains(getResources().getString(R.string.sort_order_down_price))) {
            sortType = ShoppingListDataSource.DOWN_PRICE;
        } else if (regular.contains(getResources().getString(R.string.sort_order_adding))) {
            sortType = ShoppingListDataSource.ORDER_ADDING;
        } else {
            sortType = ShoppingListDataSource.ALPHABET;
        }
        ShoppingListDataSource itemsInListDS;
        try {
            itemsInListDS = ShoppingListDataSource.getInstance();
        } catch (NullPointerException e) {
            itemsInListDS = ShoppingListDataSource.getInstance(this);
        }
        itemsInListDS.setSortSettings(mIsBoughtFirst, sortType);

        if (!prefs.getBoolean(getString(R.string.settings_key_sort_is_default_data), true)) {
            mDataSave = SettingsDataItem.NOT_USE_DEFAULT_DATA;
        } else {
            String save = prefs.getString(getString(R.string.settings_key_sort_data_item), "");
            if (save.contains(getResources().getString(R.string.data_item_button))) {
                mDataSave = SettingsDataItem.SAVE_DATA_BUTTON;
            } else if (save.contains(getResources().getString(R.string.data_item_always))) {
                mDataSave = SettingsDataItem.ALWAYS_SAVE_DATA;
            } else if (save.contains(getResources().getString(R.string.data_item_never))) {
                mDataSave = SettingsDataItem.NEVER_SAVE_DAT;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add_list:
                ListDialogFragment addListDialog = new ListDialogFragment();
                addListDialog.show(getFragmentManager(), ADD_DIALOG_DATE);
                return true;
            case R.id.delete_list:
                QuestionDialogFragment dialogFrag = new QuestionDialogFragment();
                dialogFrag.show(getFragmentManager(), "dialog");
                return true;
            case R.id.update_list:
               /* ListDialogFragment editListDialog = ListDialogFragment.newInstance(mId,
                        mCursor.getString(mCursor.getColumnIndex(ListsTable.COLUMN_NAME)));
                editListDialog.show(getFragmentManager(), UPDATE_DIALOG_DATE);*/
                return true;
            case R.id.settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        ListsDataSource listDS = new ListsDataSource(this);
        if (mId != -1) {
            listDS.delete(mId);
            mId = -1;
        } else {
            Toast.makeText(this, R.string.error_delete_list, Toast.LENGTH_SHORT).show();
        }
    }
}
