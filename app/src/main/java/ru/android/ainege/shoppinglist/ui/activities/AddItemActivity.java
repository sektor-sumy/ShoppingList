package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.content.Intent;

import ru.android.ainege.shoppinglist.ui.fragments.AddItemFragment;

public class AddItemActivity extends SingleFragmentActivity {
    public final static String EXTRA_ID_LIST = "idList";
    public final static String EXTRA_DATA_SAVE = "idDataSave";

    @Override
    protected Fragment createFragment() {
        Intent intent = getIntent();
        long id = intent.getLongExtra(EXTRA_ID_LIST, -1);
        String data = intent.getStringExtra(EXTRA_DATA_SAVE);
        return AddItemFragment.newInstance(id, data);
    }
}
