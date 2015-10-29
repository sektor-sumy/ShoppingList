package ru.android.ainege.shoppinglist.ui.activities;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.fragments.ListDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.QuestionDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;


public class ShoppingListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ListDialogFragment.List, QuestionDialogFragment.DialogListener {
    public static final String APP_PREFERENCES = "setting";
    public static final String APP_PREFERENCES_ID = "idList";

    private static final int DATA_LOADER = 0;
    private static final String ADD_DIALOG_DATE = "addListDialog";
    private static final String UPDATE_DIALOG_DATE = "updateListDialog";
    public final static String EXTRA_ID_LIST = "idList";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private Cursor mCursor;
    private SimpleCursorAdapter mAdapter;

    private SharedPreferences mSettings;
    private long mId = -1;

    private boolean mIsBoughtFirst;
    private String mDataSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView appBarImage = (ImageView) findViewById(R.id.appbar_image);
        appBarImage.setImageResource(R.drawable.list);

        initDrawer();

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCursor.moveToPosition(position);
                mId = mCursor.getLong(mCursor.getColumnIndex(ListsTable.COLUMN_ID));

                if (mDrawerLayout != null) {
                    mTitle = mCursor.getString(mCursor.getColumnIndex(ListsTable.COLUMN_NAME));
                }

                mDrawerList.setItemChecked(position, true);
                selectItem(mId);
            }
        });

        FloatingActionButton addItemFAB = (FloatingActionButton) findViewById(R.id.add_fab);
        addItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ShoppingListActivity.this, AddItemActivity.class);
                i.putExtra(EXTRA_ID_LIST, mId);
                startActivity(i);
            }
        });

        getLoaderManager().initLoader(DATA_LOADER, null, this);
    }

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mTitle = mDrawerTitle = getTitle();

        if (mDrawerLayout == null) {
            setAppTitle(mTitle);
            return;
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                setAppTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                setAppTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateItemsList();
    }

    private void updateItemsList() {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_ID)) {
            mId = mSettings.getLong(APP_PREFERENCES_ID, mId);
            if (mId != -1) {
                getSettings();
                selectItem(mId);
            }
        }
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
            mDataSave = SettingsDataItem.DATA_NOT_DEFAULT;
        } else {
            String save = prefs.getString(getString(R.string.settings_key_sort_data_item), "");
            if (save.contains(getResources().getString(R.string.data_item_button))) {
                mDataSave = SettingsDataItem.DATA_SAVE_BUTTON;
            } else if (save.contains(getResources().getString(R.string.data_item_always))) {
                mDataSave = SettingsDataItem.DATA_SAVE_ALWAYS;
            } else if (save.contains(getResources().getString(R.string.data_item_never))) {
                mDataSave = SettingsDataItem.DATA_SAVE_NEVER;
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()) {
            case R.id.delete_all_bought:
                ShoppingListDataSource itemInListDS = ShoppingListDataSource.getInstance();
                itemInListDS.deleteAllBought(mId);
                selectItem(mId);
                return true;
            case R.id.add_list:
                ListDialogFragment addListDialog = new ListDialogFragment();
                addListDialog.show(getFragmentManager(), ADD_DIALOG_DATE);
                return true;
            case R.id.delete_list:
                QuestionDialogFragment dialogFrag = new QuestionDialogFragment();
                dialogFrag.show(getFragmentManager(), "dialog");
                return true;
            case R.id.update_list:
                ListDialogFragment editListDialog = ListDialogFragment.newInstance(mId,
                        mCursor.getString(mCursor.getColumnIndex(ListsTable.COLUMN_NAME)));
                editListDialog.show(getFragmentManager(), UPDATE_DIALOG_DATE);
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
            updateData();
            mId = -1;
        } else {
            Toast.makeText(this, R.string.error_delete_list, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> loader = null;
        switch (id) {
            case DATA_LOADER:
                loader = new MyCursorLoader(this);
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
                mCursor = data;
                if(mCursor != null) {
                    String[] from = new String[] {ListsTable.COLUMN_NAME};
                    int[] to = new int[] { R.id.name};
                    mAdapter = new SimpleCursorAdapter(this, R.layout._list_item, mCursor, from, to, 0);
                    mDrawerList.setAdapter(mAdapter);
                    mDrawerList.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mId == -1) {
                                mCursor.moveToFirst();
                                getSettings();
                                selectItem(mCursor.getLong(mCursor.getColumnIndex(ListsTable.COLUMN_ID)));
                            }
                            int position = getPosition(mId);
                            if (mDrawerLayout != null) {
                                mCursor.moveToPosition(position);
                                mTitle = mCursor.getString(mCursor.getColumnIndex(ListsTable.COLUMN_NAME));
                                setAppTitle(mTitle);
                            }
                            mDrawerList.setItemChecked(position, true);
                        }
                    });
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
                    mAdapter.swapCursor(null);
                }
                break;
            default:
                break;
        }
    }

    private void updateData() {
        getLoaderManager().getLoader(DATA_LOADER).forceLoad();
    }

    private int getPosition(long id) {
        int index = 0;
        for (int i = 0; i < mDrawerList.getCount(); i++) {
            if (mDrawerList.getItemIdAtPosition(i) == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void updateResult(long idList) {
        if (idList != -1 && idList != mId) {
            updateData();
            mId = idList;
            selectItem(mId);
        } else {
            updateData();
        }
    }

    private void selectItem(long id) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, ShoppingListFragment.newInstance(id, mIsBoughtFirst, mDataSave)).commit();

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }

        if (!mSettings.contains(APP_PREFERENCES_ID) || mId != mSettings.getLong(APP_PREFERENCES_ID, mId)) {
            saveId();
        }
    }

    private void saveId() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong(APP_PREFERENCES_ID, mId);
        editor.apply();
    }

    private void setAppTitle(CharSequence appTitle) {
        this.getSupportActionBar().setTitle(appTitle);
    }

    private static class MyCursorLoader extends CursorLoader {
        private Context mContext;

        public MyCursorLoader(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public Cursor loadInBackground() {
            ListsDataSource listDS = new ListsDataSource(mContext);

            return listDS.getAll();
        }
    }
}
