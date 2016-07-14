package ru.android.ainege.shoppinglist.db;

import ru.android.ainege.shoppinglist.db.migration.Migration3;

public interface TableInterface {
	interface UnitsInterface {
		String TABLE_NAME = Migration3.UnitT.TABLE_NAME;

		String COLUMN_ID = Migration3.UnitT.COLUMN_ID;
		String COLUMN_NAME = Migration3.UnitT.COLUMN_NAME;
	}

	interface CurrenciesInterface {
		String TABLE_NAME = Migration3.CurrencyT.TABLE_NAME;

		String COLUMN_ID = Migration3.CurrencyT.COLUMN_ID;
		String COLUMN_NAME = Migration3.CurrencyT.COLUMN_NAME;
		String COLUMN_SYMBOL = Migration3.CurrencyT.COLUMN_SYMBOL;
	}

	interface CategoriesInterface {
		String TABLE_NAME = Migration3.CategoryT.TABLE_NAME;

		String COLUMN_ID = Migration3.CategoryT.COLUMN_ID;
		String COLUMN_NAME = Migration3.CategoryT.COLUMN_NAME;
		String COLUMN_COLOR = Migration3.CategoryT.COLUMN_COLOR;
	}

	interface ListsInterface {
		String TABLE_NAME = Migration3.ListT.TABLE_NAME;

		String COLUMN_ID = Migration3.ListT.COLUMN_ID;
		String COLUMN_NAME = Migration3.ListT.COLUMN_NAME;
		String COLUMN_ID_CURRENCY = Migration3.ListT.COLUMN_ID_CURRENCY;
		String COLUMN_IMAGE_PATH = Migration3.ListT.COLUMN_IMAGE_PATH;
	}

	interface ItemDataInterface {
		String TABLE_NAME = Migration3.ItemDataT.TABLE_NAME;

		String COLUMN_ID = Migration3.ItemDataT.COLUMN_ID;
		String COLUMN_AMOUNT = Migration3.ItemDataT.COLUMN_AMOUNT;
		String COLUMN_ID_UNIT = Migration3.ItemDataT.COLUMN_ID_UNIT;
		String COLUMN_PRICE = Migration3.ItemDataT.COLUMN_PRICE;
		String COLUMN_ID_CATEGORY = Migration3.ItemDataT.COLUMN_ID_CATEGORY;
		String COLUMN_COMMENT = Migration3.ItemDataT.COLUMN_COMMENT;
	}

	interface ItemsInterface {
		String TABLE_NAME = Migration3.ItemT.TABLE_NAME;

		String COLUMN_ID = Migration3.ItemT.COLUMN_ID;
		String COLUMN_NAME = Migration3.ItemT.COLUMN_NAME;
		String COLUMN_IMAGE_PATH = Migration3.ItemT.COLUMN_IMAGE_PATH;
		String COLUMN_DEFAULT_IMAGE_PATH = Migration3.ItemT.COLUMN_DEFAULT_IMAGE_PATH;
		String COLUMN_ID_DATA = Migration3.ItemT.COLUMN_ID_DATA;
	}

	interface ShoppingListsInterface {
		String TABLE_NAME = Migration3.ShoppingListT.TABLE_NAME;

		String COLUMN_ID_ITEM = Migration3.ShoppingListT.COLUMN_ID_ITEM;
		String COLUMN_ID_LIST = Migration3.ShoppingListT.COLUMN_ID_LIST;
		String COLUMN_ID_DATA = Migration3.ShoppingListT.COLUMN_ID_DATA;
		String COLUMN_IS_BOUGHT = Migration3.ShoppingListT.COLUMN_IS_BOUGHT;
		String COLUMN_DATE = Migration3.ShoppingListT.COLUMN_DATE;
	}
}

