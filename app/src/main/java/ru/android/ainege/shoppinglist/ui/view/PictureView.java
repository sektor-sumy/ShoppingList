package ru.android.ainege.shoppinglist.ui.view;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import java.io.File;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.OnFinishedImageListener;
import ru.android.ainege.shoppinglist.ui.fragments.RetainedFragment;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;
import ru.android.ainege.shoppinglist.util.Image;

public class PictureView implements OnFinishedImageListener {
	public static final int RESULT_OK = RetainedFragment.RESULT_OK;
	public static final int ERROR_OUT_MEMORY = RetainedFragment.ERROR_OUT_MEMORY;
	public static final int ERROR_PROCESSING = RetainedFragment.ERROR_PROCESSING;

	private static final String RETAINED_FRAGMENT = "retained_fragment_item";
	public static final int TAKE_PHOTO = 404;
	public static final int FROM_GALLERY = 405;

	private Fragment mFragment;
	private RetainedFragment mRetainedFragment;
	private PictureInterface mPictureInterface;

	private ImageView mImageView;
	private File mFile;

	public interface PictureInterface {
		boolean isDeleteImage(String newPath);
		void resetImage();
		void loadImage(String path);
	}

	public PictureView(Fragment fragment, Bundle savedInstanceState, PictureInterface pictureInterface) {
		mFragment = fragment;
		mPictureInterface = pictureInterface;
		setRetainedFragment(savedInstanceState, this);
	}

	@Override
	public void onFinished(int resultCode, String path) {
		if (resultCode == PictureView.RESULT_OK) {
			mPictureInterface.loadImage(path);
		} else {
			errirLoadingImage(resultCode);
		}
	}

	public ImageView getImage() {
		return mImageView;
	}

	public void setImage(View image) {
		mImageView = (ImageView) image;
		mFragment.registerForContextMenu(mImageView);

		mImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.showContextMenu();
			}
		});
	}

	public File getFile() {
		return mFile;
	}

	public void setFile(File file) {
		mFile = file;
	}

	public String getImagePath() {
		return mRetainedFragment.getImagePath();
	}

	public void setImagePath(String imagePath) {
		mRetainedFragment.setImagePath(imagePath);
	}

	public void loadImage(String path, String previousPath) {
		if (mPictureInterface.isDeleteImage(path)) {
			Image.deleteFile(previousPath);
		}

		Image.create().insertImageToView(mFragment.getActivity(), path, mImageView);
	}

	public boolean isLoading() {
		return mRetainedFragment.isLoading();
	}

	public void onCreateContextMenu(ContextMenu menu, int itemSetVisible, String title) {
		mFragment.getActivity().getMenuInflater().inflate(R.menu.image_menu, menu);
		menu.findItem(itemSetVisible).setVisible(true);
		menu.setHeaderTitle(title);
	}

	public boolean onContextItemSelected(MenuItem item, ItemFragment.OnClickListener onClickListener) {
		switch (item.getItemId()) {
			case R.id.take_photo:
				if (onClickListener != null) {
					onClickListener.onImageClick();
				}

				if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					takePhoto();
				} else {
					mFragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, TAKE_PHOTO);
				}
				break;
			case R.id.select_from_gallery:
				if (onClickListener != null) {
					onClickListener.onImageClick();
				}

				if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					selectFromGallery();
				} else {
					mFragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, FROM_GALLERY);
				}
				break;
			case R.id.default_image:
			case R.id.random_image:
				mPictureInterface.resetImage();
				break;
			default:
				return false;
		}

		return true;
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case TAKE_PHOTO:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					takePhoto();
				}
				break;
			case FROM_GALLERY:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					selectFromGallery();
				}
				break;

			default:
				break;
		}
	}

	public void takePhotoResult(String imagePath) {
		Image.create().deletePhotoFromGallery(mFragment.getActivity(), mFile);
		executeLoadImage(imagePath, mFile, null);
	}

	public void fromGalleryResult(String imagePath, Uri selectedImage) {
		File file = Image.create().createImageFile(mFragment.getActivity());

		try {
			if (file != null) {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(mFragment.getActivity().getContentResolver(), selectedImage);
				executeLoadImage(imagePath, file, bitmap);
			} else {
				Toast.makeText(mFragment.getActivity(), mFragment.getActivity().getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
			}
		} catch (OutOfMemoryError | Exception e) {
			e.printStackTrace();
			FirebaseCrash.log(mFragment.getActivity().getResources().getString(R.string.catched_exception));
			FirebaseCrash.report(e);
			Image.deleteFile(file.getAbsolutePath());
		}
	}

	private void executeLoadImage(String imagePath, File file, Bitmap bitmap) {
		String path = Image.getPathFromResource(mFragment.getActivity(), Image.mLoadingImage);
		Image.create().insertImageToView(mFragment.getActivity(), path, mImageView);

		DisplayMetrics metrics = mFragment.getResources().getDisplayMetrics();
		mRetainedFragment.execute(imagePath, file, bitmap, metrics);
	}

	private void errirLoadingImage(int resultCode) {
		Image.create().insertImageToView(mFragment.getActivity(), mRetainedFragment.getImagePath(), mImageView);
		String message;

		switch (resultCode) {
			case ERROR_OUT_MEMORY:
				message = mFragment.getString(R.string.error_out_of_memory);
				break;
			case ERROR_PROCESSING:
			default:
				message = mFragment.getString(R.string.error_bitmap);
		}

		Toast.makeText(mFragment.getActivity(), message, Toast.LENGTH_SHORT).show();
	}

	private void setRetainedFragment(Bundle savedInstanceState, OnFinishedImageListener listener) {
		FragmentManager fm = mFragment.getFragmentManager();
		mRetainedFragment = (RetainedFragment) fm.findFragmentByTag(RETAINED_FRAGMENT);

		if (mRetainedFragment == null || savedInstanceState == null) {
			mRetainedFragment = new RetainedFragment();
			fm.beginTransaction().add(mRetainedFragment, RETAINED_FRAGMENT).commit();
		}

		mRetainedFragment.setOnLoadedFinish(listener);
	}

	private boolean hasPermission(String permission){
		return ContextCompat.checkSelfPermission(mFragment.getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
	}

	private void takePhoto(){
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mFile = Image.create().createImageFile(mFragment.getActivity());

		if (mFile != null) {
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
			mFragment.startActivityForResult(cameraIntent, TAKE_PHOTO);
		} else {
			Toast.makeText(mFragment.getActivity(), mFragment.getActivity().getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
		}
	}

	private void selectFromGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		mFragment.startActivityForResult(galleryIntent, FROM_GALLERY);
	}
}
