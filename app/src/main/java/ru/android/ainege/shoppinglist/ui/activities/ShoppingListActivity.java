package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.tables.ListsTable;
import ru.android.ainege.shoppinglist.ui.fragments.AddListDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;


public class ShoppingListActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, AddListDialogFragment.List {
    public static final String APP_PREFERENCES = "setting";
    public static final String APP_PREFERENCES_ID = "idList";

    private static final int DATA_LOADER = 0;
    private static final String ADD_DIALOG_DATE = "addListDialog";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private Cursor mCursor;
    private SimpleCursorAdapter mAdapter;

    private SharedPreferences mSettings;
    private long mId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mTitle = getActionBar().getTitle();
                getActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings.contains(APP_PREFERENCES_ID)) {
            mId = mSettings.getLong(APP_PREFERENCES_ID, mId);
            selectItem(mId);
        }

        getLoaderManager().initLoader(DATA_LOADER, null, this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()) {
            case R.id.delete_all_bought:
                return true;
            case R.id.add_list:
                AddListDialogFragment addListDialog = new AddListDialogFragment();
                addListDialog.show(getFragmentManager(), ADD_DIALOG_DATE);
                return true;
            case R.id.delete_list:
                return true;
            case R.id.update_list:
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                                selectItem(mCursor.getLong(mCursor.getColumnIndex(ListsTable.COLUMN_ID)));
                            }
                            mDrawerList.setItemChecked(getPosition(mId), true);
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mCursor.moveToPosition(position);
            mId = mCursor.getLong(mCursor.getColumnIndex(ListsTable.COLUMN_ID));
            mTitle = mCursor.getString(mCursor.getColumnIndex(ListsTable.COLUMN_NAME));

            mDrawerList.setItemChecked(position, true);
            selectItem(mId);
        }
    }

    private void selectItem(long id) {
        Fragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putLong(ShoppingListFragment.ID_LIST, id);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();

        mDrawerLayout.closeDrawer(mDrawerList);
        if (!mSettings.contains(APP_PREFERENCES_ID) || mId != mSettings.getLong(APP_PREFERENCES_ID, mId)) {
            saveId();
        }

    }

    @Override
    public void updateResult(long idList) {
        if (idList != -1) {
            updateData();
            mId = idList;
            selectItem(mId);
        }
    }

    public void saveId() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong(APP_PREFERENCES_ID, mId);
        editor.apply();
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
