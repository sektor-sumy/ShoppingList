package ru.android.ainege.shoppinglist.adapter;

import android.app.Activity;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.util.MultiSelection;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;

public class ShoppingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int TYPE_CATEGORY = 0;
	private static final int TYPE_ITEM = 1;

	public List<Object> mItemList = new ArrayList<>();
	private HashMap<Long, Boolean> collapseCategoryStates = new HashMap<>();

	private Activity mActivity;
	private String mCurrency;
	private boolean mIsUseCategory;
	private boolean mIsCollapsedCategory;

	private ShowcaseListener mShowcaseListener;
	private boolean mIsShowShowcase = false;

	public interface ShowcaseListener {
		void onAttached(String idActiveSequence);
	}

	public ShoppingListAdapter(Activity activity, ShoppingListFragment fragment) {
		mActivity = activity;
		mShowcaseListener = fragment;
	}

	public void setData(List<Category> categoryList, String currency, boolean isUseCategory, boolean isCollapsedCategory) {
		mCurrency = currency;
		mIsUseCategory = isUseCategory;
		mIsCollapsedCategory = isCollapsedCategory;
		mItemList = generateParentChildItemList(categoryList);

		if (new MaterialShowcaseSequence(mActivity, Showcase.SHOT_ADD_ITEM).hasFired()) {
			mIsShowShowcase = true;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		Object listItem = getListItem(position);
		if (listItem instanceof Category) {
			return TYPE_CATEGORY;
		} else if (listItem instanceof ShoppingList) {
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

	public CategoryViewHolder onCreateCategoryViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._shopping_list_category, parent, false);
		return new CategoryViewHolder(v);
	}

	public ItemViewHolder onCreateItemViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._shopping_list_item, parent, false);
		return new ItemViewHolder(v);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		Object listItem = getListItem(position);
		if (listItem instanceof Category) {
			CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
			categoryViewHolder.setCollapsed(collapseCategoryStates.get(((Category) listItem).getId()));
			onBindCategoryViewHolder(categoryViewHolder, position, (Category) listItem);
		} else if (listItem instanceof ShoppingList) {
			onBindItemViewHolder((ItemViewHolder) holder, position, (ShoppingList) listItem);
		} else {
			throw new IllegalStateException("Incorrect ViewHolder found");
		}
	}

	public void onBindCategoryViewHolder(CategoryViewHolder holder, int position, Category category) {
		if (mIsUseCategory) {
			holder.mCategoryContainer.setVisibility(View.VISIBLE);
			holder.mColor.setBackgroundColor(category.getColor());
			holder.mCategory.setText(category.getName());

			if (isAllItemsBoughtInCategory(category)) {
				holder.mCategory.setPaintFlags(holder.mCategory.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				holder.mCategory.setPaintFlags(holder.mCategory.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
			}

			category.calculateSpentSum();
			holder.mSumCategory.setText(getValueWithCurrency(category.getSpentSum()));
		} else {
			holder.mCategoryContainer.setVisibility(View.GONE);
		}
	}

	public void onBindItemViewHolder(ItemViewHolder holder, int position, ShoppingList itemInList) {
		FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) holder.mIsBought.getLayoutParams();

		if (mIsUseCategory) {
			param.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.padding_24dp), 0, mActivity.getResources().getDimensionPixelSize(R.dimen.padding_16dp), 0);
			holder.mIsBought.setLayoutParams(param);

			holder.mColor.setVisibility(View.VISIBLE);
			holder.mColor.setBackgroundColor(itemInList.getCategory().getColor());
		} else {
			param.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.padding_12dp), 0, mActivity.getResources().getDimensionPixelSize(R.dimen.padding_12dp), 0);
			holder.mIsBought.setLayoutParams(param);

			holder.mColor.setVisibility(View.GONE);
		}

		Image.create().insertImageToView(mActivity, itemInList.getItem().getImagePath(), holder.mImage);
		holder.itemView.setSelected(MultiSelection.getInstance().isContains(itemInList));
		holder.mName.setText(itemInList.getItem().getName());

		if (itemInList.getAmount() == 0) {
			holder.mAmount.setText("-");
		} else {
			String amount = NumberFormat.getInstance().format(itemInList.getAmount()) // TODO: 17.02.2016 почему нет приведение к локальным значениям? 
					+ " " + itemInList.getUnit().getName();
			holder.mAmount.setText(amount);
		}

		holder.mPrice.setText(getValueWithCurrency(itemInList.getPrice()));
		holder.mIsBought.setVisibility(itemInList.isBought() ? View.VISIBLE : View.GONE);
	}

	@Override
	public int getItemCount() {
		return mItemList.size();
	}

	public Object getListItem(int position) {
		return mItemList.get(position);
	}

	public List<Object> getItemList() {
		return mItemList;
	}

	@Override
	public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
		super.onViewAttachedToWindow(holder);

		if (mIsShowShowcase && mShowcaseListener != null) {
			mIsShowShowcase = false;
			holder.itemView.post(new Runnable() {
				@Override
				public void run() {
					mShowcaseListener.onAttached(null);
				}
			});
		}
	}

	//<editor-fold desc="Extend/collapse category">
	public void setOnClick(RecyclerView.ViewHolder holder, int position) {
		CategoryViewHolder categoryHolder = (CategoryViewHolder) holder;
		Category category = (Category) getListItem(position);

		if (categoryHolder.isCollapsed()) {
			categoryHolder.setCollapsed(false);
			collapseCategoryStates.put(category.getId(), false);
			extendCategory(category, position);
		} else {
			categoryHolder.setCollapsed(true);
			collapseCategoryStates.put(category.getId(), true);
			collapseCategory(category, position);
		}
	}

	private void collapseCategory(Category category, int position) {
		List<ShoppingList> itemInList = category.getItemsByCategoryInList();

		if (itemInList != null) {
			for (int i = itemInList.size(); i > 0; i--) {
				mItemList.remove(position + i);
				notifyItemRemoved(position + i);
			}
		}
	}

	private void extendCategory(Category category, int position){
		mIsShowShowcase = true;
		List<ShoppingList> itemInList = category.getItemsByCategoryInList();

		if (itemInList != null) {
			for (int i = 0; i < itemInList.size(); i++) {
				mItemList.add(position + i + 1, itemInList.get(i));
				notifyItemInserted(position + i + 1);
			}
		}
	}

	public void extendAllCategory() {
		for (int i = 0; i < mItemList.size(); i++) {
			if (mItemList.get(i) instanceof Category && collapseCategoryStates.get(((Category) mItemList.get(i)).getId())) {
				extendCategory((Category) mItemList.get(i), i);
			}
		}
	}

	public void recoveryCollapseAllCategory() {
		for (int i = 0; i < mItemList.size(); i++) {
			if (mItemList.get(i) instanceof Category && collapseCategoryStates.get(((Category) mItemList.get(i)).getId())) {
				collapseCategory((Category) mItemList.get(i), i);
			} else {
				if (mItemList.get(i) instanceof ShoppingList && MultiSelection.getInstance().getSelectedItems().contains(mItemList.get(i))) {
					MultiSelection.getInstance().delete(mItemList.get(i));
					notifyItemChanged(i);
				}
			}
		}
	}
	//</editor-fold>

	//Converts the source list on the list to work with the adapter
	private List<Object> generateParentChildItemList(List<Category> categoryList) {
		List<Object> list = new ArrayList<>();
		Category category;

		if (mIsUseCategory) {
			for (int i = 0; i < categoryList.size(); i++) {
				category = categoryList.get(i);
				list.add(category);
				boolean isCollapsed = true;

				if (!mIsCollapsedCategory ||
						(mIsCollapsedCategory &&
								!collapseCategoryStates.containsKey(category.getId()) &&
								!isAllItemsBoughtInCategory(category)) ||
						(mIsCollapsedCategory && collapseCategoryStates.containsKey(category.getId()) &&
								!collapseCategoryStates.get(category.getId()))) {
					isCollapsed = false;
					int childListItemCount = category.getItemsByCategoryInList().size();
					for (int j = 0; j < childListItemCount; j++) {
						list.add(category.getItemsByCategoryInList().get(j));
					}
				}

				collapseCategoryStates.put(category.getId(), isCollapsed);
			}
		} else {
			category = categoryList.get(0);
			int childListItemCount = category.getItemsByCategoryInList().size();
			for (int j = 0; j < childListItemCount; j++) {
				list.add(category.getItemsByCategoryInList().get(j));
			}
		}

		return list;
	}

	private String getValueWithCurrency(double value) {
		return localValue(value) + " " + mCurrency;
	}

	private String localValue(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(value);
	}

	public boolean isAllItemsBoughtInCategory(Category category){
		return (category.countBoughtItems() == category.getItemsByCategoryInList().size());
	}

	//<editor-fold desc="Selection">
	public void selectItem(ShoppingList item) {
		MultiSelection.getInstance().toggleSelection(item);
		notifyItemChanged(mItemList.indexOf(item));
	}

	public void selectAllItemsInCategory(Category category) {
		MultiSelection.getInstance().selectAllItemsInCategory(category.getItemsByCategoryInList());
		notifyItemRangeChanged(mItemList.indexOf(category) + 1, category.getItemsByCategoryInList().size());
	}

	public void selectAllItems(boolean isBought) {
		MultiSelection.getInstance().selectAllItems(mItemList, isBought);
		notifyDataSetChanged();
	}

	public void removeSelected() {
		ArrayList<ShoppingList> items = MultiSelection.getInstance().getSelectedItems();

		for (ShoppingList item : items) {
			new ShoppingListDS(mActivity).delete(item.getIdItemData());
			removeItem(item);
		}

		MultiSelection.getInstance().clearSelections();
	}

	public void clearSelections() {
		MultiSelection.getInstance().clearSelections();
	}
	//</editor-fold>

	public void removeItem(ShoppingList item) {
		int position = mItemList.indexOf(item);
		mItemList.remove(item);
		item.getCategory().getItemsByCategoryInList().remove(item);
		notifyItemRemoved(position);
		position = mItemList.indexOf(item.getCategory());

		if (position != -1) {
			if (item.getCategory().getItemsByCategoryInList().size() == 0) {
				mItemList.remove(item.getCategory());
				notifyItemRemoved(position);
			} else {
				notifyItemChanged(position);
			}
		}
	}

	public class CategoryViewHolder extends RecyclerView.ViewHolder {
		public LinearLayout mCategoryContainer;
		public TextView mColor;
		public TextView mCategory;
		public TextView mSumCategory;

		private boolean mIsCollapsed = false;

		public CategoryViewHolder(View v) {
			super(v);
			mCategoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
			mColor = (TextView) v.findViewById(R.id.color);
			mCategory = (TextView) v.findViewById(R.id.category);
			mSumCategory = (TextView) v.findViewById(R.id.sum_category);
		}

		public boolean isCollapsed() {
			return mIsCollapsed;
		}

		public void setCollapsed(boolean collapsed) {
			mIsCollapsed = collapsed;
		}
	}

	public class ItemViewHolder extends RecyclerView.ViewHolder {
		public ImageView mImage;
		public TextView mName;
		public TextView mAmount;
		public TextView mPrice;
		public View mIsBought;
		public TextView mColor;

		public ItemViewHolder(View v) {
			super(v);
			mColor = (TextView) v.findViewById(R.id.color);
			mImage = (ImageView) v.findViewById(R.id.item_image_list);
			mName = (TextView) v.findViewById(R.id.item_name_list);
			mAmount = (TextView) v.findViewById(R.id.item_amount_list);
			mPrice = (TextView) v.findViewById(R.id.item_price_list);
			mIsBought = v.findViewById(R.id.is_bought_list);
		}
	}
}