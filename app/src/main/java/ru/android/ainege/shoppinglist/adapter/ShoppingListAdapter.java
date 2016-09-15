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
import java.util.List;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.GenericDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.ItemData;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.fragments.ShoppingListFragment;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.MultiSelection;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;

public class ShoppingListAdapter extends NestedListAdapter {
	private String mCurrency;

	private ShowcaseListener mShowcaseListener;
	private boolean mIsShowShowcase = false;

	public interface ShowcaseListener {
		void onAttachedAdapter(String idActiveSequence);
	}

	public ShoppingListAdapter(Activity activity, ShoppingListFragment fragment) {
		super(activity);
		mActivity = activity;
		mShowcaseListener = fragment;
	}

	public void setData(List<Category> categoryList, String currency, boolean isUseCategory, boolean isCollapsedCategory) {
		mIsUseCategory = isUseCategory;
		ArrayList<Object> items = generateParentChildItemList(categoryList, isCollapsedCategory);

		setData(items, currency, isUseCategory);
	}

	public void setData(ArrayList<Object> categoryList, String currency, boolean isUseCategory) {
		mCurrency = currency;
		mIsUseCategory = isUseCategory;
		mItemList = categoryList;

		if (new MaterialShowcaseSequence(mActivity, Showcase.SHOT_ADD_ITEM).hasFired()) {
			mIsShowShowcase = true;
		}

		notifyDataSetChanged();
	}

	public void setCurrency(String currency, boolean withUpdate) {
		mCurrency = currency;
		if (withUpdate) {
			notifyDataSetChanged();
		}
	}

	//Converts the source list on the list to work with the adapter
	@Override
	protected ArrayList<Object> generateParentChildItemList(List<Category> categoryList, boolean isCollapsedCategory) {
		ArrayList<Object> list = new ArrayList<>();
		Category category;

		if (mIsUseCategory) {
			for (int i = 0; i < categoryList.size(); i++) {
				category = categoryList.get(i);
				list.add(category);
				boolean isCollapsed = true;

				if (!isCollapsedCategory ||
						(isCollapsedCategory &&
								!mCollapseCategoryStates.containsKey(category.getId()) &&
								!isAllItemsBoughtInCategory(category)) ||
						(isCollapsedCategory &&
								mCollapseCategoryStates.containsKey(category.getId()) &&
								!mCollapseCategoryStates.get(category.getId()))) {
					isCollapsed = false;
					int childListItemCount = category.getItemsByCategories().size();
					for (int j = 0; j < childListItemCount; j++) {
						list.add(category.getItemsByCategories().get(j));
					}
				}

				setCollapseCategoryStates(category.getId(), isCollapsed);
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
	public CategoryViewHolder onCreateCategoryViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_shopping_lists_category, parent, false);
		return new CategoryViewHolder(v);
	}

	@Override
	public ItemViewHolder onCreateItemViewHolder(ViewGroup parent) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_shopping_lists_item, parent, false);
		return new ItemViewHolder(v);
	}

	@Override
	public void onBindCategoryViewHolder(CategoryVH viewHolder, int position, Category category) {
		CategoryViewHolder holder = (CategoryViewHolder) viewHolder;

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

	@Override
	public void onBindItemViewHolder(ItemVH viewHolder, int position, ItemData item) {
		ItemViewHolder holder = (ItemViewHolder) viewHolder;
		ShoppingList itemInList = (ShoppingList) item;

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
			String amount = NumberFormat.getInstance().format(itemInList.getAmount())
					+ " " + itemInList.getUnit().getName();
			holder.mAmount.setText(amount);
		}

		holder.mPrice.setText(getValueWithCurrency(itemInList.getPrice()));
		holder.mIsBought.setVisibility(itemInList.isBought() ? View.VISIBLE : View.GONE);
	}

	@Override
	protected GenericDS getDS() {
		return new ShoppingListDS(mActivity);
	}

	@Override
	public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
		super.onViewAttachedToWindow(holder);

		if (mIsShowShowcase && mShowcaseListener != null) {
			mIsShowShowcase = false;
			holder.itemView.post(new Runnable() {
				@Override
				public void run() {
					mShowcaseListener.onAttachedAdapter(null);
				}
			});
		}
	}

	private boolean isAllItemsBoughtInCategory(Category category){
		return (category.countBoughtItems() == category.getItemsByCategories().size());
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

	public class CategoryViewHolder extends CategoryVH {
		public LinearLayout mCategoryContainer;
		public TextView mColor;
		public TextView mCategory;
		public TextView mSumCategory;

		public CategoryViewHolder(View v) {
			super(v);
			mCategoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
			mColor = (TextView) v.findViewById(R.id.color);
			mCategory = (TextView) v.findViewById(R.id.category);
			mSumCategory = (TextView) v.findViewById(R.id.sum_category);
		}
	}

	public class ItemViewHolder extends ItemVH {
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

		public void markBought(boolean isBought) {
			mIsBought.setVisibility(isBought ? View.VISIBLE : View.GONE);
		}
	}
}