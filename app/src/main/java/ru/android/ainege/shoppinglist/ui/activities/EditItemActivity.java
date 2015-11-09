package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;

import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.EditItemDialogFragment;

public class EditItemActivity extends SingleFragmentActivity {
    public final static String EXTRA_ITEM = "item";
    public final static String EXTRA_DATA_SAVE = "idDataSave";

    @Override
    protected Fragment createFragment() {
        Intent intent = getIntent();
        ShoppingList itemInList = (ShoppingList) intent.getSerializableExtra(EXTRA_ITEM);
        String data = intent.getStringExtra(EXTRA_DATA_SAVE);
        return EditItemDialogFragment.newInstance(itemInList, data);
    }
}
