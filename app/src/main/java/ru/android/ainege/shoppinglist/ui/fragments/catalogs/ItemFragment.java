package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.adapter.NestedListAdapter;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.DeleteDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.item.AddItemDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.item.EditItemDialogFragment;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.MultiSelection;

public class ItemFragment extends CatalogFragment<Item>{
	private static final String STATE_COLLAPSE = "state_collapse";

	private boolean mIsUseCategory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			((ItemAdapter) mAdapterRV).setCollapseCategoryStates((HashMap<Long, Boolean>) savedInstanceState.getSerializable(STATE_COLLAPSE));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_COLLAPSE, ((ItemAdapter) mAdapterRV).getCollapseCategoryStates());
	}

	@Override
	public int getKey() {
		return R.string.catalogs_key_item;
	}

	@Override
	protected String getTitle(Toolbar toolbar) {
		if (mIsUseCategory) {
			toolbar.inflateMenu(R.menu.shopping_list_menu);
			toolbar.setOnMenuItemClickListener(onMenuItemClickListener());
		}
		return getString(R.string.catalogs_items);
	}

	@Override
	protected CatalogAdapter getAdapter() {
		SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mIsUseCategory = mSharedPref.getBoolean(getString(R.string.settings_key_use_category), true);

		return new ItemAdapter(getActivity(), mIsUseCategory);
	}

	@Override
	protected CatalogDS getDS() {
		return new ItemDS(getActivity());
	}

	@Override
	protected GeneralDialogFragment getAddDialog() {
		return new AddItemDialogFragment();
	}

	@Override
	protected GeneralDialogFragment getEditDialog() {
		return new EditItemDialogFragment();
	}

	@Override
	protected ArrayList getCatalog(Cursor data) {
		ArrayList<Category> categories = new ArrayList<>();

		if (mIsUseCategory) {
			CategoriesDS.CategoryCursor categoryCursor = new CategoriesDS(getActivity()).getAll();
			categories = ((ItemDS.ItemCursor) data).getEntities(categoryCursor.getEntities());
		} else {
			ArrayList<Item> itemsInList = ((ItemDS.ItemCursor) data).getEntities();
			Item.sort(itemsInList);

			categories.add(new Category(itemsInList));

			for (Item item : itemsInList) {
				item.getCategory().setItemsByCategories(itemsInList);
			}
		}

		return categories;
	}

	protected void loadData() {
		Item item = getLastEditItem(mCatalog);
		mAdapterRV.setData(mCatalog);

		if (item != null) {
			setScrollPosition(item);
		}

		if (mScrollToPosition != -1) {
			mCatalogRV.scrollToPosition(mScrollToPosition);
		}
	}

	private Item getLastEditItem(ArrayList categories) {
		if (mLastEditId != -1) {
			for (Object c : categories) {
				for (Object i : ((Category) c).getItemsByCategories()) {
					Item item = (Item) i;

					if (item.getIdItem() == mLastEditId) {
						((ItemAdapter) mAdapterRV).setCollapseCategoryStates(item.getIdCategory(), false);
						return item;
					}
				}
			}
		}

		return null;
	}

	private void setScrollPosition(Item item) {
		int itemPosition = ((ItemAdapter) mAdapterRV).getItemList().indexOf(item);
		mScrollToPosition = itemPosition != -1 ? itemPosition : ((ItemAdapter) mAdapterRV).getItemList().indexOf(item.getCategory());
	}

	private Toolbar.OnMenuItemClickListener onMenuItemClickListener () {
		return new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.collapse_all:
						((ItemAdapter) mAdapterRV).collapseAllCategory(true);
						return true;
					case R.id.expanded_all:
						((ItemAdapter) mAdapterRV).extendAllCategory(true);
						return true;
					default:
						return false;
				}
			}
		};
	}

	private class ItemAdapter extends NestedListAdapter implements CatalogAdapter {

		public ItemAdapter(Activity activity, boolean isUseCategory) {
			super(activity);
			mIsUseCategory = isUseCategory;
		}

		@Override
		public void setData(ArrayList categoryList) {
			mItemList = generateParentChildItemList(categoryList, true);
			notifyDataSetChanged();
		}

		@Override
		protected List<Object> generateParentChildItemList(List<Category> categoryList, boolean isCollapsedCategory) {
			List<Object> list = new ArrayList<>();
			Category category;

			if (mIsUseCategory) {
				for (int i = 0; i < categoryList.size(); i++) {
					category = categoryList.get(i);
					list.add(category);

					if (mCollapseCategoryStates.containsKey(category.getId()) &&
							!mCollapseCategoryStates.get(category.getId())) {
						int childListItemCount = category.getItemsByCategories().size();

						for (int j = 0; j < childListItemCount; j++) {
							list.add(category.getItemsByCategories().get(j));
						}
					} else {
						setCollapseCategoryStates(category.getId(), true);
					}
				}
			} else {
				category = categoryList.get(0);
				int childListItemCount = category.getItemsByCategories().size();

				for (int j = 0; j < childListItemCount; j++) {
					list.add(category.getItemsByCategories().get(j));
				}
			}

			return list;
		}

		@Override
		protected CategoryViewHolder onCreateCategoryViewHolder(ViewGroup parent) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_shopping_lists_category, parent, false);
			return new CategoryViewHolder(v);
		}

		@Override
		protected ItemViewHolder onCreateItemViewHolder(ViewGroup parent) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_catalog_item, parent, false);
			return new ItemViewHolder(v);
		}

		@Override
		protected void onBindCategoryViewHolder(CategoryVH viewHolder, int position, Category category) {
			CategoryViewHolder holder = (CategoryViewHolder) viewHolder;

			if (mIsUseCategory) {
				holder.mCategoryContainer.setVisibility(View.VISIBLE);
				holder.mColor.setBackgroundColor(category.getColor());
				holder.mCategory.setText(category.getName());
				holder.mItemsCount.setText(String.valueOf(category.getItemsByCategories().size()));
			} else {
				holder.mCategoryContainer.setVisibility(View.GONE);
			}
		}

		@Override
		protected void onBindItemViewHolder(ItemVH viewHolder, int position, ItemData itemInList) {
			ItemViewHolder holder = (ItemViewHolder) viewHolder;
			Item item = (Item) itemInList;

			if (mIsUseCategory) {
				holder.mColor.setVisibility(View.VISIBLE);
				holder.mColor.setBackgroundColor(item.getCategory().getColor());
			} else {
				holder.mColor.setVisibility(View.GONE);
			}

			Image.create().insertImageToView(mActivity, item.getImagePath(), holder.mImage);
			holder.mImage.setSelected(MultiSelection.getInstance().isContains(item));
			holder.mName.setText(item.getName());
			holder.mUnit.setText(item.getUnit().getName());

			holder.itemView.setSelected(MultiSelection.getInstance().isContains(itemInList));
		}

		@Override
		protected CatalogDS<Item> getDS() {
			return new ItemDS(mActivity);
		}

		@Override
		public void removeItem(int position) {
			removeItem((Item) mItemList.get(position));

			if (mItemList.size() == 0) {
				mCatalogRV.setVisibility(View.GONE);
				mEmptyImage.setVisibility(View.VISIBLE);
			}
		}

		public class CategoryViewHolder extends CategoryVH {
			public LinearLayout mCategoryContainer;
			public TextView mColor;
			public TextView mCategory;
			public TextView mItemsCount;

			public CategoryViewHolder(View v) {
				super(v);
				mCategoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
				mColor = (TextView) v.findViewById(R.id.color);
				mCategory = (TextView) v.findViewById(R.id.category);
				mItemsCount = (TextView) v.findViewById(R.id.sum_category);

				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						setOnClick(getAdapterPosition());
					}
				});
			}
		}

		public class ItemViewHolder extends ItemVH {
			public TextView mColor;
			public ImageView mImage;
			public TextView mName;
			public TextView mUnit;
			public final ImageButton mDelete;

			public ItemViewHolder(View v) {
				super(v);

				mColor = (TextView) v.findViewById(R.id.color);
				mImage = (ImageView) v.findViewById(R.id.image);
				mName = (TextView) v.findViewById(R.id.name);
				mUnit = (TextView) v.findViewById(R.id.unit);
				mDelete = (ImageButton) v.findViewById(R.id.delete);

				mDelete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						Item d = (Item) mItemList.get(itemPosition);

						ListsDS.ListCursor cursor = ((ItemDS) getDS()).isUsedInLists(d.getId());

						if (cursor.moveToFirst()) {
							ArrayList lists = cursor.getEntities();

							DeleteDialogFragment dialogFrag = DeleteDialogFragment.newInstance(itemPosition, d, lists);
							dialogFrag.setTargetFragment(ItemFragment.this, DELETE);
							dialogFrag.show(getFragmentManager(), DELETE_DATE);
						} else {
							deleteItem(itemPosition, d.getId(), -1);
						}
					}
				});

				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showEditDialog((Item) mItemList.get(getAdapterPosition()));
					}
				});
			}
		}
	}
}
