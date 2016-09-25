package ru.android.ainege.shoppinglist.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.android.ainege.shoppinglist.ui.fragments.MainPreferenceFragment;

public class SettingsActivity extends AppCompatActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new MainPreferenceFragment())
					.commit();
		}
	}

	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_OK,
				new Intent().putExtra(CatalogsActivity.LAST_EDIT, getIntent().getSerializableExtra(CatalogsActivity.EXTRA_DATA)));
		super.onBackPressed();
	}
}
