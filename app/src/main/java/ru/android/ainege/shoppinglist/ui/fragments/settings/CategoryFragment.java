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
import ru.android.ainege.shoppinglist.db.entities.Category;

import static ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS.CategoryCursor;

public class CategoryFragment extends DictionaryFragment<Category> {

	@Override
	protected String getTitle() {
		return getString(R.string.settings_title_category);
	}

	@Override
	protected View.OnClickListener getAddHandler() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralDialogFragment addItemDialog = new CategoryDialogFragment();
				addItemDialog.setTargetFragment(CategoryFragment.this, ADD);
				addItemDialog.show(getFragmentManager(), ADD_DATE);
			}
		};
	}

	@Override
	protected DictionaryDS getDS() {
		return new CategoriesDS(getActivity());
	}

	@Override
	protected RecyclerViewAdapter getAdapter() {
		return new CategoryAdapter();
	}

	@Override
	protected boolean isEntityUsed(long idCategory) {
		return getDS().isUsed(idCategory);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case DATA_LOADER:
				if (data.moveToFirst()) {
					mDictionary = ((CategoryCursor) data).getEntities();
					mAdapterRV.notifyDataSetChanged();

					if (mLastEditId != -1) {
						mDictionaryRV.scrollToPosition(getPosition(mLastEditId));
					}
				}

				break;
			default:
				break;
		}
	}

	@Override
	protected void showEditDialog(int position) {
		GeneralDialogFragment editItemDialog = CategoryDialogFragment.newInstance(mDictionary.get(position));
		editItemDialog.setTargetFragment(CategoryFragment.this, EDIT);
		editItemDialog.show(getFragmentManager(), EDIT_DATE);
	}

	private class CategoryAdapter extends RecyclerViewAdapter<CategoryAdapter.CategoryHolder> {

		@Override
		public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._settings_dictionary, parent, false);
			return new CategoryHolder(v);
		}

		@Override
		public void onBindViewHolder(CategoryHolder holder, int position) {
			super.onBindViewHolder(holder, position);

			holder.mColor.setBackgroundColor(mDictionary.get(position).getColor());
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
