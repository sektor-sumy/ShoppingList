package ru.android.ainege.shoppinglist.ui.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS.ItemCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.ui.Image;

public class AddItemFragment extends ItemFragment {
	private static final String ID_LIST = "idList";

	private boolean mIsUseDefaultData = false;

	private String mItemName = "";
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		} else {
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		if (prefs.getBoolean(getString(R.string.settings_key_sort_is_default_data), true)) {
			mIsUseDefaultData = true;
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

		String defaultName = getActivity().getResources().getStringArray(R.array.units)[0];
		mUnits.setSelection(getPosition(mUnits, defaultName));

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
					if (mIsUseDefaultData && mIsSelectedItem && !mItemName.equals(s.toString().trim())) {
						mAmount.setText(mAddedAmount);
						mUnits.setSelection(mAddedUnit);
						mPrice.setText(mAddedPrice);
						mComment.setText(mAddedComment);
						mIdSelectedItem = -1;
						mIsSelectedItem = false;
					}
					//Check is the item in the list. If there is a warning display
					//If it isn`t, check is it in the catalog of items. If there is select it
					ShoppingListCursor cursor = mItemsInListDS.existItemInList(s.toString().trim(), getIdList());
					if (cursor.moveToFirst()) {
						mInfo.setText(R.string.info_exit_item_in_list);
						mInfo.setVisibility(View.VISIBLE);

						setImage(cursor.getEntity().getItem());
					} else {
						mInfo.setVisibility(View.GONE);
						if (mIsProposedItem) {
							ItemCursor cursorItem = mItemDS.getByName(s.toString().trim());
							if (cursorItem.moveToFirst()) {
								setImage(cursorItem.getEntity());
							}
							cursorItem.close();
						} else {
							mIdSelectedItem = -1;
						}
					}
					cursor.close();
				}
			}

			private void setImage(Item item) {
				mIdSelectedItem = item.getId();

				mImagePath = item.getImagePath();
				mImageDefaultPath = item.getDefaultImagePath();
				loadImage(false);
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
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				mIdSelectedItem = l;
				mIsSelectedItem = true;
				//If default data are used, they fill in the fields
				// and save previously introduced data
				if (mIsUseDefaultData) {
					mAddedAmount = mAmount.getText().toString();
					mAddedUnit = mUnits.getSelectedItemPosition();
					mAddedPrice = mPrice.getText().toString();
					mAddedComment = mComment.getText().toString();

					ItemCursor c = mItemDS.getWithData(mIdSelectedItem);
					c.moveToFirst();
					Item item = c.getEntity();
					c.close();

					mItemName = item.getName();

					mImagePath = item.getImagePath();
					mImageDefaultPath = item.getDefaultImagePath();
					loadImage(false);

					double amount = item.getItemData().getAmount();
					if (amount > 0) {
						mAmount.setText(new DecimalFormat("#.######").format(amount));
					}
					mUnits.setSelection(getPosition(mUnits, item.getItemData().getIdUnit()));
					double price = item.getItemData().getPrice();
					if (price > 0) {
						mPrice.setText(String.format("%.2f", price));
					}
					mComment.setText(item.getItemData().getComment());
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

		if (!mIsImageLoad) {
			Toast.makeText(getActivity(), getString(R.string.wait_load_image), Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!mNameInputLayout.isErrorEnabled() && !mAmountInputLayout.isErrorEnabled() &&
				!mPriceInputLayout.isErrorEnabled()) {
			Item item = getItem();
			setImagePath(item);

			addItem(item);
			long idItem = mIdSelectedItem;

			//Save item to list
			if (mInfo.getVisibility() == View.VISIBLE) { //If item in list, update it
				mItemsInListDS.update(getItemInList(item));
			} else { //Add new item to list
				idItem = mItemsInListDS.add(getItemInList(item));
			}

			sendResult(idItem);
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
		if (mIdSelectedItem != -1 && mImageDefaultPath != null) {
			loadImage(true);
		} else {
			mImagePath = null;
			mAppBarImage.setImageResource(android.R.color.transparent);
		}
	}

	private void addItem(Item item) {
		if (mIdSelectedItem != -1) {
			item.setId(mIdSelectedItem);
			item.getItemData().setId(mItemDS.getIdData(mIdSelectedItem));
			mItemDS.update(item);
		} else {
			mIdSelectedItem = (int) mItemDS.add(item);
			item.setId(mIdSelectedItem);
		}
	}

	private void setImagePath(Item item) {
		if (mImagePath == null) {
			mImagePath = Image.CHARACTER_IMAGE_PATH + (int) item.getName().toUpperCase().charAt(0) + ".png";
			mImageDefaultPath = mImagePath;

			item.setDefaultImagePath(mImagePath);
			item.setImagePath(mImagePath);
		} else {
			if (mImageDefaultPath == null) {
				mImageDefaultPath = mImagePath;
			}
			item.setDefaultImagePath(mImageDefaultPath);
		}
	}

	private int getPosition(Spinner spinner, String name) {
		int index = 0;
		for (int i = 0; i < spinner.getCount(); i++) {
			if (((UnitsDS.UnitCursor) spinner.getItemAtPosition(i)).getEntity().getName().equals(name)) {
				index = i;
				break;
			}
		}
		return index;
	}
}
