package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.CategoryDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;

public class CategoryFragment extends CatalogFragment<Category> {

	@Override
	public int getKey() {
		return R.string.catalogs_key_category;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.catalogs_category);
	}

	@Override
	protected RecyclerViewAdapter getAdapter() {
		return new CategoryAdapter();
	}

	@Override
	protected CatalogDS getDS() {
		return new CategoriesDS(getActivity());
	}

	@Override
	protected GeneralDialogFragment getDialog() {
		return new CategoryDialogFragment();
	}

	private class CategoryAdapter extends RecyclerViewAdapter<CategoryAdapter.CategoryHolder> {

		@Override
		public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dictionaries_item, parent, false);
			return new CategoryHolder(v);
		}

		@Override
		public void onBindViewHolder(CategoryHolder holder, int position) {
			super.onBindViewHolder(holder, position);

			holder.mColor.setBackgroundColor(mCatalog.get(position).getColor());
		}

		public class CategoryHolder extends RecyclerViewAdapter<CategoryHolder>.ViewHolder {
			public TextView mColor;

			public CategoryHolder(View v) {
				super(v);

				mColor = (TextView) v.findViewById(R.id.color);
				mColor.setVisibility(View.VISIBLE);
			}
		}
	}
}
