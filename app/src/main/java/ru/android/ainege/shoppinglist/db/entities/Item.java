package ru.android.ainege.shoppinglist.db.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Item extends ItemData implements Catalog {
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

	@Override
	public long getId() {
		return mId;
	}

	@Override
	public void setId(long id) {
		mId = id;
	}

	@Override
	public long getIdItem() {
		return getId();
	}

	@Override
	public void setIdItem(long id) {
		setId(id);
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public void setName(String name) {
		mName = name;
	}

	@Override
	public String toString() {
		return mName;
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

	public static void sort(ArrayList<Item> itemsInList) {
		Collections.sort(itemsInList, new Comparator<Item>() {
			@Override
			public int compare(Item lhs, Item rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});
	}
}
