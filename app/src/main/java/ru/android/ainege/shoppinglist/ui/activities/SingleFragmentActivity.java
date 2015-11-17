package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.android.ainege.shoppinglist.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected void fragment(Fragment newFragment) {
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, newFragment).commit();
    }
}
