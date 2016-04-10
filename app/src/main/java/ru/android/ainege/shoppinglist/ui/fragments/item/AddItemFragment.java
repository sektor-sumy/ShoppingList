package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.GenericDS;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS.ItemCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Dictionary;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.util.Image;

public class AddItemFragment extends ItemFragment {
	private static final String ID_LIST = "idList";

	private boolean mIsUseDefaultData = false;

	private String mItemName = "";
	private String mAddedAmount = "";
	private int mAddedUnit = 0;
	private String mAddedPrice = "";
	private int mAddedCategory = 0;
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		} else {
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}

		mIsUseDefaultData = mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_data), true);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	protected void setupView(View v) {
		super.setupView(v);

		mUnit.setSelection(getPosition(mUnit, getActivity().getResources().getStringArray(R.array.units)[0]));
		mCategory.setSelection(getPosition(mCategory, (getActivity().getResources().getStringArray(R.array.categories)[0]).split("—")[0]));

		mName.setOnItemClickListener(getOnNameClickListener());
		mCollapsingToolbarLayout.setTitle(getString(R.string.add));
		mCollapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
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
				} else if (s.toString().equals(s.toString().trim())) {
					disableError(mNameInputLayout);
					//If selected a existent item and default data are used,
					//when changing item, fill in the data that have been previously introduced
					if (mIsUseDefaultData && mIsSelectedItem && !mItemName.equals(s.toString().trim())) {
						mAmount.setText(mAddedAmount);
						mUnit.setSelection(mAddedUnit);
						mPrice.setText(mAddedPrice);
						mCategory.setSelection(mAddedCategory);
						mComment.setText(mAddedComment);
						mItemInList.setIdItem(0);
						mIsSelectedItem = false;
					}
					//Check is the item in the list. If there is a warning display
					//If it isn`t, check is it in the catalog of items. If there is select it
					ShoppingListCursor cursor = mItemsInListDS.getByName(s.toString().trim(), getIdList());
					if (cursor.moveToFirst()) {
						mInfo.setText(R.string.info_exit_item_in_list);
						mInfo.setVisibility(View.VISIBLE);

						ShoppingList itemInList = cursor.getEntity();
						mItemInList.setIdItemData(itemInList.getIdItemData());
						setImage(itemInList.getItem());
					} else {
						mInfo.setVisibility(View.GONE);
						mItemInList.setIdItem(0);

						if (mIsProposedItem) {
							ItemCursor cursorItem = mItemDS.getByName(s.toString().trim());
							if (cursorItem.moveToFirst()) {
								setImage(cursorItem.getEntity());
							}
							cursorItem.close();
						}
					}
					cursor.close();
				}
			}

			private void setImage(Item item) {
				mItemInList.setIdItem(item.getId());

				mItemInList.getItem().setImagePath(item.getImagePath());
				mItemInList.getItem().setDefaultImagePath(item.getDefaultImagePath());
				mItemInList.getItem().setIdItemData(item.getIdItemData());
				loadImage();
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
					mAddedAmount = mAmount.getText().toString();
					mAddedUnit = mUnit.getSelectedItemPosition();
					mAddedPrice = mPrice.getText().toString();
					mAddedCategory = mCategory.getSelectedItemPosition();
					mAddedComment = mComment.getText().toString();

					ItemCursor c = mItemDS.getWithData(mItemInList.getIdItem());
					c.moveToFirst();
					Item item = c.getEntity();
					c.close();

					mItemName = item.getName();

					if (mInfo.getVisibility() == View.GONE) {
						mItemInList.getItem().setImagePath(item.getImagePath());
						mItemInList.getItem().setDefaultImagePath(item.getDefaultImagePath());
						mItemInList.getItem().setIdItemData(item.getIdItemData());
						loadImage();
					}

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_amount), true) && item.getAmount() != 0) {
						mAmount.setText(new DecimalFormat("#.######").format(item.getAmount()));
					}

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_unit), true)) {
						mUnit.setSelection(getPosition(mUnit, item.getIdUnit()));
					}

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_price), true) && item.getPrice() != 0) {
						mPrice.setText(String.format("%.2f", item.getPrice()));
					}

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_category), true)) {
						mCategory.setSelection(getPosition(mCategory, item.getIdCategory()));
					}

					if (mPrefs.getBoolean(getString(R.string.settings_key_auto_complete_comment), true)) {
						mComment.setText(item.getComment());
					}
				}
			}
		};
	}

	@Override
	protected boolean saveData() {
		boolean isSave = false;

		if (mName.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_value));
		} else if (mName.length() < 3) {
			mNameInputLayout.setError(getString(R.string.error_length_name));
		}

		if (!mIsImageLoaded) {
			Toast.makeText(getActivity(), getString(R.string.wait_load_image), Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!mNameInputLayout.isErrorEnabled() && !mAmountInputLayout.isErrorEnabled() &&
				!mPriceInputLayout.isErrorEnabled()) {
			updateItemInList();
			mItemInList.updateItem(getActivity());

			if (mInfo.getVisibility() == View.VISIBLE) { //If item in list, update it
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
	protected long getIdList() {
		return getArguments().getLong(ID_LIST);
	}

	@Override
	protected void updatedItem() {
		if (mItemInList.getItem().isNew()) { //if (mItemInList.getIdItem() == 0) {
			String image = mItemInList.getItem().getImagePath();
			String defaultImage = mItemInList.getItem().getDefaultImagePath();

			if (image == null) {
				defaultImage = image = Image.CHARACTER_IMAGE_PATH + (int) getName().toUpperCase().charAt(0) + ".png";
			}

			mItemInList.setItem(new Item(getName(), defaultImage == null ?  image : defaultImage, image));
		} else {
			mItemInList.getItem().setName(getName());
		}

	}

	@Override
	protected void resetImage() {
		if (!mItemInList.getItem().isNew() && mItemInList.getItem().getDefaultImagePath() != null) {
			mItemInList.getItem().setImagePath(mItemInList.getItem().getDefaultImagePath());
			loadImage();
		} else {
			mItemInList.getItem().setImagePath(null);
			mAppBarImage.setImageResource(android.R.color.transparent);
			mCollapsingToolbarLayout.setTitle(getString(R.string.add));
		}
	}

	private int getPosition(Spinner spinner, String name) {
		int index = -1;
		for (int i = 0; i < spinner.getCount(); i++) {
			if (((GenericDS.EntityCursor<Dictionary>) spinner.getItemAtPosition(i)).getEntity().getName().equals(name)) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			index = mIsUseNewItemInSpinner ? 1 : 0;
		}
		return index;
	}
}
