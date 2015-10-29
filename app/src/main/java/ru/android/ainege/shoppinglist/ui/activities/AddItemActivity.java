package ru.android.ainege.shoppinglist.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.android.ainege.shoppinglist.R;

public class AddItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_item);
    }
}
