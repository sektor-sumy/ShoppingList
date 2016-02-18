package ru.android.ainege.shoppinglist.adapter;

import android.content.Context;
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
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.util.Image;

public class ShoppingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int TYPE_CATEGORY = 0;
	private static final int TYPE_ITEM = 1;

	private List<Object> mItemList = new ArrayList<>();

	private Context mContext;
	private boolean mIsUseCategory;
	private String mCurrency;

	public ShoppingListAdapter(Context context) {
		mContext = context;
	}

	public void setData(List<Category> categoryList, String currency, boolean isUseCategory) {
		mCurrency = currency;
		mIsUseCategory = isUseCategory;
		mItemList = generateParentChildItemList(categoryList);

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
			onBindCategoryViewHolder((CategoryViewHolder) holder, position, (Category) listItem);
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

			category.calculateSpentSum();
			holder.mSumCategory.setText(getValueWithCurrency(category.getSpentSum()));
		} else {
			holder.mCategoryContainer.setVisibility(View.GONE);
		}
	}

	public void onBindItemViewHolder(ItemViewHolder holder, int position, ShoppingList itemInList) {
		if (mIsUseCategory) {
			FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) holder.mIsBought.getLayoutParams();
			param.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.padding_24dp),
					0, mContext.getResources().getDimensionPixelSize(R.dimen.padding_16dp), 0);
			holder.mIsBought.setLayoutParams(param);

			holder.mColor.setVisibility(View.VISIBLE);


			holder.mColor.setBackgroundColor(itemInList.getCategory().getColor());
		} else {
			FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) holder.mIsBought.getLayoutParams();
			param.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.padding_12dp),
					0, mContext.getResources().getDimensionPixelSize(R.dimen.padding_12dp), 0);
			holder.mIsBought.setLayoutParams(param);

			holder.mColor.setVisibility(View.GONE);
		}

		Image.create().insertImageToView(mContext, itemInList.getItem().getImagePath(), holder.mImage);
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

	private Object getListItem(int position) {
		return mItemList.get(position);
	}

	//преобразует исходный список в список для работы с адаптером
	private List<Object> generateParentChildItemList(List<Category> categoryList) {
		List<Object> parentWrapperList = new ArrayList<>();
		Category parentWrapper;

		if (mIsUseCategory) {
			for (int i = 0; i < categoryList.size(); i++) {
				parentWrapper = categoryList.get(i);
				parentWrapperList.add(parentWrapper);

				int childListItemCount = parentWrapper.getItemsByCategoryInList().size();
				for (int j = 0; j < childListItemCount; j++) {
					parentWrapperList.add(parentWrapper.getItemsByCategoryInList().get(j));
				}
			}
		} else {
			parentWrapper = categoryList.get(0);
			int childListItemCount = parentWrapper.getItemsByCategoryInList().size();
			for (int j = 0; j < childListItemCount; j++) {
				parentWrapperList.add(parentWrapper.getItemsByCategoryInList().get(j));
			}
		}

		return parentWrapperList;
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

	private class CategoryViewHolder extends RecyclerView.ViewHolder {
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

	private class ItemViewHolder extends RecyclerView.ViewHolder {
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
