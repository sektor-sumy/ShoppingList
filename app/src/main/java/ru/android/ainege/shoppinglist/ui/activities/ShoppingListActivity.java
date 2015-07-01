package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;

import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;

/**
 * Created by Belkin on 06.06.2015.
 */
public class ShoppingListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ShoppingListFragment();
    }
}
