package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.OnFinishedImageListener;
import ru.android.ainege.shoppinglist.util.Image;

public class RetainedFragment extends Fragment implements OnFinishedImageListener {
	public static final int RESULT_OK = 1;
	public static final int ERROR_OUT_MEMORY = 2;
	public static final int ERROR_PROCESSING = 3;
	private Activity mActivity;
	private OnFinishedImageListener mLoadingListener;
	private String mImagePath;
	private boolean mIsLoading;
	private ImageView mImageView;

	public RetainedFragment() {

	}

	public RetainedFragment(Activity activity) {
		mActivity = activity;
	}

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

	public void execute(ImageView view, String imagePath, File file) {
		execute(view, imagePath, file, null);
	}

	public void execute(ImageView view, String imagePath, File file, Bitmap bitmap) {
		DisplayMetrics metrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		mImagePath = imagePath;
		mImageView = view;

		mIsLoading = true;
		String path = Image.getPathFromResource(mActivity, Image.mLoadingImage);
		Image.create().insertImageToView(mActivity, path, mImageView);
		new Image.BitmapWorkerTask(file, bitmap, metrics.widthPixels - 30, this).execute();
	}

	@Override
	public void onFinished(int resultCode, String path) {
		mIsLoading = false;

		if (resultCode == RESULT_OK) {
			mImagePath = path;

			if (mLoadingListener != null) {
				mLoadingListener.onFinished(resultCode, mImagePath);
			}
		} else {
			Image.create().insertImageToView(mActivity, mImagePath, mImageView);
			String message;

			switch (resultCode) {
				case ERROR_OUT_MEMORY:
					message = getString(R.string.error_out_of_memory);
					break;
				case ERROR_PROCESSING:
				default:
					message = getString(R.string.error_bitmap);
			}

			Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
		}
	}

	public void setOnLoadedFinish(OnFinishedImageListener listener){
		mLoadingListener = listener;
	}
}