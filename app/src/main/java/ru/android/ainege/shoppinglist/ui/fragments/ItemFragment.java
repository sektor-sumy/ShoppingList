package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource.CurrencyCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.Image;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.Validation;

import static ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource.*;
import static ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource.*;

public abstract class ItemFragment extends Fragment implements SettingsDataItem, ImageFragmentInterface {
	private static final String ID_ITEM = "idItem";
	private static final int TAKE_PHOTO_CODE = 0;
	private static final int LOAD_IMAGE_CODE = 1;

	ItemDataSource mItemDS;
	ShoppingListDataSource mItemsInListDS;

	protected String mDataSave;

	boolean mIsSaveButton = false;
	boolean mIsProposedItem = false;
	private String mCurrencyList;
	private File mFile;
	protected String mImagePath;
	protected String mImageDefaultPath;
	protected boolean mIsImageLoad = true;
	private String mPhotoPath;

	ImageView mAppBarImage;
	CollapsingToolbarLayout mCollapsingToolbarLayout;
	TextInputLayout mNameInputLayout;
	AutoCompleteTextView mName;
	TextView mInfo;
	TextInputLayout mAmountInputLayout;
	EditText mAmount;
	Spinner mUnits;
	TextInputLayout mPriceInputLayout;
	EditText mPrice;
	EditText mComment;
	ToggleButton mIsBought;
	private TextView mFinishPrice;

	protected abstract TextWatcher getNameChangedListener();

	protected abstract SimpleCursorAdapter getCompleteTextAdapter();

	protected abstract boolean saveData(boolean isUpdateData);

	protected abstract long getIdList();

	protected abstract void resetImage();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mItemDS = new ItemDataSource(getActivity());
		mItemsInListDS = new ShoppingListDataSource(getActivity());

		getSettings();
		if (SAVE_DATA_BUTTON.equals(mDataSave)) {
			mIsSaveButton = true;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_item, container, false);

		getCurrency();

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		mAppBarImage = (ImageView) v.findViewById(R.id.appbar_image);

