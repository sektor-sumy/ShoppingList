package ru.android.ainege.shoppinglist.db.dataSources;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import ru.android.ainege.shoppinglist.db.TableInterface.*;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.entities.Unit;

public class ShoppingListDS extends GenericDS<ShoppingList> implements ShoppingListsInterface {
	private static final String DEFAULT_ITEM_DATA = "id_default_data";

	private String selectItemsQuery = "SELECT " + TABLE_NAME + ".*, " +
			ItemsInterface.COLUMN_NAME + ", " + ItemsInterface.COLUMN_DEFAULT_IMAGE_PATH + ", " +
			ItemsInterface.COLUMN_IMAGE_PATH + ", " +
			ItemsInterface.TABLE_NAME + "." + ItemsInterface.COLUMN_ID_DATA + " AS " + DEFAULT_ITEM_DATA + ", " +
			ItemDataInterface.TABLE_NAME + ".*, " +
			UnitsInterface.COLUMN_NAME + ", " + CategoriesInterface.COLUMN_NAME + ", " +
			CategoriesInterface.COLUMN_COLOR +
			" FROM " + TABLE_NAME +
			" INNER JOIN " + ItemsInterface.TABLE_NAME + " ON " +
			TABLE_NAME + "." + COLUMN_ID_ITEM + " = " + ItemsInterface.TABLE_NAME + "." + ItemsInterface.COLUMN_ID +
			" INNER JOIN " + ItemDataInterface.TABLE_NAME + " ON " +
			TABLE_NAME + "." + COLUMN_ID_DATA + " = " + ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID +
			" INNER JOIN " + UnitsInterface.TABLE_NAME + " ON " +
			ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID_UNIT + " = " + UnitsInterface.TABLE_NAME + "." + UnitsInterface.COLUMN_ID +
			" INNER JOIN " + CategoriesInterface.TABLE_NAME + " ON " +
			ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID_CATEGORY + " = " + CategoriesInterface.TABLE_NAME + "." + CategoriesInterface.COLUMN_ID +
			" WHERE " + TABLE_NAME + "." + COLUMN_ID_LIST + " = ? ";


	public ShoppingListDS(Context context) {
		super(context);
	}

	@Override
	public EntityCursor<ItemData> getAll() {
		return null;
	}

