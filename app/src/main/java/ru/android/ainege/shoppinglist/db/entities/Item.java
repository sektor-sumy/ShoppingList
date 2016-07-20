package ru.android.ainege.shoppinglist.db.entities;

public class Item extends ItemData {
	private long mId;
	private String mName;
	private String mDefaultImagePath;
	private String mImagePath;

	public Item() {}

	public Item(Item item) {
		super(item);
		mId = item.getId();
		mName = item.getName();
		mDefaultImagePath = item.getDefaultImagePath();
		mImagePath = item.getImagePath();
	}

	public Item(String name, String defaultImagePath) {
		mName = name;
		mDefaultImagePath = defaultImagePath;
	}

	public Item(String name, String defaultImagePath, String imagePath) {
		this(name, defaultImagePath);
		mImagePath = imagePath;
	}

	public Item(long id, String name, String defaultImagePath, String imagePath, long idData) {
		this(name, defaultImagePath, imagePath);
		mId = id;
		mIdItemData = idData;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getDefaultImagePath() {
		return mDefaultImagePath;
	}

	public void setDefaultImagePath(String imagePath) {
		mDefaultImagePath = imagePath;
	}

	public String getImagePath() {
		return mImagePath;
	}

	public void setImagePath(String imagePath) {
		mImagePath = imagePath;
	}

	public boolean isNew() {
		return mId == 0;
	}
}
