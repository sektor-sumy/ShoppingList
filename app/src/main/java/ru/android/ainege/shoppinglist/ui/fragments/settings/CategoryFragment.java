package ru.android.ainege.shoppinglist.ui.fragments.settings;

import android.content.Loader;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.DictionaryDS;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataDS;
import ru.android.ainege.shoppinglist.db.entities.Category;

public class CategoryFragment extends DictionaryFragment<Category> {

	@Override
	protected String getTitle() {
		return getString(R.string.setting_title_category);
	}

	@Override
	protected View.OnClickListener getAddHandler() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralDialogFragment addItemDialog = new CategoryDialogFragment();
				addItemDialog.setTargetFragment(CategoryFragment.this, ADD_FRAGMENT_CODE);
				addItemDialog.show(getFragmentManager(), ADD_FRAGMENT_DATE);
			}
		};
	}

	@Override
	protected DictionaryDS getDS() {
		return new CategoriesDS(getActivity());
	}

	@Override
	protected RecyclerViewAdapter getAdapter() {
		return new CategoryViewAdapter();
	}

	@Override
	protected boolean isEntityUsed(long idCategory) {
		return new ItemDataDS(getActivity()).isCategoryUsed(idCategory);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case DATA_LOADER:
				if (data.moveToFirst()) {
					mDictionary = ((CategoriesDS.CategoryCursor) data).getEntities();
					mAdapterRV.notifyDataSetChanged();
				}

				data.close();
				break;
			default:
				break;
		}
	}

	@Override
	protected void showEditDialog(int position) {
		GeneralDialogFragment editItemDialog = CategoryDialogFragment.newInstance(mDictionary.get(position));
		editItemDialog.setTargetFragment(CategoryFragment.this, EDIT_FRAGMENT_CODE);
		editItemDialog.show(getFragmentManager(), EDIT_FRAGMENT_DATE);
	}

	private class CategoryViewAdapter extends RecyclerViewAdapter<CategoryViewAdapter.CategoryViewHolder> {

		@Override
		public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._settings_dictionary, parent, false);
			return new CategoryViewHolder(v);
		}

		@Override
		public void onBindViewHolder(CategoryViewHolder holder, int position) {
			super.onBindViewHolder(holder, position);

			holder.mColor.setBackgroundColor(mDictionary.get(position).getColor());
		}

		public class CategoryViewHolder extends RecyclerViewAdapter<CategoryViewHolder>.ViewHolder {
			public TextView mColor;

			public CategoryViewHolder(View v) {
				super(v);

				mColor = (TextView) v.findViewById(R.id.color);
			}
		}
	}
}
