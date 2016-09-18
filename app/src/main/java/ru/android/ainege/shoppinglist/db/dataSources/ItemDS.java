package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.db.TableInterface.CategoriesInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.ItemDataInterface;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.Unit;

import static ru.android.ainege.shoppinglist.db.TableInterface.*;

public class ItemDS extends CatalogDS<Item> implements ItemsInterface {

	public ItemDS(Context context) {
		super(context);
	}

	@Override
	public ItemCursor getAll() {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + TABLE_NAME + ".*, " +
				ItemDataInterface.COLUMN_AMOUNT + ", " +
				ItemDataInterface.COLUMN_ID_UNIT + ", " +
				ItemDataInterface.COLUMN_PRICE + ", " +
				ItemDataInterface.COLUMN_ID_CATEGORY + ", " +
				ItemDataInterface.COLUMN_COMMENT + ", " +
				UnitsInterface.COLUMN_NAME + ", " +
				CategoriesInterface.COLUMN_NAME + ", " +
				CategoriesInterface.COLUMN_COLOR +
				" FROM " + TABLE_NAME +
				" INNER JOIN " + ItemDataInterface.TABLE_NAME + " ON " +
				TABLE_NAME + "." + COLUMN_ID_DATA + " = " + ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID +
				" INNER JOIN " + UnitsInterface.TABLE_NAME + " ON " +
				ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID_UNIT + " = " + UnitsInterface.TABLE_NAME + "." + UnitsInterface.COLUMN_ID +
				" INNER JOIN " + CategoriesInterface.TABLE_NAME + " ON " +
				ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID_CATEGORY + " = " + CategoriesInterface.TABLE_NAME + "." + CategoriesInterface.COLUMN_ID,
				null);

