package ru.android.ainege.shoppinglist;

import android.app.Fragment;

/**
 * Created by Belkin on 06.06.2015.
 */
public class ShoppingListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ShoppingListFragment();
    }
}
