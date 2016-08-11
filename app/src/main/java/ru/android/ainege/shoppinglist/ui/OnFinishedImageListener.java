package ru.android.ainege.shoppinglist.ui;

import android.app.Activity;

public interface OnFinishedImageListener {
	void onFinished(boolean isSuccess, String path);
	Activity getActivity();
}