	public ShoppingListCursor get(long idList, long idItem) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		String selectQuery = selectItemsQuery +
				" AND " + TABLE_NAME + "." + COLUMN_ID_ITEM + " = ? " ;
		Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(idList), String.valueOf(idItem)});
		return new ShoppingListCursor(cursor);
	}

	public ShoppingListCursor getItemsInList(long id) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectItemsQuery, new String[]{String.valueOf(id)});
		return new ShoppingListCursor(cursor);
	}

	public ShoppingListCursor getByName(String name, long idList) {
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + TABLE_NAME + ".*, " +
				ItemsInterface.COLUMN_NAME + ", " +
				ItemsInterface.COLUMN_DEFAULT_IMAGE_PATH + ", " +
				ItemsInterface.COLUMN_IMAGE_PATH + ", " +
				ItemDataInterface.COLUMN_ID_CATEGORY + " AS " + ItemDataInterface.COLUMN_ID_CATEGORY + "Item , " +
				ItemsInterface.TABLE_NAME + "." + ItemsInterface.COLUMN_ID_DATA + " AS " + DEFAULT_ITEM_DATA +
				" FROM " + TABLE_NAME + " INNER JOIN " + ItemsInterface.TABLE_NAME +
				" ON " + TABLE_NAME + "." + COLUMN_ID_ITEM + " = " + ItemsInterface.TABLE_NAME + "." + ItemsInterface.COLUMN_ID +
				" INNER JOIN " + ItemDataInterface.TABLE_NAME +
				" ON " + ItemsInterface.TABLE_NAME + "." + ItemsInterface.COLUMN_ID + " = " + ItemDataInterface.TABLE_NAME + "." + ItemDataInterface.COLUMN_ID +
				" WHERE " + ItemsInterface.COLUMN_NAME + " LIKE '" + name +
				"' AND " + COLUMN_ID_LIST + " = " + idList, null);
		return new ShoppingListCursor(cursor);
	}

	public void setIsBought(boolean isBought, long idItem, long idList) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(isBought);
		db.update(TABLE_NAME,
				values,
				COLUMN_ID_ITEM + " = ? AND " + COLUMN_ID_LIST + " = ?",
				new String[]{String.valueOf(idItem), String.valueOf(idList)});
	}

	@Override
	public int update(ShoppingList shoppingList) {
		return update(shoppingList, shoppingList.getIdItem());
	}

	public int update(ShoppingList shoppingList, long idOldItem) {
		new ItemDataDS(mContext).update(shoppingList);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(shoppingList.isBought());
		values.put(COLUMN_ID_ITEM, shoppingList.getIdItem());
		return db.update(TABLE_NAME, values,
				COLUMN_ID_ITEM + " = ? AND " + COLUMN_ID_LIST + " = ?",
				new String[]{String.valueOf(idOldItem), String.valueOf(shoppingList.getIdList())});
	}

	@Override
	public long add(ShoppingList shoppingList) {
		long idData = new ItemDataDS(mContext).add(shoppingList);
		shoppingList.setIdItemData(idData);

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = createContentValues(shoppingList);
		return db.insert(TABLE_NAME, null, values);
	}

	@Override
	public void delete(long idItemData) {
		new ItemDataDS(mContext).delete(idItemData);
	}

	private ContentValues createContentValues(boolean isBought) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_IS_BOUGHT, isBought);
		return values;
	}

	private ContentValues createContentValues(ShoppingList shoppingList) {
		ContentValues values = createContentValues(shoppingList.isBought());
		values.put(COLUMN_ID_ITEM, shoppingList.getIdItem());
		values.put(COLUMN_ID_LIST, shoppingList.getIdList());
		values.put(COLUMN_ID_DATA, shoppingList.getIdItemData());
		values.put(COLUMN_DATE, System.currentTimeMillis() / 1000);
		return values;
	}

	public static class ShoppingListCursor extends EntityCursor<ShoppingList> {
		public ShoppingListCursor(Cursor cursor) {
			super(cursor);
		}

		public ShoppingList getEntity() {
			long idItem = getLong(getColumnIndex(COLUMN_ID_ITEM));
			long idList = getLong(getColumnIndex(COLUMN_ID_LIST));
			long idData = getLong(getColumnIndex(COLUMN_ID_DATA));
			boolean isBought = getInt(getColumnIndex(COLUMN_IS_BOUGHT)) == 1;
			long date = getLong(getColumnIndex(COLUMN_DATE));

			ShoppingList shoppingList = new ShoppingList(idItem, idList, isBought, idData, new Date(date));

			if (getColumnIndex(ItemsInterface.COLUMN_NAME) != -1) {
				String nameItem = getString(getColumnIndex(ItemsInterface.COLUMN_NAME));
				String defaultImage = getString(getColumnIndex(ItemsInterface.COLUMN_DEFAULT_IMAGE_PATH));
				String image = getString(getColumnIndex(ItemsInterface.COLUMN_IMAGE_PATH));
				long idItemData = getLong(getColumnIndex(DEFAULT_ITEM_DATA));

				Item item = new Item(idItem, nameItem, defaultImage, image, idItemData);
				if (getColumnIndex(ItemDataInterface.COLUMN_ID_CATEGORY + "Item") != -1) {
					item.setIdCategory(getLong(getColumnIndex(ItemDataInterface.COLUMN_ID_CATEGORY + "Item")));
				}
				shoppingList.setItem(item);
			}

			if (getColumnIndex(ItemDataInterface.COLUMN_AMOUNT) != -1) {
				shoppingList.setAmount(getDouble(getColumnIndex(ItemDataInterface.COLUMN_AMOUNT)));
				shoppingList.setPrice(getDouble(getColumnIndex(ItemDataInterface.COLUMN_PRICE)));
				shoppingList.setComment(getString(getColumnIndex(ItemDataInterface.COLUMN_COMMENT)));

				long idUnit = getLong(getColumnIndex(ItemDataInterface.COLUMN_ID_UNIT));
				if (getColumnIndex(UnitsInterface.COLUMN_NAME) != -1) {
					String unitName = getString(getColumnIndex(UnitsInterface.COLUMN_NAME));
					shoppingList.setUnit(new Unit(idUnit, unitName));
				} else {
					shoppingList.setIdUnit(idUnit);
				}

				long idCategory = getLong(getColumnIndex(ItemDataInterface.COLUMN_ID_CATEGORY));
				if (getColumnIndex(CategoriesInterface.COLUMN_NAME) != -1) {
					String categoryName = getString(getColumnIndex(CategoriesInterface.COLUMN_NAME));
					int color = getInt(getColumnIndex(CategoriesInterface.COLUMN_COLOR));
					shoppingList.setCategory(new Category(idCategory, categoryName, color));
				} else {
					shoppingList.setIdCategory(idCategory);
				}
			}

			return shoppingList;
		}
	}
}
