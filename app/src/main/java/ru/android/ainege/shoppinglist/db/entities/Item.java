package ru.android.ainege.shoppinglist.db.entities;

import java.io.Serializable;

public class Item implements Serializable {
	private long mId;
	private String mName;
	private String mDefaultImagePath;
	private String mImagePath;
	private long mIdItemData;
	private ItemData mItemData;

	public Item(String name, String imagePath, ItemData itemData) {
		mName = name;
		mImagePath = imagePath;
		mItemData = itemData;
		if (mItemData != null) {
			mIdItemData = mItemData.getId();
		}
	}

	public Item(long id, String name, String defaultImagePath, String imagePath) {
		this(name, imagePath, null);
		mId = id;
		mDefaultImagePath = defaultImagePath;
	}

	public Item(long id, String name, String defaultImagePath, String imagePath, ItemData itemData) {
		this(id, name, defaultImagePath, imagePath);
		mItemData = itemData;
		if (mItemData != null) {
			mIdItemData = mItemData.getId();
		}
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setDefaultImagePath(String imagePath) {
		mDefaultImagePath = imagePath;
	}

	public String getDefaultImagePath() {
		return mDefaultImagePath;
	}

	public void setImagePath(String imagePath) {
		mImagePath = imagePath;
	}

	public String getImagePath() {
		return mImagePath;
	}

	public long getIdItemData() {
		return mIdItemData;
	}

	public void setIdItemData(long id) {
		mIdItemData = id;
	}

	public ItemData getItemData() {
		return mItemData;
	}

	public void setItemData(ItemData itemData) {
		mItemData = itemData;
		if (mItemData != null) {
			mIdItemData = mItemData.getId();
		}
	}
}
