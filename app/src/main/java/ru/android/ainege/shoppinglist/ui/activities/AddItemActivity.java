package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;

import ru.android.ainege.shoppinglist.ui.fragments.AddItemDialogFragment;

public class AddItemActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        Intent intent = getIntent();
        long id =intent.getLongExtra(ShoppingListActivity.EXTRA_ID_LIST, 0);
        return AddItemDialogFragment.newInstance(id, "null");
    }
}
