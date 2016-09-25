package ru.android.ainege.shoppinglist.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.android.ainege.shoppinglist.R;

public class Image {
	private static final String PATH_PROTOCOL = "file://";
	private static final String RESOURCE_IMAGE_PATH = "android.resource://";
	public static final String ASSETS_IMAGE_PATH = PATH_PROTOCOL + "/android_asset/images/";
	public static final String CHARACTER_IMAGE_PATH = ASSETS_IMAGE_PATH + "character/";
	public static final String LIST_IMAGE_PATH = ASSETS_IMAGE_PATH + "list/";
	public static final String ITEM_IMAGE_PATH = ASSETS_IMAGE_PATH + "item/";

	public static Image create() {
		return new Image();
	}

	public static String getFilePath(File file) {
		return Image.PATH_PROTOCOL + file.getAbsolutePath();
	}

	public static String getPathFromResource(Context context, int resource) {
		String packageName = "ru.android.ainege.shoppinglist";

		if (context != null) {
			packageName = context.getPackageName();
		}

		return RESOURCE_IMAGE_PATH + packageName + "/" + resource;
	}

	public static boolean deleteFile(String path) {
		boolean result = false;

		if (path != null && !path.contains(Image.ASSETS_IMAGE_PATH) && !path.contains(RESOURCE_IMAGE_PATH)) {
			File f = new File(Uri.parse(path).getPath());
			result = f.delete();
		}

		return result;
	}

	public Image insertImageToView(Context context, String path, ImageView image) {
		if (path != null) {
			Picasso.with(context)
					.load(Uri.parse(path))
					.placeholder(R.drawable.loader)
					.error(R.drawable.load_error)
					.into(image);
		}

		return this;
	}

	public File createImageFile(Context context) {
		File mediaStorageDir = getMediaDir(context);
		File newFile = null;

		if (mediaStorageDir != null) {
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String name = "IMG_" + timeStamp + ".jpg";

			newFile = new File(mediaStorageDir, name);
		}

		return newFile;
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

	private File getMediaDir(Context context){
		return isExternalStorageWritable() ? context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : null;
	}

	private boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}
}
