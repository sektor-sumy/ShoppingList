package ru.android.ainege.shoppinglist.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.android.ainege.shoppinglist.R;

import static android.graphics.Bitmap.CompressFormat;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;

public class Image {
	public static final String ASSETS_IMAGE_PATH = "file:///android_asset/images/";
	private static final double MIN_RATIO = 1.25;
	private static final double MAX_RATIO = 2;
	private int mLoadingImage = R.drawable.load;
	private int mDefaultImage = R.drawable.default_list;

	public static Image create() {
		return new Image();
	}

	public File createImageFile() {
		if (isExternalStorageWritable()) {
			File mediaStorageDir = getAlbumStorageDir();
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

	public boolean postProcessing(File file, int widthImageView) {
		if (!isExternalStorageReadable()) {
			return false;
		}

		boolean result = false;
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

		if (isNeedCrop(bitmap)) {
			bitmap = crop(bitmap);
		}

		if (isNeedScale(bitmap, widthImageView)) {
			bitmap = scale(bitmap, widthImageView);
		}

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

	public Image insertImageToView(Context context, Uri path, ImageView image) {
		Picasso.with(context)
				.load(path)
				.placeholder(mLoadingImage)
				.error(mDefaultImage)
				.into(image);
		return this;
	}

	public Image insertImageToView(Context context, String path, ImageView image) {
		insertImageToView(context, Uri.parse(path), image);
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

	private boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	private boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	private File getAlbumStorageDir() {
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "ShopList");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		return mediaStorageDir;
	}

	private boolean isNeedCrop(Bitmap bitmap) {
		double ratio = getRatio(bitmap);

		return ratio < MIN_RATIO || ratio > MAX_RATIO;
	}

	private boolean isNeedScale(Bitmap bitmap, int screenWidth) {
		return bitmap.getWidth() > screenWidth;
	}

	private double getRatio(Bitmap bitmap) {
		double width = bitmap.getWidth();
		double height = bitmap.getHeight();

		return width / height;
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

	private Bitmap scale(Bitmap originalBmp, int width) {
		double ratio = getRatio(originalBmp);

		Bitmap bitmap = createScaledBitmap(originalBmp, width,
				(int) (width / ratio), false);

		originalBmp.recycle();

		return bitmap;
	}
}
