package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS.ItemCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.util.Image;

public class AddItemFragment extends ItemFragment {
	private static final String ID_LIST = "idList";
	private static final String STATE_ITEM_NAME = "state_item_name";
	private static final String STATE_ADDED_AMOUNT = "state_added_amount";
	private static final String STATE_ADDED_UNIT = "state_added_unit";
	private static final String STATE_ADDED_PRICE = "state_added_price";
	private static final String STATE_ADDED_CATEGORY = "state_added_category";
	private static final String STATE_ADDED_COMMENT = "state_added_comment";
	private static final String STATE_IS_SELECTED_ITEM = "state_is_selected_item";

	private boolean mIsUseDefaultData = false;

	private String mItemName = "";
	private String mAddedAmount = "";
	private long mIdAddedUnit;
	private String mAddedPrice = "";
	private long mIdAddedCategory;
	private String mAddedComment = "";

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
		mItemInList = new ShoppingList(getIdList());
		mItemInList.getItem().setImagePath(Image.getPathFromResource(R.drawable.no_image));

		mIsUseDefaultData = mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_data), true);
		mIsAdded = true;

		if (savedInstanceState != null) {
			mItemName = savedInstanceState.getString(STATE_ITEM_NAME);
			mAddedAmount = savedInstanceState.getString(STATE_ADDED_AMOUNT);
			mIdAddedUnit = savedInstanceState.getLong(STATE_ADDED_UNIT);
			mAddedPrice = savedInstanceState.getString(STATE_ADDED_PRICE);
			mIdAddedCategory = savedInstanceState.getLong(STATE_ADDED_CATEGORY);
			mAddedComment = savedInstanceState.getString(STATE_ADDED_COMMENT);

			mIsSelectedItem = savedInstanceState.getBoolean(STATE_IS_SELECTED_ITEM);
		}
	}

	@Override
	protected void setupView(View v, Bundle savedInstanceState) {
		super.setupView(v, savedInstanceState);

		// TODO: 03.09.2016 lost image (assets) after retained
		if (savedInstanceState != null) {
			loadImage(mPictureView.getImagePath());
		}

		mUnitSpinner.setSelected(getActivity().getResources().getStringArray(R.array.units)[0]);

		if (savedInstanceState != null && !mIsUseCategory) {
			mCategorySpinner.setSelected(savedInstanceState.getLong(STATE_CATEGORY_ID));
		} else {
			mCategorySpinner.setSelected(getActivity().getResources().getStringArray(R.array.categories)[0].split("—")[0]);
		}

		mNameTextView.setOnItemClickListener(getOnNameClickListener());
		mCollapsingToolbarLayout.setTitle(getString(R.string.add));
		mCollapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(STATE_ITEM_NAME, mItemName);
		outState.putString(STATE_ADDED_AMOUNT, mAddedAmount);
		outState.putLong(STATE_ADDED_UNIT, mIdAddedUnit);
		outState.putString(STATE_ADDED_PRICE, mAddedPrice);
		outState.putLong(STATE_ADDED_CATEGORY, mIdAddedCategory);
		outState.putString(STATE_ADDED_COMMENT, mAddedComment);
		outState.putBoolean(STATE_IS_SELECTED_ITEM, mIsSelectedItem);
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
				if (s.length() > 0 && s.toString().equals(s.toString().trim())) {
					disableError(mNameInputLayout);
					//If selected a existent item and default data are used,
					//when changing item, fill in the data that have been previously introduced
					if (mIsUseDefaultData && mIsSelectedItem && !mItemName.equals(s.toString().trim())) {
						mAmountEditText.setText(mAddedAmount);
						mUnitSpinner.setSelected(mIdAddedUnit);
						mPriceEditText.setText(mAddedPrice);
						mCategorySpinner.setSelected(mIdAddedCategory);
						mCommentEditText.setText(mAddedComment);
						mItemInList.setIdItem(0);
						mIsSelectedItem = false;
					}
					//Check is the item in the list. If there is a warning display
					//If it isn`t, check is it in the catalog of items. If there is select it
					ShoppingListCursor cursor = mItemsInListDS.getByName(s.toString().trim(), getIdList());
					if (cursor.moveToFirst()) {
						mInfoTextView.setText(R.string.info_exit_item_in_list);
						mInfoTextView.setTextColor(Color.RED);
						mInfoTextView.setVisibility(View.VISIBLE);

						ShoppingList itemInList = cursor.getEntity();
						mItemInList.setIdItemData(itemInList.getIdItemData());
						setDefaultData(itemInList.getItem());
					} else {
						mInfoTextView.setVisibility(View.GONE);
						mItemInList.setIdItem(0);

						if (mIsProposedItem) {
							ItemCursor cursorItem = mItemDS.getWithData(s.toString().trim());
							if (cursorItem.moveToFirst()) {
								setDefaultData(cursorItem.getEntity());
							}
							cursorItem.close();
						}
					}
					cursor.close();
				}
			}

			private void setDefaultData(Item item) {
				mItemInList.setIdItem(item.getId());
				mItemInList.getItem().setDefaultImagePath(item.getDefaultImagePath());
				mItemInList.getItem().setIdItemData(item.getIdItemData());

				loadImage(item.getImagePath());
				mUnitSpinner.setSelected(item.getIdUnit());
				mCategorySpinner.setSelected(item.getIdCategory());
			}
		};
	}

	@Override
	protected SimpleCursorAdapter getCompleteTextAdapter() {
		return super.getCompleteTextAdapter(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence charSequence) {
				Cursor managedCursor = mItemDS.getNames(charSequence != null ? charSequence.toString().trim() : null);

				if (managedCursor.moveToFirst()) {
					mIsProposedItem = true;
				}

				return managedCursor;
			}
		});
	}

	private AdapterView.OnItemClickListener getOnNameClickListener() {
		return new AdapterView.OnItemClickListener() {
			@Override // TODO: id != l
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				mItemInList.setIdItem(l);

				mIsSelectedItem = true;
				//If default data are used, they fill in the fields
				// and save previously introduced data
				if (mIsUseDefaultData) {
					mAddedAmount = mAmountEditText.getText().toString();
					mIdAddedUnit = mUnitSpinner.getSelected().getId();
					mAddedPrice = mPriceEditText.getText().toString();
					mIdAddedCategory = mCategorySpinner.getSelected().getId();
					mAddedComment = mCommentEditText.getText().toString();

					ItemCursor c = mItemDS.getWithData(mItemInList.getIdItem());
					c.moveToFirst();
					Item item = c.getEntity();
					c.close();

					mItemName = item.getName();

					if (mInfoTextView.getVisibility() == View.GONE) {
						mItemInList.getItem().setDefaultImagePath(item.getDefaultImagePath());
						mItemInList.getItem().setIdItemData(item.getIdItemData());
						loadImage(item.getImagePath());
					}

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_amount), true) && item.getAmount() != 0) {
						mAmountEditText.setText(new DecimalFormat("#.######").format(item.getAmount()));
					}

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_unit), true)) {
						mUnitSpinner.setSelected(item.getIdUnit());
					}

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_price), true) && item.getPrice() != 0) {
						mPriceEditText.setText(String.format("%.2f", item.getPrice()));
					}

					mCategorySpinner.setSelected(item.getIdCategory());

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_comment), true)) {
						mCommentEditText.setText(item.getComment());
					}
				}
			}
		};
	}

	@Override
	protected long getIdList() {
		return getArguments().getLong(ID_LIST);
	}

	@Override
	protected boolean saveData() {
		boolean isSave = false;

		if (isValidData()) {
			refreshItem();
			mItemInList.updateItem(getActivity());

			if (mInfoTextView.getVisibility() == View.VISIBLE) { //If item in list, update it
				mItemsInListDS.update(mItemInList);
			} else { //Add new item to list
				mItemsInListDS.add(mItemInList);
			}

			sendResult(mItemInList.getIdItem());
			isSave = true;
		}

		return isSave;
	}

	@Override
	protected ShoppingList refreshItem() {
		if (mItemInList.getItem().isNew()) {
			String image = mItemInList.getItem().getImagePath();
			String defaultImage = mItemInList.getItem().getDefaultImagePath();

			if (image == null || image.equals(Image.getPathFromResource(R.drawable.no_image))) {
				defaultImage = image = Image.CHARACTER_IMAGE_PATH + (int) getName().toUpperCase().charAt(0) + ".png";
			}

			mItemInList.setItem(new Item(getName(), defaultImage == null ? image : defaultImage, image));
		} else {
			mItemInList.getItem().setName(getName());
		}

		return super.refreshItem();
	}

	@Override
	public void resetImage() {
		if (!mItemInList.getItem().isNew() && mItemInList.getItem().getDefaultImagePath() != null) {
			loadImage(mItemInList.getItem().getDefaultImagePath());
		} else {
			if (!mItemInList.getItem().getImagePath().contains(Image.ASSETS_IMAGE_PATH)) {
				Image.deleteFile(mItemInList.getItem().getImagePath());
			}

			mItemInList.getItem().setImagePath(Image.getPathFromResource(R.drawable.no_image));
			mPictureView.getImage().setImageResource(R.drawable.no_image);
			mCollapsingToolbarLayout.setTitle(getString(R.string.add));
		}
	}

	@Override
	public boolean isDeleteImage(String newPath) {
		boolean result = true;

		if(mItemInList.getItem() == null) {
			return false;
		}
		String imagePath = mItemInList.getItem().getImagePath();
		String dPath  = mItemInList.getItem().getDefaultImagePath();

		if (dPath == null && imagePath == null) {
			result = false;
		} else{
			if (imagePath != null) {
				result = !imagePath.contains(Image.ASSETS_IMAGE_PATH);
			}

			if (dPath != null) {
				result = result && !newPath.equals(imagePath) && !dPath.equals(imagePath);
			}
		}

		return result;
	}

	private boolean isValidData() {
		boolean isValid = true;

		if (mNameTextView.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_value));
			isValid = false;
		} else if (mNameTextView.length() < 3) {
			mNameInputLayout.setError(getString(R.string.error_length_name));
			isValid = false;
		}

		if (mPictureView.isLoading()) {
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.wait_load_image), Toast.LENGTH_SHORT).show();
			isValid = false;
		}

		if (mNameInputLayout.isErrorEnabled() || mAmountInputLayout.isErrorEnabled() || mPriceInputLayout.isErrorEnabled()) {
			isValid = false;
		}

		return isValid;
	}
}