		setView(v);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		mName.setSelection(mName.getText().length());
		mAmount.setSelection(mAmount.getText().length());
		mPrice.setSelection(mPrice.getText().length());
		mComment.setSelection(mComment.getText().length());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.item_menu, menu);
		if (mIsSaveButton) {
			menu.findItem(R.id.update_item).setVisible(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.update_item:
				if (saveData(true)) {
					Toast.makeText(getActivity(), R.string.info_data_is_save, Toast.LENGTH_LONG).show();
				}
				return true;
			case R.id.save_item:
				Boolean wantToCloseDialog = saveData(false);
				if (wantToCloseDialog) {
					getActivity().onBackPressed();
				} else {
					Toast.makeText(getActivity(), R.string.info_wrong_value, Toast.LENGTH_LONG).show();
				}
				return true;
			case R.id.take_photo:
				mIsImageLoad = false;
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				mFile = Image.create().createImageFile();
				if (mFile != null) {
					mPhotoPath = Image.PATH_PROTOCOL + mFile.getAbsolutePath();

					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
					startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
				} else {
					Toast.makeText(getActivity(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.select_from_gallery:
				mIsImageLoad = false;
				Intent galleryIntent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(galleryIntent, LOAD_IMAGE_CODE);
				return true;
			case R.id.default_image:
				resetImage();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			mIsImageLoad = true;

			return;
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		switch (requestCode) {
			case TAKE_PHOTO_CODE:
				mImagePath = mPhotoPath;
				deletePhotoFromGallery();
				new Image.BitmapWorkerTask(mFile,  metrics.widthPixels - 30, this).execute();
				break;
			case LOAD_IMAGE_CODE:
				Uri selectedImage = data.getData();

				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

					Image image = Image.create();
					File file = image.createImageFile();

					if (file != null) {
						mPhotoPath = Image.PATH_PROTOCOL + file.getAbsolutePath();
						new Image.BitmapWorkerTask(file, bitmap, metrics.widthPixels - 30, this).execute();

						mImagePath = mPhotoPath;
					} else {
						Toast.makeText(getActivity(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
		}
	}

	@Override
	public void updateImage() {
		mIsImageLoad = true;
		loadImage(false);
	}

	void setView(View v) {
		mCollapsingToolbarLayout = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);

		mInfo = (TextView) v.findViewById(R.id.info);

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (AutoCompleteTextView) v.findViewById(R.id.new_item_name);
		mName.addTextChangedListener(getNameChangedListener());
		mName.setAdapter(getCompleteTextAdapter());

		mFinishPrice = (TextView) v.findViewById(R.id.finish_price);

		mAmountInputLayout = (TextInputLayout) v.findViewById(R.id.amount_input_layout);
		mAmount = (EditText) v.findViewById(R.id.new_amount_item);
		mAmount.addTextChangedListener(getAmountChangedListener());

		mUnits = (Spinner) v.findViewById(R.id.new_amount_units);
		mUnits.setAdapter(getSpinnerAdapter());

		mPriceInputLayout = (TextInputLayout) v.findViewById(R.id.price_input_layout);
		mPrice = (EditText) v.findViewById(R.id.new_item_price);
		mPrice.addTextChangedListener(getPriceChangedListener());

		TextView mCurrency = (TextView) v.findViewById(R.id.currency);
		mCurrency.setText(mCurrencyList);

		mComment = (EditText) v.findViewById(R.id.comment);

		mIsBought = (ToggleButton) v.findViewById(R.id.is_bought);
	}

	SimpleCursorAdapter getCompleteTextAdapter(FilterQueryProvider provider) {
		SimpleCursorAdapter completeTextAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line,
				null,
				new String[]{ItemsTable.COLUMN_NAME},
				new int[]{android.R.id.text1}, 0);
		completeTextAdapter.setFilterQueryProvider(provider);
		completeTextAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cursor) {
				return ((ItemCursor) cursor).getEntity().getName();
			}
		});
		return completeTextAdapter;
	}

	void disableError(TextInputLayout field) {
		if (field.getError() != null) {
			field.setError(null);
			field.setErrorEnabled(false);
		}
	}

	String getName() {
		return mName.getText().toString().trim();
	}

	Item getItem() {
		String name = getName();
		double amount = 0.0;
		if (mAmount.getText().length() > 0) {
			amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
		}
		long idUnit = ((UnitCursor) mUnits.getSelectedItem()).getEntity().getId();
		double price = 0.0;
		if (mPrice.getText().length() > 0) {
			price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
		}
		String comment = mComment.getText().toString();

		return new Item(name, amount, idUnit, price, comment, mImagePath);
	}

	ShoppingList getItemInList(Item item) {
		return new ShoppingList(item.getId(),
				getIdList(),
				mIsBought.isChecked(),
				item.getAmount(),
				item.getIdUnit(),
				item.getPrice(),
				item.getComment(),
				null
		);
	}

	void sendResult(long id) {
		getActivity().setResult(android.app.Activity.RESULT_OK, new Intent().putExtra(ID_ITEM, id));
	}

	private void getSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		if (!prefs.getBoolean(getString(R.string.settings_key_sort_is_default_data), true)) {
			mDataSave = SettingsDataItem.NOT_USE_DEFAULT_DATA;
		} else {
			String save = prefs.getString(getString(R.string.settings_key_sort_data_item), "");
			if (save.contains(getString(R.string.data_item_button))) {
				mDataSave = SettingsDataItem.SAVE_DATA_BUTTON;
			} else if (save.contains(getString(R.string.data_item_always))) {
				mDataSave = SettingsDataItem.ALWAYS_SAVE_DATA;
			}
		}
	}

	private void getCurrency() {
		CurrencyCursor cursor = new CurrenciesDataSource(getActivity()).getByList(getIdList());
		if (cursor.moveToFirst()) {
			mCurrencyList = cursor.getEntity().getSymbol();
		}
		cursor.close();
	}

	private TextWatcher getAmountChangedListener() {
		return new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s != null && s.length() > 0) {
					if (!Validation.isValid(s.toString().trim(), false)) {
						mAmountInputLayout.setError(getString(R.string.error_value));
					} else {
						disableError(mAmountInputLayout);
						if (mPrice.getText().length() > 0) {
							setFinishPrice();
						}
					}
				} else {
					disableError(mAmountInputLayout);
					mFinishPrice.setVisibility(View.GONE);
				}
			}
		};
	}

	private TextWatcher getPriceChangedListener() {
		return new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s != null && s.length() > 0) {
					if (!Validation.isValid(s.toString().trim(), true)) {
						mPriceInputLayout.setError(getString(R.string.error_value));
					} else {
						disableError(mPriceInputLayout);
						if (mAmount.getText().length() > 0) {
							setFinishPrice();
						}
					}
				} else {
					mFinishPrice.setVisibility(View.GONE);
					disableError(mPriceInputLayout);
				}
			}
		};
	}

	private SimpleCursorAdapter getSpinnerAdapter() {
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item,
				new UnitsDataSource(getActivity()).getAll(),
				new String[]{UnitsTable.COLUMN_NAME},
				new int[]{android.R.id.text1}, 0);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spinnerAdapter;
	}

	private void setFinishPrice() {
		double amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
		double price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
		String finalPriceText = getString(R.string.finish_price) + localValue(amount * price) + " " + mCurrencyList;
		mFinishPrice.setText(finalPriceText);
		mFinishPrice.setVisibility(View.VISIBLE);
	}

	private String localValue(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(value);
	}

	protected void loadImage(boolean isDefaultImage) {
		if (isDefaultImage) {
			mImagePath = mImageDefaultPath;
			Image.create().insertImageToView(getActivity(), mImageDefaultPath, mAppBarImage);
		} else {
			Image.create().insertImageToView(getActivity(), mImagePath, mAppBarImage);
		}
	}

	protected int getPosition(Spinner spinner, long id) {
		int index = 0;
		for (int i = 0; i < spinner.getCount(); i++) {
			if (((UnitCursor) spinner.getItemAtPosition(i)).getEntity().getId() == id) {
				index = i;
				break;
			}
		}
		return index;
	}

	private void deletePhotoFromGallery() {
		String[] projection = { BaseColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN };

		Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

		if ((cursor != null) && (cursor.moveToFirst())) {
			String id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
			long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));

			if (Math.abs(date - mFile.lastModified()) < 30000) {
				ContentResolver cr = getActivity().getContentResolver();
				cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + id, null);
			}
			cursor.close();
		}
	}
}
