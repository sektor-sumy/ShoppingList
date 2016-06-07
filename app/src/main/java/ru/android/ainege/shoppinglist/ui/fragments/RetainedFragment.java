package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.ImageFragmentInterface;
import ru.android.ainege.shoppinglist.util.Image;

public class RetainedFragment extends Fragment implements ImageFragmentInterface{
	private ImageLoad loading;
	private String mImagePath;
	private boolean mIsLoading;

	public interface ImageLoad {
		void finish(String path);
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
		loading = null;
	}

	public boolean isLoading() {
		return mIsLoading;
	}

	public void execute(File file, int widthImageView) {
		mIsLoading = true;
		new Image.BitmapWorkerTask(file, widthImageView, this).execute();
	}

	public void execute(File file, Bitmap bitmap, int widthImageView) {
		mIsLoading = true;
		new Image.BitmapWorkerTask(file, bitmap, widthImageView, this).execute();
	}

	@Override
	public void onImageLoaded(boolean isSuccess, String path) {
		if (isSuccess) {
			mImagePath = path;
			mIsLoading = false;

			if (loading != null) {
				loading.finish(mImagePath);
			}
		} else {
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_bitmap), Toast.LENGTH_SHORT).show();
		}
	}

	public void setOnLoadedFinish(ImageLoad load){
		loading = load;
	}
}