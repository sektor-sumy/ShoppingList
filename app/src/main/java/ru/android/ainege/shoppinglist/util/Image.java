package ru.android.ainege.shoppinglist.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.ui.OnFinishedImageListener;
import ru.android.ainege.shoppinglist.ui.fragments.RetainedFragment;

import static android.graphics.Bitmap.CompressFormat;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;

public class Image {
	public static final String PATH_PROTOCOL = "file://";
	public static final String ASSETS_IMAGE_PATH = PATH_PROTOCOL + "/android_asset/images/";
	public static final String RESOURCE_IMAGE_PATH = "android.resource://";
	public static final String CHARACTER_IMAGE_PATH = ASSETS_IMAGE_PATH + "character/";
	public static final String LIST_IMAGE_PATH = ASSETS_IMAGE_PATH + "list/";
	public static final String ITEM_IMAGE_PATH = ASSETS_IMAGE_PATH + "item/";
	private static final double MIN_RATIO = 1.25;
	private static final double MAX_RATIO = 2;

	public static int mLoadingImage = R.drawable.loader;
	public static int mDefaultImage = R.drawable.load_error;

	public static Image create() {
		return new Image();
	}

	public static String getFilePath(File file) {
		return Image.PATH_PROTOCOL + file.getAbsolutePath();
	}

	public static boolean deleteFile(String path) {
		boolean result = false;
		if (path != null && !path.contains(Image.ASSETS_IMAGE_PATH) && !path.contains(RESOURCE_IMAGE_PATH)) {
			File f = new File(Uri.parse(path).getPath());
			result = f.delete();
		}
		return result;
	}

	public static String getPathFromResource(Context context, int resource) {
		String packageName = "ru.android.ainege.shoppinglist";

		if (context != null) {
			packageName = context.getPackageName();
		}

		return RESOURCE_IMAGE_PATH + packageName + "/" + resource;
	}

	public Image insertImageToView(Context context, String path, ImageView image) {
		if (path != null) {
			insertImageToView(context, Uri.parse(path), image);
		}

		return this;
	}

	public Image insertImageToView(Context context, Uri path, ImageView image) {
		Picasso.with(context)
				.load(path)
				.placeholder(mLoadingImage)
				.error(mDefaultImage)
				.into(image);
		return this;
	}

	public Image setLoadingImage(int image) {
		mLoadingImage = image;
		return this;
	}

	public Image setDefaultImage(int image) {
		mDefaultImage = image;
		return this;
	}

	public File createImageFile(Context context) {
		if (isExternalStorageWritable()) {
			File mediaStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

			if (mediaStorageDir == null) {
				return null;
			}

			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String name = "IMG_" + timeStamp + ".jpg";

			return new File(mediaStorageDir.getAbsolutePath(), name);
		} else {
			return null;
		}
	}

	public void deletePhotoFromGallery(Activity activity, File file) {
		String[] projection = {BaseColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN};

		Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

		if ((cursor != null) && (cursor.moveToFirst())) {
			String id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
			long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));

			if (Math.abs(date - file.lastModified()) < 30000) {
				ContentResolver cr = activity.getContentResolver();
				cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + id, null);
			}
			cursor.close();
		}
	}

	private boolean postProcessingToFile(File file, int widthImageView) {
		if (!isExternalStorageReadable()) {
			return false;
		}

		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

		if (bitmap == null) {
			throw new NullPointerException("Bitmap is null");
		}

		return postProcessingToFile(file, bitmap, widthImageView);
	}

	private boolean postProcessingToFile(File file, Bitmap bitmap, int widthImageView) {
		return saveImageToFile(file, postProcessing(bitmap, widthImageView));
	}

	private boolean saveImageToFile(File file, Bitmap bitmap) {
		boolean result = false;

		try {
			FileOutputStream stream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, stream);
			stream.close();

			bitmap.recycle();

			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	private boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	private boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
	}

	private Bitmap postProcessing(Bitmap bitmap, int widthImageView) {
		if (isNeedCrop(bitmap)) {
			bitmap = crop(bitmap);
		}

		if (isNeedScale(bitmap, widthImageView)) {
			bitmap = scale(bitmap, widthImageView);
		}

		return bitmap;
	}

	private boolean isNeedCrop(Bitmap bitmap) {
		double ratio = getRatio(bitmap);

		return ratio < MIN_RATIO || ratio > MAX_RATIO;
	}

	private Bitmap crop(Bitmap originalBmp) {
		Bitmap bitmap;
		int bitmapWidth, bitmapHeight;
		double ratio = originalBmp.getWidth() / originalBmp.getHeight();

		if (ratio < MIN_RATIO) { //уменьшаем высоту
			bitmapWidth = originalBmp.getWidth();
			bitmapHeight = (int) (originalBmp.getWidth() / MIN_RATIO);

			bitmap = createBitmap(
					originalBmp,
					0,
					originalBmp.getHeight() / 2 - bitmapHeight / 2,
					bitmapWidth,
					bitmapHeight
			);
		} else if (ratio > MAX_RATIO) { //уменшаем ширину
			bitmapWidth = (int) (originalBmp.getHeight() * MAX_RATIO);
			bitmapHeight = originalBmp.getHeight();

			bitmap = createBitmap(
					originalBmp,
					originalBmp.getWidth() / 2 - bitmapWidth / 2,
					0,
					bitmapWidth,
					bitmapHeight
			);
		} else {
			bitmap = originalBmp;
		}

		originalBmp.recycle();

		return bitmap;
	}

	private boolean isNeedScale(Bitmap bitmap, int screenWidth) {
		return bitmap.getWidth() > screenWidth;
	}

	private Bitmap scale(Bitmap originalBmp, int width) {
		double ratio = getRatio(originalBmp);

		Bitmap bitmap = createScaledBitmap(originalBmp, width,
				(int) (width / ratio), false);

		originalBmp.recycle();

		return bitmap;
	}

	private double getRatio(Bitmap bitmap) {
		double width = bitmap.getWidth();
		double height = bitmap.getHeight();

		return width / height;
	}

	public static class BitmapWorkerTask extends AsyncTask<Integer, Void, Boolean> {
		private File mFile;
		private Bitmap mBitmap;
		private int mWidthImageView;
		private OnFinishedImageListener mOnFinishedImageListener;

		private int mResultCode = RetainedFragment.RESULT_OK;

		public BitmapWorkerTask(File file, Bitmap bitmap, int widthImageView, OnFinishedImageListener onFinishedImageListener) {
			mFile = file;
			mWidthImageView = widthImageView;
			mOnFinishedImageListener = onFinishedImageListener;
			mBitmap = bitmap;
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			boolean result;

			try {
				if (mBitmap == null) {
					result = Image.create().postProcessingToFile(mFile, mWidthImageView);
				} else {
					result = Image.create().postProcessingToFile(mFile, mBitmap, mWidthImageView);
				}
			} catch (OutOfMemoryError | Exception e) {
				if (e instanceof OutOfMemoryError) {
					mResultCode = RetainedFragment.ERROR_OUT_MEMORY;
				} else {
					mResultCode = RetainedFragment.ERROR_PROCESSING;
				}

				e.printStackTrace();
				FirebaseCrash.log("Catched exception");
				FirebaseCrash.report(e);

				deleteFile(mFile.getAbsolutePath());
				result = false;
			}

			return result;
		}

		@Override
		protected void onPostExecute(Boolean isSuccess) {
			mOnFinishedImageListener.onFinished(mResultCode, getFilePath(mFile));
		}
	}
}
