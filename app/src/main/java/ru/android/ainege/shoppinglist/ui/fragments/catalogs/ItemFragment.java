package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
	private static final String STATE_SEARCH = "state_search";
	private static final String STATE_ORIGINAL_COLLAPSE = "state_original_collapse";

	private MenuItem mSearchMenu;
	private SearchView mSearchView;
	private TextView mEmptySearchTextView;
	private boolean mIsUseCategory;
	private boolean mIsLastEdit = false;
	protected ItemAdapter mAdapterRV;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapterRV = (ItemAdapter) super.mAdapterRV;

		if (savedInstanceState != null) {
			mAdapterRV.setCollapseCategoryStates((HashMap<Long, Boolean>) savedInstanceState.getSerializable(STATE_COLLAPSE));
			mAdapterRV.setOriginalCollapseCategoryStates((HashMap<Long, Boolean>) savedInstanceState.getSerializable(STATE_ORIGINAL_COLLAPSE));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		mEmptySearchTextView = (TextView) v.findViewById(R.id.empty_search);

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_COLLAPSE, mAdapterRV.getCollapseCategoryStates());

		if (mSearchMenu.isActionViewExpanded()) {
			outState.putSerializable(STATE_ORIGINAL_COLLAPSE, mAdapterRV.getOriginalCollapseCategoryStates());
			outState.putCharSequence(STATE_SEARCH, mSearchView.getQuery());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != Activity.RESULT_OK) return;

		switch (requestCode) {
			case ADD:
			case EDIT:
				mIsLastEdit = true;
				break;
		}
	}

	@Override
	public int getKey() {
		return R.string.catalogs_key_item;
	}

	@Override
	protected String getTitle(Toolbar toolbar, Bundle savedInstanceState) {
		setMenu(toolbar, savedInstanceState);
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
		if (mSearchMenu.isActionViewExpanded()) {
			mAdapterRV.setData(mCatalog, false);

			mAdapterRV.saveOriginalList();
			mAdapterRV.getFilter().filter(mSearchView.getQuery());
		} else {
			Item item = getLastEditItem(mCatalog);
			mAdapterRV.setData(mCatalog, true);

			if (item != null) {
				setScrollPosition(item);
			}

			scrollToPosition();
		}
	}

	private void setMenu(Toolbar toolbar, Bundle savedInstanceState) {
		toolbar.inflateMenu(R.menu.collapse_category);

		final Menu menu = toolbar.getMenu();
		menu.setGroupVisible(R.id.collapse_category, mIsUseCategory);

		toolbar.setOnMenuItemClickListener(onMenuItemClickListener(menu));

		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		mSearchMenu = menu.findItem(R.id.action_search);
		mSearchMenu.setVisible(true);
		mSearchView = (SearchView) mSearchMenu.getActionView();

		if (savedInstanceState != null) {
			CharSequence query = savedInstanceState.getCharSequence(STATE_SEARCH);

			if (query != null) {
				MenuItemCompat.expandActionView(mSearchMenu);
				afterExpandedSearchView(menu);
				mSearchView.setQuery(savedInstanceState.getCharSequence(STATE_SEARCH), false);
			}
		}

		mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		mSearchView.setIconifiedByDefault(false);

		mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				mAdapterRV.getFilter().filter(newText);
				return true;
			}
		});

		MenuItemCompat.setOnActionExpandListener(mSearchMenu, new MenuItemCompat.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				afterExpandedSearchView(menu);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				mAdapterRV.recoveryFromOriginalList();
				menu.setGroupVisible(R.id.collapse_category, mIsUseCategory);
				return true;
			}
		});
	}

	private void afterExpandedSearchView(Menu menu) {
		mAdapterRV.saveOriginalList();
		menu.setGroupVisible(R.id.collapse_category, false);
	}

	private Item getLastEditItem(ArrayList categories) {
		if (mLastEditId != -1) {
			for (Object c : categories) {
				for (Object i : ((Category) c).getItemsByCategories()) {
					Item item = (Item) i;

					if (item.getIdItem() == mLastEditId) {
						mAdapterRV.setCollapseCategoryStates(item.getIdCategory(), false);
						return item;
					}
				}
			}
		}

		return null;
	}

	private void setScrollPosition(Item item) {
		int itemPosition = mAdapterRV.getItemList().indexOf(item);
		mScrollToPosition = itemPosition != -1 ? itemPosition : mAdapterRV.getItemList().indexOf(item.getCategory());
	}

	private Toolbar.OnMenuItemClickListener onMenuItemClickListener (final Menu menu) {
		return new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.collapse_all:
						mAdapterRV.collapseAllCategory(true);
						return true;
					case R.id.expanded_all:
						mAdapterRV.extendAllCategory(true);
						return true;
					default:
						return false;
				}
			}
		};
	}

	private class ItemAdapter extends NestedListAdapter implements CatalogAdapter, Filterable {
		private SearchFilter mSearchFilter;
		private ArrayList mOriginalList;
		private HashMap<Long, Boolean> mOriginalCollapseCategoryStates;

		ItemAdapter(Activity activity, boolean isUseCategory) {
			super(activity);
			mIsUseCategory = isUseCategory;
		}

		@Override
		public void setData(ArrayList categoryList, boolean isUpdate) {
			mItemList = generateParentChildItemList(categoryList, true);

			if (isUpdate) {
				notifyDataSetChanged();
			}
		}

		@Override
		protected ArrayList<Object> generateParentChildItemList(List<Category> categoryList, boolean isCollapsedCategory) {
			ArrayList<Object> list = new ArrayList<>();
			HashMap<Long, Boolean> collapseStates;

			if (mSearchMenu.isActionViewExpanded()) {
				collapseStates = mOriginalCollapseCategoryStates;
			} else {
				collapseStates = mCollapseCategoryStates;
			}

			if (mIsUseCategory) {
				for (Category category : categoryList) {
					list.add(category);

					if (collapseStates.containsKey(category.getId()) &&
							!collapseStates.get(category.getId())) {
						int childListItemCount = category.getItemsByCategories().size();

						for (int j = 0; j < childListItemCount; j++) {
							list.add(category.getItemsByCategories().get(j));
						}
					} else {
						setCollapseCategoryStates(category.getId(), true);
					}
				}
			} else {
				Category category = categoryList.get(0);
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

		Item getItemById(long id, ArrayList list) {
			if (id > 0) {
				for (Object c : list) {
					if (mIsUseCategory) {
						if (c instanceof Category) {
							for (Object i : ((Category) c).getItemsByCategories()) {
								Item item = (Item) i;

								if (item.getId() == id) {
									return item;
								}
							}
						}
					} else {
						Item item = (Item) c;

						if (item.getId() == id) {
							return item;
						}
					}
				}
			}

			return null;
		}

		@Override
		public void removeItem(int position) {
			Item item = (Item) mItemList.get(position);
			removeItem(item);

			if (!item.getImagePath().contains(Image.ASSETS_IMAGE_PATH)) {
				Image.deleteFile(item.getImagePath());
			}

			if (!item.getDefaultImagePath().contains(Image.ASSETS_IMAGE_PATH)) {
				Image.deleteFile(item.getDefaultImagePath());
			}

			if (mSearchView.getQuery().length() > 0) {
				Item originalItem = getItemById(item.getId(), mOriginalList);
				mOriginalList.remove(originalItem);
				originalItem.getCategory().getItemsByCategories().remove(originalItem);

				if (originalItem.getCategory().getItemsByCategories().size() == 0) {
					mOriginalList.remove(originalItem.getCategory());
					mOriginalCollapseCategoryStates.remove(originalItem.getCategory().getId());
				}
			} else {
				if (mItemList.size() == 0) {
					mCatalogRV.setVisibility(View.GONE);
					mEmptyImage.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public Filter getFilter() {
			if (mSearchFilter == null) {
				mSearchFilter = new SearchFilter();
			}

			return mSearchFilter;
		}

		HashMap<Long, Boolean> getOriginalCollapseCategoryStates() {
			return mOriginalCollapseCategoryStates;
		}

		void setOriginalCollapseCategoryStates(HashMap<Long, Boolean> originalCollapseCategoryStates) {
			if (mOriginalCollapseCategoryStates == null && originalCollapseCategoryStates != null) {
				mOriginalCollapseCategoryStates = new HashMap<>(originalCollapseCategoryStates);
			}
		}

		void saveOriginalList() {
			mOriginalList = new ArrayList(mItemList);
			setOriginalCollapseCategoryStates(mCollapseCategoryStates);
		}

		List recoveryFromOriginalList() {
			mCollapseCategoryStates = new HashMap<>(mOriginalCollapseCategoryStates);
			mOriginalCollapseCategoryStates = null;
			return null;
		}

		class SearchFilter extends Filter {

			@Override
			protected Filter.FilterResults performFiltering(CharSequence charSequence) {
				Filter.FilterResults results = new Filter.FilterResults();

				if (charSequence == null || charSequence.length() == 0) {
					List list;

					if (mOriginalList != null) {
						list = mOriginalList;

						if (mOriginalCollapseCategoryStates != null) {
							mCollapseCategoryStates = new HashMap<>(mOriginalCollapseCategoryStates);
						}
					} else {
						list = mItemList;
					}

					results.values = list;
					results.count = list.size();
				} else {
					ArrayList filteredItems = new ArrayList();

					for (Object c : mOriginalList) {
						if (mIsUseCategory) {
							filteredItems = category(filteredItems, c, charSequence);
						} else {
							filteredItems = item(filteredItems, c, charSequence);
						}
					}

					results.values = filteredItems;
					results.count = filteredItems.size();
				}

				return results;
			}

			@Override
			protected void publishResults(CharSequence charSequence, Filter.FilterResults filterResults) {
				mItemList = (ArrayList) filterResults.values;
				notifyDataSetChanged();

				if (mItemList.isEmpty()) {
					mEmptySearchTextView.setVisibility(View.VISIBLE);
					mCatalogRV.setVisibility(View.GONE);
				} else {
					mEmptySearchTextView.setVisibility(View.GONE);
					mCatalogRV.setVisibility(View.VISIBLE);

					Item item = getItemById(mLastEditId, mItemList);

					if (mIsLastEdit && item != null) {
						mIsLastEdit = false;
						setScrollPosition(item);
					}

					scrollToPosition();
				}
			}

			private ArrayList category(ArrayList filteredItems, Object originalCategory, CharSequence charSequence) {
				if (originalCategory instanceof Category) {
					Category newCategory = new Category((Category) originalCategory);

					for (Object originalItem : ((Category) originalCategory).getItemsByCategories()) {
						Item newItem = new Item((Item) originalItem);

						if (((Item) originalItem).getName().toUpperCase().contains(charSequence.toString().toUpperCase())) {
							if (!filteredItems.contains(newCategory)) {
								filteredItems.add(newCategory);
								mCollapseCategoryStates.put(newCategory.getId(), false);
							}

							newItem.setCategory(newCategory);
							newCategory.getItemsByCategories().add(newItem);
							filteredItems.add(newItem);
						}
					}
				}

				return filteredItems;
			}

			private ArrayList item(ArrayList filteredItems, Object originalItem, CharSequence charSequence) {
				Item newItem = new Item((Item) originalItem);

				if (((Item) originalItem).getName().toUpperCase().contains(charSequence.toString().toUpperCase())) {
					filteredItems.add(newItem);
				}

				return filteredItems;
			}
		}

		class CategoryViewHolder extends CategoryVH {
			LinearLayout mCategoryContainer;
			TextView mColor;
			TextView mCategory;
			TextView mItemsCount;

			CategoryViewHolder(View v) {
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

		class ItemViewHolder extends ItemVH {
			TextView mColor;
			ImageView mImage;
			TextView mName;
			TextView mUnit;
			final ImageButton mDelete;

			ItemViewHolder(View v) {
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
