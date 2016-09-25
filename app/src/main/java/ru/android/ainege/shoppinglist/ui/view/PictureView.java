package ru.android.ainege.shoppinglist.ui.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;
import ru.android.ainege.shoppinglist.util.Image;

public class PictureView {
	public static final int TAKE_PHOTO = 404;
	public static final int FROM_GALLERY = 405;

	private Fragment mFragment;
	private PictureInterface mPictureInterface;

	private ImageView mImageView;
	private File mFile;

	public interface PictureInterface {
		boolean isDeleteImage(String newPath);
		void resetImage();
		void loadImage(String path);
	}

	public PictureView(Fragment fragment, PictureInterface pictureInterface) {
		mFragment = fragment;
		mPictureInterface = pictureInterface;
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

	public void loadImage(String path, String previousPath) {
		if (mPictureInterface.isDeleteImage(path)) {
			Image.deleteFile(previousPath);
		}

		Image.create().insertImageToView(mFragment.getActivity(), path, mImageView);
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

	public void takePhotoResult() {
		Image.create().deletePhotoFromGallery(mFragment.getActivity(), mFile);
		mPictureInterface.loadImage(Image.getFilePath(mFile));
		mFile = null;
	}

	public void fromGalleryResult() {
		mPictureInterface.loadImage(Image.getFilePath(mFile));
		mFile = null;
	}

	public void cancelResult() {
		if (mFile != null) {
			mFile = null;
		}
	}

	private boolean hasPermission(String permission){
		return ContextCompat.checkSelfPermission(mFragment.getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
	}

	private void takePhoto(){
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		sentIntent(cameraIntent, TAKE_PHOTO);
	}

	private void selectFromGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		sentIntent(galleryIntent, FROM_GALLERY);
	}

	private void sentIntent(Intent intent, int requestCode) {
		mFile = Image.create().createImageFile(mFragment.getActivity());

		if (mFile != null) {
			int width = mFragment.getResources().getDisplayMetrics().widthPixels;
			int height = width * 9 / 16;

			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
			intent.putExtra("crop", "true");
			intent.putExtra("scale", true);
			intent.putExtra("scaleUpIfNeeded", true);
			intent.putExtra("noFaceDetection", true);
			intent.putExtra("aspectX", 16);
			intent.putExtra("aspectY", 9);
			intent.putExtra("outputX", width);
			intent.putExtra("outputY", height);
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			mFragment.startActivityForResult(intent, requestCode);
		} else {
			Toast.makeText(mFragment.getActivity(), mFragment.getActivity().getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
		}
	}
}
