package ru.android.ainege.shoppinglist.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

public interface OnCreateViewListener {
	void onCreateViewListener(Fragment fragment, Toolbar toolbar);
	void onDeleteSavedList();
}
