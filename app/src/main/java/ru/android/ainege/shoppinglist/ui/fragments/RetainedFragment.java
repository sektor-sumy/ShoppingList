package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.OnFinishedImageListener;
import ru.android.ainege.shoppinglist.util.Image;

public class RetainedFragment extends Fragment implements OnFinishedImageListener {
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

	public void execute(ImageView view, String imagePath, File file, int widthImageView) {
		execute(view, imagePath, file, null, widthImageView);
	}

	public void execute(ImageView view, String imagePath, File file, Bitmap bitmap, int widthImageView) {
		mImagePath = imagePath;
		mImageView = view;

		mIsLoading = true;
		Image.create().insertImageToView(mActivity, "android.resource://ru.android.ainege.shoppinglist/" + Image.mLoadingImage, mImageView);
		new Image.BitmapWorkerTask(file, bitmap, widthImageView, this).execute();
	}

	@Override
	public void onFinished(boolean isSuccess, String path) {
		mIsLoading = false;

		if (isSuccess) {
			mImagePath = path;

			if (mLoadingListener != null) {
				mLoadingListener.onFinished(isSuccess, mImagePath);
			}
		} else {
			Image.create().insertImageToView(mActivity, mImagePath, mImageView);
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_bitmap), Toast.LENGTH_SHORT).show();
		}
	}

	public void setOnLoadedFinish(OnFinishedImageListener load){
		mLoadingListener = load;
	}
}