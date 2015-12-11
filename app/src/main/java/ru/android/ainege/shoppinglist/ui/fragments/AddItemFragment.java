package ru.android.ainege.shoppinglist.ui.fragments;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.ui.Image;

public class AddItemFragment extends ItemFragment {
	private static final String ID_LIST = "idList";

	private boolean mIsUseDefaultData = false;

	private String mAddedImagePath;
	private String mAddedAmount = "";
	private int mAddedUnit = 0;
	private String mAddedPrice = "";
	private String mAddedComment = "";

	private long mIdSelectedItem = -1;
	private boolean mIsSelectedItem = false;

	public static AddItemFragment newInstance(long id) {
		Bundle args = new Bundle();
		args.putLong(ID_LIST, id);

		AddItemFragment fragment = new AddItemFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!NOT_USE_DEFAULT_DATA.equals(mDataSave)) {
			mIsUseDefaultData = true;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		} else {
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	protected void setView(View v) {
		super.setView(v);
		mName.setOnItemClickListener(getOnNameClickListener());
		mCollapsingToolbarLayout.setTitle(getString(R.string.add));
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
					//If selected a existent item and default data are used,
					//when changing item, fill in the data that have been previously introduced
					if (mIsUseDefaultData && mIsSelectedItem) {
						mImagePath = mAddedImagePath;
						if (mImagePath == null) {
							resetImage();
						} else {
							loadImage();
						}

						mAmount.setText(mAddedAmount);
						mUnits.setSelection(mAddedUnit);
						mPrice.setText(mAddedPrice);
						mComment.setText(mAddedComment);
						mIdSelectedItem = -1;
					}
					//Check is the item in the list. If there is a warning display
					//If it isn`t, check is it in the catalog of items. If there is select it
					ShoppingListCursor cursor = ShoppingListDataSource.getInstance(getActivity()).
							existItemInList(s.toString().trim(), getIdList());
					if (cursor.moveToFirst()) {
						mInfo.setText(R.string.info_exit_item_in_list);
						mInfo.setVisibility(View.VISIBLE);
						mIdSelectedItem = cursor.getEntity().getIdItem();
					} else {
						mInfo.setVisibility(View.GONE);
						if (mIsProposedItem) {
							ItemDataSource.ItemCursor cursorItem = new ItemDataSource(getActivity()).getByName(s.toString().trim());
							if (cursorItem.moveToFirst()) {
								mIdSelectedItem = cursorItem.getEntity().getId();
							}
							cursorItem.close();
						} else {
							mIdSelectedItem = -1;
						}
					}
					cursor.close();
				}
			}
		};
	}

	@Override
	protected SimpleCursorAdapter getCompleteTextAdapter() {
		return super.getCompleteTextAdapter(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence charSequence) {
				Cursor managedCursor = new ItemDataSource(getActivity()).getNames(charSequence != null ? charSequence.toString().trim() : null);
				if (managedCursor.moveToFirst()) {
					mIsProposedItem = true;
				}
				return managedCursor;
			}
		});
	}

	private AdapterView.OnItemClickListener getOnNameClickListener() {
		return new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				mIsSelectedItem = true;
				mIdSelectedItem = l;
				//If default data are used, they fill in the fields
				// and save previously introduced data
				if (mIsUseDefaultData) {
					mAddedImagePath = mImagePath;
					mAddedAmount = mAmount.getText().toString();
					mAddedUnit = mUnits.getSelectedItemPosition();
					mAddedPrice = mPrice.getText().toString();
					mAddedComment = mComment.getText().toString();

					ItemDataSource.ItemCursor c = new ItemDataSource(getActivity()).get(mIdSelectedItem);
					c.moveToFirst();
					Item item = c.getEntity();
					c.close();

					mImagePath = item.getImagePath();
					loadImage();

					double amount = item.getAmount();
					if (amount > 0) {
						mAmount.setText(new DecimalFormat("#.######").format(amount));
						mUnits.setSelection((int) item.getIdUnit());
					}
					double price = item.getPrice();
					if (price > 0) {
						mPrice.setText(String.format("%.2f", price));
					}
					mComment.setText(item.getComment());
				}
			}
		};
	}

	@Override
	protected boolean saveData(boolean isUpdateData) {
		boolean isSave = false;

		if (mName.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_value));
		} else if (mName.length() < 3) {
			mNameInputLayout.setError(getString(R.string.error_length_name));
		}

		if (!mNameInputLayout.isErrorEnabled() && !mAmountInputLayout.isErrorEnabled() &&
				!mPriceInputLayout.isErrorEnabled()) {
			Item item = getItem();
			setImagePath(item);
			ItemDataSource itemDS = new ItemDataSource(getActivity());

			if (isUpdateData) { //Updating in the catalog if the item is selected or create a new
				updateItem(item);
			} else {
				long idItem;
				if (mIsAlwaysSave) { //Always save default data
					updateItem(item);
					idItem = mIdSelectedItem;
				} else { //Don`t save default data
					if (mIdSelectedItem != -1) {
						idItem = mIdSelectedItem;
					} else {
						idItem = (int) itemDS.add(new Item(getName(), mImagePath, mImagePath));
					}
				}
				item.setId(idItem);

				//Save item to list
				ShoppingListDataSource itemInListDS = ShoppingListDataSource.getInstance(getActivity());
				if (mInfo.getVisibility() == View.VISIBLE) { //If item in list, update it
					itemInListDS.update(getItemInList(item));
				} else { //Add new item to list
					idItem = itemInListDS.add(getItemInList(item));
				}
				sendResult(idItem);
			}
			isSave = true;
		}
		return isSave;
	}

	@Override
	protected long getIdList() {
		return getArguments().getLong(ID_LIST);
	}

	@Override
	protected void resetImage() {
		mAppBarImage.setImageResource(android.R.color.transparent);
	}

	private void updateItem(Item item) {
		ItemDataSource itemDS = new ItemDataSource(getActivity());
		if (mIdSelectedItem != -1) {
			item.setId(mIdSelectedItem);
			itemDS.update(item);
		} else {
			mIdSelectedItem = (int) itemDS.add(item);
		}
	}

	private void setImagePath(Item item) {
		if (mImagePath == null) {
			mImagePath = Image.CHARACTER_IMAGE_PATH + (int) item.getName().toUpperCase().charAt(0) + ".png";
			item.setDefaultImagePath(mImagePath);
			item.setImagePath(mImagePath);
		}
	}
}
