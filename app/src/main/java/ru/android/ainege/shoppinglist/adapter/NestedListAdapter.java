package ru.android.ainege.shoppinglist.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.android.ainege.shoppinglist.db.dataSources.GenericDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.util.MultiSelection;

public abstract class NestedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int TYPE_CATEGORY = 0;
	private static final int TYPE_ITEM = 1;

	protected HashMap<Long, Boolean> mCollapseCategoryStates = new HashMap<>();
	protected HashMap<Long, Boolean> mOriginalCollapseCategoryStates;
	protected ArrayList<Object> mItemList = new ArrayList<>();

	protected Activity mActivity;
	protected boolean mIsUseCategory;

	protected abstract List<Object> generateParentChildItemList(List<Category> categoryList, boolean isCollapsedCategory);
	protected abstract RecyclerView.ViewHolder onCreateCategoryViewHolder(ViewGroup parent);
	protected abstract RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent);
	protected abstract void onBindCategoryViewHolder(CategoryVH holder, int position, Category category);
	protected abstract void onBindItemViewHolder(ItemVH holder, int position, ItemData itemInList);
	protected abstract GenericDS getDS();

	protected abstract class CategoryVH extends RecyclerView.ViewHolder {

		public CategoryVH(View itemView) {
			super(itemView);
		}
	}
	protected abstract class ItemVH extends RecyclerView.ViewHolder {

		public ItemVH(View itemView) {
			super(itemView);
		}
	}

	public NestedListAdapter(Activity activity) {
		mActivity = activity;
	}

	@Override
	public int getItemViewType(int position) {
		Object listItem = getListItem(position);

		if (listItem instanceof Category) {
			return TYPE_CATEGORY;
		} else if (listItem instanceof ItemData) {
			return TYPE_ITEM;
		} else {
			throw new IllegalStateException("Null object added");
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_CATEGORY) {
			return onCreateCategoryViewHolder(parent);
		} else if (viewType == TYPE_ITEM) {
			return onCreateItemViewHolder(parent);
		} else {
			throw new IllegalStateException("Incorrect ViewType found");
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		Object listItem = getListItem(position);

		if (listItem instanceof Category) {
			onBindCategoryViewHolder((CategoryVH) holder, position, (Category) listItem);
		} else if (listItem instanceof ItemData) {
			onBindItemViewHolder((ItemVH) holder, position, (ItemData) listItem);
		} else {
			throw new IllegalStateException("Incorrect ViewHolder found");
		}
	}

	@Override
	public int getItemCount() {
		return mItemList != null ? mItemList.size() : 0;
	}

	public Object getListItem(int position) {
		return mItemList.get(position);
	}

	public ArrayList<Object> getItemList() {
		return mItemList;
	}

	public void removeItem(ItemData item) {
		int position = mItemList.indexOf(item);
		mItemList.remove(position);
		item.getCategory().getItemsByCategories().remove(item);
		notifyItemRemoved(position);

		int categoryPosition = mItemList.indexOf(item.getCategory());

		if (categoryPosition != -1) {
			if (item.getCategory().getItemsByCategories().size() == 0) {
				mItemList.remove(categoryPosition);
				mCollapseCategoryStates.remove(item.getCategory().getId());

				if (mOriginalCollapseCategoryStates != null) {
					mOriginalCollapseCategoryStates.remove(item.getCategory().getId());
				}

				notifyItemRemoved(categoryPosition);
			} else {
				notifyItemChanged(categoryPosition);
			}
		}
	}

	//<editor-fold desc="Extend/collapse category">
	public void setOnClick(int position) {
		Category category = (Category) getListItem(position);

		if (mCollapseCategoryStates.get(category.getId())) {
			setCollapseCategoryStates(category.getId(), false);
			extendCategory(category, position);
		} else {
			setCollapseCategoryStates(category.getId(), true);
			collapseCategory(category, position);
		}
	}

	private void collapseCategory(Category category, int position) {
		List<ItemData> itemInList = category.getItemsByCategories();

		if (itemInList != null) {
			for (int i = itemInList.size() + position; i > position; i--) {
				mItemList.remove(i);
				notifyItemRemoved(i);
			}
		}
	}

	private void extendCategory(Category category, int position){
		List<ItemData> itemInList = category.getItemsByCategories();

		if (itemInList != null) {
			for (int i = 0; i < itemInList.size(); i++) {
				mItemList.add(position + i + 1, itemInList.get(i));
				notifyItemInserted(position + i + 1);
			}
		}
	}

	public void extendAllCategory() {
		for (int i = 0; i < mItemList.size(); i++) {
			if (mItemList.get(i) instanceof Category && mCollapseCategoryStates.get(((Category) mItemList.get(i)).getId())) {
				extendCategory((Category) mItemList.get(i), i);
				setCollapseCategoryStates(((Category) mItemList.get(i)).getId(), false);
			}
		}
	}

	public void collapseAllCategory() {
		for (int i = mItemList.size() - 1; i >= 0; i--) {
			if (mItemList.get(i) instanceof Category && !mCollapseCategoryStates.get(((Category) mItemList.get(i)).getId())) {
				collapseCategory((Category) mItemList.get(i), i);
				setCollapseCategoryStates(((Category) mItemList.get(i)).getId(), true);
			}
		}
	}

	public HashMap<Long, Boolean> getOriginalCollapseCategoryStates() {
		return mOriginalCollapseCategoryStates;
	}

	public void setOriginalCollapseCategoryStates(HashMap<Long, Boolean> originalCollapseCategoryStates) {
		mOriginalCollapseCategoryStates = originalCollapseCategoryStates;
	}

	public void saveOriginalCollapseCategory() {
		mOriginalCollapseCategoryStates = new HashMap<>(mCollapseCategoryStates);
	}

	public void recoveryCollapseAllCategory() {
		mCollapseCategoryStates = new HashMap<>(mOriginalCollapseCategoryStates);
		mOriginalCollapseCategoryStates = null;

		for (int i = mItemList.size() - 1; i >= 0; i--) {
			if (mItemList.get(i) instanceof Category && mCollapseCategoryStates.get(((Category) mItemList.get(i)).getId())) {
				collapseCategory((Category) mItemList.get(i), i);
			} else {
				if (mItemList.get(i) instanceof ItemData && MultiSelection.getInstance().getSelectedItems().contains(mItemList.get(i))) {
					notifyItemChanged(i);
				}
			}
		}
	}

	public void setCollapseCategoryStates(long idCategory, boolean isCollapsed) {
		mCollapseCategoryStates.put(idCategory, isCollapsed);
	}

	public void setCollapseCategoryStates(HashMap<Long, Boolean> collapseState) {
		mCollapseCategoryStates = collapseState;
	}

	public HashMap<Long, Boolean> getCollapseCategoryStates() {
		return mCollapseCategoryStates;
	}

	//</editor-fold>

	//<editor-fold desc="Selection">
	public ArrayList<ItemData> getSelectedItems() {
		return MultiSelection.getInstance().getSelectedItems();
	}

	public void setSelectedItems(ArrayList<ItemData> selectedItems) {
		MultiSelection.getInstance().setSelectedItems(selectedItems);
	}

	public boolean isContainsInSelected(long id) {
		for (ItemData item : getSelectedItems()) {
			if (item.getIdItem() == id) {
				return true;
			}
		}

		return false;
	}

	public void selectItem(ItemData item) {
		MultiSelection.getInstance().toggleSelection(item);
		notifyItemChanged(mItemList.indexOf(item));
	}

	public void selectAllItemsInCategory(Category category) {
		MultiSelection.getInstance().selectAllItemsInCategory(category.getItemsByCategories());
		notifyItemRangeChanged(mItemList.indexOf(category) + 1, category.getItemsByCategories().size());
	}

	public void selectAllItems(boolean isBought) {
		MultiSelection.getInstance().selectAllItems(mItemList, isBought);
		notifyDataSetChanged();
	}

	public void removeSelected() {
		ArrayList<ItemData> items = MultiSelection.getInstance().getSelectedItems();

		for (ItemData item : items) {
			getDS().delete(item.getIdItemData());
			removeItem(item);
		}

		MultiSelection.getInstance().clearSelections();
	}

	public void clearSelections() {
		MultiSelection.getInstance().clearSelections();
	}
	//</editor-fold>
}