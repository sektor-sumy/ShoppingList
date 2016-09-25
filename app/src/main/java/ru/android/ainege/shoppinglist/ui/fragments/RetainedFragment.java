package ru.android.ainege.shoppinglist.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import java.io.File;

import ru.android.ainege.shoppinglist.ui.OnFinishedImageListener;
import ru.android.ainege.shoppinglist.util.Image;

public class RetainedFragment extends Fragment implements OnFinishedImageListener {
	public static final int RESULT_OK = 1;
	public static final int ERROR_OUT_MEMORY = 2;
	public static final int ERROR_PROCESSING = 3;

	private OnFinishedImageListener mLoadingListener;
	private String mImagePath;
	private boolean mIsLoading;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	public String getImagePath() {
		return mImagePath;
	}

	public void setImagePath(String imagePath) {
		mImagePath = imagePath;
		mLoadingListener = null;
	}

	public boolean isLoading() {
		return mIsLoading;
	}

	public void execute(String imagePath, File file, Bitmap bitmap, DisplayMetrics metrics) {
		mIsLoading = true;
		mImagePath = imagePath;

		new Image.BitmapWorkerTask(file, bitmap, metrics.widthPixels - 30, this).execute();
	}

	@Override
	public void onFinished(int resultCode, String path) {
		mIsLoading = false;

		if (resultCode == RESULT_OK) {
			mImagePath = path;
		}

		if (mLoadingListener != null) {
			mLoadingListener.onFinished(resultCode, mImagePath);
		}
	}

	public void setOnLoadedFinish(OnFinishedImageListener listener){
		mLoadingListener = listener;
	}
}