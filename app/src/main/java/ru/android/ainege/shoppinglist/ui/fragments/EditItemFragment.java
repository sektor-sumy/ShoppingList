package ru.android.ainege.shoppinglist.ui.fragments;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;

public class EditItemFragment extends ItemFragment {
	private static final String ITEM_IN_LIST = "itemInList";

	private ShoppingList mItemInList;

	public static EditItemFragment newInstance(ShoppingList itemInList) {
		Bundle args = new Bundle();
		args.putSerializable(ITEM_IN_LIST, itemInList);

		EditItemFragment fragment = new EditItemFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mItemInList = (ShoppingList) getArguments().getSerializable(ITEM_IN_LIST);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		} else {
			getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
	}

	@Override
	protected void setView(View v) {
		super.setView(v);
		setDataToView();
	}

	@Override
	protected TextWatcher getNameChangedListener() {
		return new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					mNameInputLayout.setError(getString(R.string.error_name));
				} else {
					disableError(mNameInputLayout);
					//Check is the item in the list or catalog of items. If there is a warning display
					ShoppingListCursor cursor = ShoppingListDataSource.getInstance(getActivity()).
							existItemInList(s.toString().trim(), mItemInList.getIdList());
					showInfo(cursor.moveToFirst() && !cursor.getEntity().getItem().getName().equals(mItemInList.getItem().getName()));
					cursor.close();
					if (mIsProposedItem) {
						ItemDataSource.ItemCursor cursorItem = new ItemDataSource(getActivity()).getByName(s.toString().trim());
						showInfo(cursorItem.moveToFirst() && !cursorItem.getEntity().getName().equals(mItemInList.getItem().getName()));
						cursorItem.close();
					}
				}
			}

			private void showInfo(boolean condition) {
				if (condition) {
					mNameInputLayout.setError(getString(R.string.info_exit_item));
				} else {
					disableError(mNameInputLayout);
				}
			}
		};
	}

	@Override
	protected SimpleCursorAdapter getCompleteTextAdapter() {
		return super.getCompleteTextAdapter(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence charSequence) {
				Cursor managedCursor = null;
				if (!charSequence.equals(mItemInList.getItem().getName())) {
					managedCursor = new ItemDataSource(getActivity()).getNames(charSequence.toString().trim());
					if (managedCursor.moveToFirst()) {
						mIsProposedItem = true;
					}
				}
				return managedCursor;
			}
		});
	}

	private void setDataToView() {
		mName.setText(mItemInList.getItem().getName());
		mName.setSelection(mItemInList.getItem().getName().length());

		if (mItemInList.getAmount() != 0) {
			mAmount.setText(new DecimalFormat("#.######").format(mItemInList.getAmount()));
			mUnits.setSelection(getPosition(mUnits, mItemInList.getUnit().getId()));
		} else {
			mUnits.setSelection(0);
		}

		if (mItemInList.getPrice() != 0) {
			mPrice.setText(String.format("%.2f", mItemInList.getPrice()));
		}

		mComment.setText(mItemInList.getComment());

		mIsBought.setChecked(mItemInList.isBought());
	}

	private int getPosition(Spinner spinner, long id) {
		int index = 0;
		for (int i = 0; i < spinner.getCount(); i++) {
			if (((UnitsDataSource.UnitCursor) spinner.getItemAtPosition(i)).getEntity().getId() == id) {
				index = i;
				break;
			}
		}
		return index;
	}

	@Override
	protected boolean saveData(boolean isUpdateData) {
		boolean isSave = false;

		if (mName.length() < 3) {
			mNameInputLayout.setError(getString(R.string.error_length_name));
		}

		if (!mNameInputLayout.isErrorEnabled() && !mAmountInputLayout.isErrorEnabled() &&
				!mPriceInputLayout.isErrorEnabled()) {
			Item item = getItem();
			ItemDataSource itemDS = new ItemDataSource(getActivity());

			if (isUpdateData) { //Updating in the catalog if the item is selected or create a new
				itemDS.update(getItem());
			} else {
				if (mIsAlwaysSave) {  //Always save default data
					itemDS.update(getItem());
				} else { //Don`t save default data
					itemDS.update(new Item(mItemInList.getIdItem(), getName()));
				}

				//Update item in list
				ShoppingListDataSource itemInListDS = ShoppingListDataSource.getInstance(getActivity());
				itemInListDS.update(getItemInList(item));
				sendResult(mItemInList.getIdItem());
			}
			isSave = true;
		}
		return isSave;
	}

	@Override
	protected long getIdList() {
		return mItemInList.getIdList();
	}

	@Override
	Item getItem() {
		Item item = super.getItem();
		item.setId(mItemInList.getIdItem());
		return item;
	}
}
