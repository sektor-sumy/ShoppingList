package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.view.View;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.text.DecimalFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS.ItemCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;

import static java.lang.String.format;

public class EditItemFragment extends ItemFragment {
	private static final String ITEM_IN_LIST = "itemInList";

	private long mOriginalIdItem;

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
		mOriginalIdItem = mItemInList.getIdItem();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Fade());
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
	protected void setupView(View v, Bundle savedInstanceState) {
		super.setupView(v, savedInstanceState);

		if (savedInstanceState == null) {
			setDataToView(savedInstanceState);
		} else {
			loadImage(dataFragment.getImagePath());
		}
	}

	private void setDataToView(Bundle savedInstanceState) {
		loadImage(mItemInList.getItem().getImagePath());

		mName.setText(mItemInList.getItem().getName());

		if (mItemInList.getAmount() != 0) {
			mAmount.setText(new DecimalFormat("#.######").format(mItemInList.getAmount()));
		}

		setSelectionUnit(mItemInList.getUnit().getId());

		if (mItemInList.getPrice() != 0) {
			mPrice.setText(format("%.2f", mItemInList.getPrice()));
		}

		if (savedInstanceState != null && !mIsUseCategory) {
			setSelectionCategory(savedInstanceState.getLong(STATE_CATEGORY_ID));
		} else {
			setSelectionCategory(mItemInList.getCategory().getId());
		}

		mComment.setText(mItemInList.getComment());

		setIsBought(mItemInList.isBought());
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
					//Check is the item in the list or catalog of items. If there is a warning display
					ShoppingListCursor cursor = mItemsInListDS.getByName(s.toString().trim(), mItemInList.getIdList());
					showInfo(cursor.moveToFirst() && !cursor.getEntity().getItem().getName().equals(mItemInList.getItem().getName()));
					cursor.close();
					if (mIsProposedItem) {
						ItemCursor cursorItem = mItemDS.getWithData(s.toString().trim());
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
				if (charSequence != null && !charSequence.toString().trim().equals(mItemInList.getItem().getName())) {
					managedCursor = mItemDS.getNames(charSequence.toString().trim());
					if (managedCursor.moveToFirst()) {
						mIsProposedItem = true;
					}
				}
				return managedCursor;
			}
		});
	}

	@Override
	protected boolean saveData() {
		boolean isSave = false;

		if (mName.length() < 3) {
			mNameInputLayout.setError(getString(R.string.error_length_name));
		}

		if (dataFragment.isLoading()) {
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.wait_load_image), Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!mNameInputLayout.isErrorEnabled() && !mAmountInputLayout.isErrorEnabled() &&
				!mPriceInputLayout.isErrorEnabled()) {
			updateItemInList();
			boolean isItemChanged = mItemInList.getItem().isNew();
			mItemInList.updateItem(getActivity());

			if (!isItemChanged) {
				mItemsInListDS.update(mItemInList);
			} else {
				mItemsInListDS.update(mItemInList, mOriginalIdItem);
			}

			sendResult(mItemInList.getIdItem());
			isSave = true;
		}
		return isSave;
	}

	@Override
	protected long getIdList() {
		return mItemInList.getIdList();
	}

	@Override
	protected void updatedItem() {
		if (!getName().equals(mItemInList.getItem().getName())) {
			mItemInList.setItem(new Item(getName(), mItemInList.getItem().getImagePath(), mItemInList.getItem().getImagePath()));
		}
	}

	@Override
	protected void resetImage() {
		if (!mItemInList.getItem().getImagePath().equals(mItemInList.getItem().getDefaultImagePath())) {
			loadImage(mItemInList.getItem().getDefaultImagePath());
		}
	}

	@Override
	public void onBackPressed() {
		if (!mItemInList.equals(mItemInList)) {
			super.onBackPressed();
		} else {
			getActivity().finish();
		}
	}
}
