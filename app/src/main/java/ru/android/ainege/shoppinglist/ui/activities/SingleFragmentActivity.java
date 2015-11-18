package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.android.ainege.shoppinglist.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {
    protected abstract Fragment getFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        injectFragment(getFragment(), getDefaultContainer());
    }

    protected int getDefaultContainer() {
        return R.id.fragment_container;
    }

    protected void injectFragment(Fragment fragment, Integer container) {
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(container, fragment).commit();
    }
}
