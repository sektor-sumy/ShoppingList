package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.text.NumberFormat;
import java.util.Date;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource.CurrencyCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDataSource;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.SettingsDataItem;
import ru.android.ainege.shoppinglist.ui.Validation;

public abstract class ItemFragment extends Fragment implements SettingsDataItem {
	private static final String ID_ITEM = "idItem";

	protected String mDataSave;

	boolean mIsAlwaysSave = false;
	boolean mIsProposedItem = false;
	private String mCurrencyList;
	protected String mImagePath;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getSettings();

		if (ALWAYS_SAVE_DATA.equals(mDataSave)) {
			mIsAlwaysSave = true;
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.item_menu, menu);
		if (SAVE_DATA_BUTTON.equals(mDataSave)) {
			menu.findItem(R.id.update_item).setVisible(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.update_item:
				if (saveData(true)) {
					Toast.makeText(getActivity(), R.string.data_is_save, Toast.LENGTH_LONG).show();
				}
				return true;
			case R.id.save_item:
				Boolean wantToCloseDialog = saveData(false);
				if (wantToCloseDialog) {
					getActivity().onBackPressed();
				} else {
					Toast.makeText(getActivity(), R.string.wrong_value, Toast.LENGTH_LONG).show();
				}
				return true;
			case R.id.take_photo:
				Toast.makeText(getActivity(), "new photo", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.select_from_gallery:
				Toast.makeText(getActivity(), "image from gallery", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.reset_image:
				Toast.makeText(getActivity(), "random image", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		getSettings();
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
		mUnits.setSelection(0);

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
				return ((ItemDataSource.ItemCursor) cursor).getEntity().getName();
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
		long idUnit = ((UnitsDataSource.UnitCursor) mUnits.getSelectedItem()).getEntity().getId();
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
			} else if (save.contains(getString(R.string.data_item_never))) {
				mDataSave = SettingsDataItem.NEVER_SAVE_DAT;
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

	protected void loadImage() {
		Image.create().insertImageToView(getActivity(), mImagePath, mAppBarImage);
	}
}
