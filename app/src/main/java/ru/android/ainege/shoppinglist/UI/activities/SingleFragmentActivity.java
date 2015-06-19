package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import ru.android.ainege.shoppinglist.R;

/**
 * Created by Belkin on 06.06.2015.
 */
public abstract class SingleFragmentActivity extends Activity {
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);


        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null){
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}