		return new ItemCursor(cursor);
	}

	@Override
	public ItemCursor getAll(long withoutId) {
		return null;
	}

	@Override
	public boolean isUsed(long id) {
		return false;
	}

	public ListsDS.ListCursor isUsedInLists(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + ListsInterface.TABLE_NAME + ".*" +
				" FROM " + ShoppingListsInterface.TABLE_NAME + " INNER JOIN " +
				ListsInterface.TABLE_NAME + " ON " +
				ShoppingListsInterface.TABLE_NAME + "." + ShoppingListsInterface.COLUMN_ID_LIST + " = " +
				ListsInterface.TABLE_NAME + "." + ListsInterface.COLUMN_ID +
				" WHERE " + ShoppingListsInterface.COLUMN_ID_ITEM + " =  ?",  new String[] { String.valueOf(id) });

		return new ListsDS.ListCursor(cursor);
	}

	public Cursor getNames(String substring) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return db.rawQuery("SELECT " + TABLE_NAME + ".*, " +
				CategoriesInterface.TABLE_NAME + "." + CategoriesInterface.COLUMN_COLOR +
				" FROM " + TABLE_NAME +
				" INNER JOIN " + ItemDataInterface.TABLE_NAME +
				" ON " + TABLE_NAME + "." + COLUMN_ID_DATA + " = " +
				ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID +
				" INNER JOIN " + CategoriesInterface.TABLE_NAME +
				" ON " + ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID_CATEGORY + " = " +
				CategoriesInterface.TABLE_NAME + "." + CategoriesInterface.COLUMN_ID +
				" WHERE " + COLUMN_NAME + " LIKE ?", new String[] {"%" + substring + "%"});
	}

	public ItemCursor getWithData(long id) {
		return getFullData(" WHERE " + TABLE_NAME + "." + COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	public ItemCursor getWithData(String name) {
		return getFullData(" WHERE " + TABLE_NAME + "." + COLUMN_NAME + " LIKE ?", new String[]{name});
	}

	private ItemCursor getFullData(String where, String[] params) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + TABLE_NAME + ".*, " +
				ItemDataInterface.COLUMN_AMOUNT + ", " +
				ItemDataInterface.COLUMN_ID_UNIT + ", " +
				ItemDataInterface.COLUMN_PRICE + ", " +
				ItemDataInterface.COLUMN_ID_CATEGORY + ", " +
				ItemDataInterface.COLUMN_COMMENT +
				" FROM " + TABLE_NAME + " INNER JOIN " + ItemDataInterface.TABLE_NAME + " ON " +
				TABLE_NAME + "." + COLUMN_ID_DATA + " = " + ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID +
				where, params);

		return new ItemCursor(cursor);
	}

	@Override
	public int update(Item item) {
		new ItemDataDS(mContext).update(item);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(item);

		return db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
	}

	@Override
	public long add(Item item) {
		long idData = new ItemDataDS(mContext).add(item);
		item.setIdItemData(idData);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(item);

		return db.insert(TABLE_NAME, null, values);
	}

	@Override
	public void delete(long id) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(TABLE_NAME, COLUMN_ID + " = ? ", new String[]{String.valueOf(id)});
	}

	@Override
	public void delete(long id, long newId) {

	}

	private ContentValues createContentValues(Item item) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME, item.getName());
		values.put(COLUMN_DEFAULT_IMAGE_PATH, item.getDefaultImagePath());
		values.put(COLUMN_IMAGE_PATH, item.getImagePath());
		values.put(COLUMN_ID_DATA, item.getIdItemData());

		return values;
	}

	public static class ItemCursor extends CatalogCursor<Item> {

		public ItemCursor(Cursor cursor) {
			super(cursor);
		}

		public ArrayList<Category> getEntities (ArrayList<Category> categories){
			ArrayList<Category> res = new ArrayList<>();
			ArrayList<Item> items = getEntities();
			Item.sort(items);

			for (Item item : items) {
				for (Category category : categories) {
					if (category.getId() == item.getIdCategory()) {
						item.setCategory(category);
						category.addItem(item);
						break;
					}
				}
			}

			for (Category category : categories) {
				if (category.getItemsByCategories().size() > 0) {
					res.add(category);
				}
			}

			return res;
		}

		public Item getEntity() {
			long id = getLong(getColumnIndex(COLUMN_ID));
			String name = getString(getColumnIndex(COLUMN_NAME));
			String defaultImagePath = getString(getColumnIndex(COLUMN_DEFAULT_IMAGE_PATH));
			String imagePath = getString(getColumnIndex(COLUMN_IMAGE_PATH));
			long idData = getLong(getColumnIndex(COLUMN_ID_DATA));

			Item item = new Item(id, name, defaultImagePath, imagePath, idData);

			if (getColumnIndex(ItemDataInterface.COLUMN_AMOUNT) != -1) {
				item.setAmount(getDouble(getColumnIndex(ItemDataInterface.COLUMN_AMOUNT)));
				item.setPrice(getDouble(getColumnIndex(ItemDataInterface.COLUMN_PRICE)));
				item.setComment(getString(getColumnIndex(ItemDataInterface.COLUMN_COMMENT)));

				long idUnit = getLong(getColumnIndex(ItemDataInterface.COLUMN_ID_UNIT));
				if (getColumnIndex(UnitsInterface.COLUMN_NAME) != -1) {
					String unitName = getString(getColumnIndex(UnitsInterface.COLUMN_NAME));
					item.setUnit(new Unit(idUnit, unitName));
				} else {
					item.setIdUnit(idUnit);
				}

				long idCategory = getLong(getColumnIndex(ItemDataInterface.COLUMN_ID_CATEGORY));
				if (getColumnIndex(CategoriesInterface.COLUMN_NAME) != -1) {
					String categoryName = getString(getColumnIndex(CategoriesInterface.COLUMN_NAME));
					int color = getInt(getColumnIndex(CategoriesInterface.COLUMN_COLOR));
					item.setCategory(new Category(idCategory, categoryName, color));
				} else {
					item.setIdCategory(idCategory);
				}
			}

			return item;
		}
	}
}
