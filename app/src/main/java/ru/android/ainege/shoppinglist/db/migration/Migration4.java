package ru.android.ainege.shoppinglist.db.migration;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

import ru.android.ainege.shoppinglist.util.Image;

public class Migration4 extends UpgradeDB {

	public Migration4(SQLiteDatabase db, Context ctx) {
		super(db, ctx);
	}

	@Override
	public void run() {
		updateImages();
	}

	private void updateImages() {
		File oldPath = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "ShopList");
		File newPath = mCtx.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

		if (oldPath.isDirectory()) {
			File[] images = oldPath.listFiles();
			ArrayList<String> imagesFromDB = getImages();

			if (images != null) {
				for (File image : images) {
					if (imagesFromDB.contains(Image.PATH_PROTOCOL + image.getAbsolutePath())) {
						image.renameTo(new File(newPath, image.getName()));
					} else {
						image.delete();
					}
				}

				updateListImages(oldPath.getAbsolutePath(), newPath.getAbsolutePath());
				updateItemImages(oldPath.getAbsolutePath(), newPath.getAbsolutePath());
			}

			oldPath.delete();
		}
	}

	private ArrayList<String> getImages() {
		String where = " WHERE image not like '" + Image.ASSETS_IMAGE_PATH + "%' ";
		Cursor cursor = mDb.rawQuery("SELECT " + Migration3.ItemT.COLUMN_IMAGE_PATH + " AS image" +
				" FROM " + Migration3.ItemT.TABLE_NAME + where +
				" UNION SELECT " + Migration3.ItemT.COLUMN_DEFAULT_IMAGE_PATH + " AS image" +
				" FROM " + Migration3.ItemT.TABLE_NAME + where +
				" UNION SELECT " + Migration3.ListT.COLUMN_IMAGE_PATH + " AS image" +
				" FROM " + Migration3.ListT.TABLE_NAME + where, null);
		ArrayList<String> images = new ArrayList<>();
		while (cursor.moveToNext()) {
			images.add(cursor.getString(0));
		}

		cursor.close();
		return images;
	}

	private void updateItemImages(String oldPath, String newPath) {
		mDb.execSQL("UPDATE " + Migration3.ItemT.TABLE_NAME + " SET " +
				Migration3.ItemT.COLUMN_IMAGE_PATH + " = replace(" + Migration3.ItemT.COLUMN_IMAGE_PATH + ", '" + oldPath + "', '" + newPath + "'), " +
				Migration3.ItemT.COLUMN_DEFAULT_IMAGE_PATH + " = replace(" + Migration3.ItemT.COLUMN_DEFAULT_IMAGE_PATH + ", '" + oldPath + "', '" + newPath + "')");
	}

	private void updateListImages(String oldPath, String newPath) {
		mDb.execSQL("UPDATE " + Migration3.ListT.TABLE_NAME + " SET " +
				Migration3.ListT.COLUMN_IMAGE_PATH + " = replace(" + Migration3.ListT.COLUMN_IMAGE_PATH + ", '" + oldPath + "', '" + newPath + "')");
	}
}
