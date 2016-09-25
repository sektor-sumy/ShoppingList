package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdView;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.adapter.SpinnerColorAdapter;
import ru.android.ainege.shoppinglist.db.TableInterface.ItemsInterface;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS.CurrencyCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.entities.Item;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.activities.CatalogsActivity;
import ru.android.ainege.shoppinglist.ui.activities.SingleFragmentActivity;
import ru.android.ainege.shoppinglist.ui.fragments.OnCreateViewListener;
import ru.android.ainege.shoppinglist.ui.fragments.QuestionDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.ui.view.PictureView;
import ru.android.ainege.shoppinglist.ui.view.spinners.CategorySpinner;
import ru.android.ainege.shoppinglist.ui.view.spinners.GeneralSpinner;
import ru.android.ainege.shoppinglist.ui.view.spinners.UnitSpinner;
import ru.android.ainege.shoppinglist.util.AndroidBug5497Workaround;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.Showcase;
import ru.android.ainege.shoppinglist.util.Validation;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public abstract class ItemFragment extends Fragment implements PictureView.PictureInterface, OnBackPressedListener, SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String ID_ITEM = "idItem";
	private static final int IS_SAVE_CHANGES = 401;
	private static final String IS_SAVE_CHANGES_DATE = "answerDialog";
	private static final String STATE_FILE = "state_file";
	protected static final String STATE_IMAGE_PATH = "state_image_path";
	protected static final String STATE_CATEGORY_ID = "state_category_id";

	protected CollapsingToolbarLayout mCollapsingToolbarLayout;
	protected TextInputLayout mNameInputLayout;
	protected AutoCompleteTextView mNameTextView;
	protected TextView mInfoTextView;
	protected TextInputLayout mAmountInputLayout;
	protected EditText mAmountEditText;
	protected TextInputLayout mPriceInputLayout;
	protected EditText mPriceEditText;
	protected EditText mCommentEditText;
	private ToggleButton mIsBoughtButton;
	private AppBarLayout mAppBarLayout;
	private TextView mCurrencyTextView;
	private TextView mFinishPriceTextView;

	protected PictureView mPictureView;
	protected UnitSpinner mUnitSpinner;
	protected CategorySpinner mCategorySpinner;

	protected ItemDS mItemDS;
	protected ShoppingListDS mItemsInListDS;

	private OnCreateViewListener mOnCreateViewListener;
	private OnClickListener mOnClickListener;
	private OnItemChangedListener mOnItemChangedListener;

	protected ShoppingList mItemInList;
	protected boolean mIsProposedItem = false;
	protected boolean mIsAdded = false;
	private String mCurrencyList;
	private boolean mIsOpenedKeyboard = false;
	private boolean mIsExpandedAppbar = true;
	private boolean mIsLandscapePhone;
	private int mScreenAppHeight;

	protected SharedPreferences mPrefs;
	protected boolean mIsUseCategory;
	protected boolean mIsUpdateSL = false;

	protected abstract TextWatcher getNameChangedListener();
	protected abstract SimpleCursorAdapter getCompleteTextAdapter();
	protected abstract boolean saveData();
	protected abstract long getIdList();

	public interface OnClickListener {
		void onItemSave(long id, boolean isAdded, boolean isClose);
		void onImageClick();
		void onNotSave();
		void toPreviousScreen();
	}

	public interface OnItemChangedListener {
		void updateCurrentList();
	}

	@TargetApi(23)
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mOnCreateViewListener = (OnCreateViewListener) getActivity();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			mOnCreateViewListener = (OnCreateViewListener) getActivity();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Fade());
		} else {
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}

		mItemDS = new ItemDS(getActivity());
		mItemsInListDS = new ShoppingListDS(getActivity());

		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mPrefs.registerOnSharedPreferenceChangeListener(this);

		mPictureView = new PictureView(this, this);
		mUnitSpinner = new UnitSpinner(this);
		mCategorySpinner = new CategorySpinner(this);

		if (savedInstanceState != null) {
			mPictureView.setFile((File) savedInstanceState.getSerializable(STATE_FILE));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_item, container, false);

		getCurrencyFromDb();
		initKeyboardListener(v);

		setupView(v, savedInstanceState);
		showCaseViews();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mAppBarLayout.setExpanded(true);
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		closeKeyboard();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mOnCreateViewListener = null;
		mOnClickListener = null;
		mOnItemChangedListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(STATE_IMAGE_PATH, mItemInList.getItem().getImagePath());
		outState.putSerializable(STATE_FILE, mPictureView.getFile());
		outState.putLong(STATE_CATEGORY_ID, mCategorySpinner.getSelected().getId());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case PictureView.TAKE_PHOTO:
					mPictureView.takePhotoResult();
					break;
				case PictureView.FROM_GALLERY:
					mPictureView.fromGalleryResult();
					break;
				case IS_SAVE_CHANGES:
					saveItem(true);
					break;
				case UnitSpinner.UNIT_ADD:
					mUnitSpinner.updateSpinner(data.getLongExtra(GeneralDialogFragment.ID_ITEM, 1), false);
					break;
				case CategorySpinner.CATEGORY_ADD:
					mCategorySpinner.updateSpinner(data.getLongExtra(GeneralDialogFragment.ID_ITEM, 1), false);
					break;
				case SingleFragmentActivity.CATALOGS:
				case SingleFragmentActivity.SETTINGS:
					HashMap<Integer, Long> modifyCatalog = (HashMap<Integer, Long>) data.getSerializableExtra(CatalogsActivity.LAST_EDIT);

					if (modifyCatalog != null) {
						mIsUpdateSL = true;

						if (modifyCatalog.containsKey(R.string.catalogs_key_item) &&
								mItemInList.getIdItem() == modifyCatalog.get(R.string.catalogs_key_item)) {
							ItemDS.ItemCursor itemCursor = mItemDS.getWithData(mItemInList.getIdItem());

							if (itemCursor.moveToFirst()) {
								Item item = itemCursor.getEntity();

								loadImage(item.getImagePath());
								mNameTextView.setText(item.getName());
								mItemInList.getItem().setName(item.getName());
							} else {
								if (mOnClickListener != null) {
									mOnClickListener.toPreviousScreen();
								}
							}

							itemCursor.close();
						}

						if (modifyCatalog.containsKey(R.string.catalogs_key_category)) {
							mCategorySpinner.updateSpinner(modifyCatalog.get(R.string.catalogs_key_category), true);
						}

						if (modifyCatalog.containsKey(R.string.catalogs_key_unit)) {
							mUnitSpinner.updateSpinner(modifyCatalog.get(R.string.catalogs_key_unit), true);
						}

						if (modifyCatalog.containsKey(R.string.catalogs_key_currency)) {
							updateCurrency();
						}

						if (mOnItemChangedListener != null) {
							mOnItemChangedListener.updateCurrentList();
						}
					}

					break;
			}
		} else {
			switch (requestCode) {
				case PictureView.TAKE_PHOTO:
				case PictureView.FROM_GALLERY:
					mPictureView.cancelResult();
					break;
				case IS_SAVE_CHANGES:
					if (isDeleteImage(null)) {
						Image.deleteFile(mItemInList.getItem().getImagePath());
					}

					if (mIsUpdateSL) {
						sendResult(-1);
					}

					if (mOnClickListener != null) {
						mOnClickListener.onNotSave();
					}
					break;
				case UnitSpinner.UNIT_ADD:
					mUnitSpinner.setSelected(GeneralSpinner.ID_ADD_CATALOG);
					break;
				case CategorySpinner.CATEGORY_ADD:
					mCategorySpinner.setSelected(GeneralSpinner.ID_ADD_CATALOG);
					break;
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mPictureView.onCreateContextMenu(menu, R.id.default_image, getString(R.string.item_image));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return mPictureView.onContextItemSelected(item, mOnClickListener) || super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		mPictureView.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public boolean onBackPressed() {
		boolean result = true;

		if (mNameTextView.length() != 0) {
			QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_save_item), -1);
			dialogFrag.setTargetFragment(this, IS_SAVE_CHANGES);
			dialogFrag.show(getFragmentManager(), IS_SAVE_CHANGES_DATE);

			result = false;
		} else {
			if (mIsAdded && isDeleteImage(null)) {
				Image.deleteFile(mItemInList.getItem().getImagePath());
			}

			if (mIsUpdateSL) {
				sendResult(-1);
			}
		}

		return result;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (isAdded()) {
			if (key.equals(getString(R.string.settings_key_use_category))) {
				updateUseCategory(getView());
			} else if (key.equals(getString(R.string.settings_key_fast_edit))) {
				mUnitSpinner.updateSpinner(GeneralSpinner.ID_ADD_CATALOG, true);
				mCategorySpinner.updateSpinner(GeneralSpinner.ID_ADD_CATALOG, true);
			}
		}
	}

	@Override
	public void loadImage(String path) {
		mPictureView.loadImage(path, mItemInList.getItem().getImagePath());
		mItemInList.getItem().setImagePath(path);
		mCollapsingToolbarLayout.setTitle("");
	}

	public void setListeners(OnClickListener onClickListener, OnItemChangedListener onItemChangedListener) {
		setOnClickListener(onClickListener);
		setOnItemChangedListener(onItemChangedListener);
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		mOnClickListener = onClickListener;
	}

	public void setOnItemChangedListener(OnItemChangedListener onItemChangedListener) {
		mOnItemChangedListener = onItemChangedListener;
	}

	public void setIsBoughtButton(boolean isBoughtButton) {
		mIsBoughtButton.setChecked(isBoughtButton);
	}

	public void updateCurrency() {
		getCurrencyFromDb();
		mCurrencyTextView.setText(mCurrencyList);

		if (mFinishPriceTextView.getVisibility() == View.VISIBLE) {
			setFinishPrice();
		}
	}

	protected void setupView(View v, final Bundle savedInstanceState) {
		initToolbar(v);

		if (mIsLandscapePhone && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.primary_dark));
		}

		mAppBarLayout = (AppBarLayout) v.findViewById(R.id.appbar);
		mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
			@Override
			public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
				if (!mIsOpenedKeyboard) {
					mIsExpandedAppbar = (verticalOffset == 0);
				}
			}
		});

		mCollapsingToolbarLayout = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);
		mPictureView.setImage(v.findViewById(R.id.appbar_image));
		mIsBoughtButton = (ToggleButton) v.findViewById(R.id.is_bought);


		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mNameTextView = (AutoCompleteTextView) v.findViewById(R.id.new_item_name);
		mNameTextView.addTextChangedListener(getNameChangedListener());
		mNameTextView.setAdapter(getCompleteTextAdapter());
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_name), false)) {
			mNameTextView.setSelectAllOnFocus(true);
		}

		mInfoTextView = (TextView) v.findViewById(R.id.info);

		mAmountInputLayout = (TextInputLayout) v.findViewById(R.id.amount_input_layout);
		mAmountEditText = (EditText) v.findViewById(R.id.new_amount_item);
		mAmountEditText.addTextChangedListener(getAmountChangedListener());
		mAmountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus){
					onElementFocused(mAmountEditText, R.string.settings_key_text_selection_amount);
				}
			}
		});
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_amount), false)) {
			mAmountEditText.setSelectAllOnFocus(true);
		}

		mUnitSpinner.setSpinner(v.findViewById(R.id.amount_unit), true);

		mPriceInputLayout = (TextInputLayout) v.findViewById(R.id.price_input_layout);
		mPriceEditText = (EditText) v.findViewById(R.id.new_item_price);
		mPriceEditText.addTextChangedListener(getPriceChangedListener());
		mPriceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					onElementFocused(mPriceEditText, R.string.settings_key_text_selection_price);
				}
			}
		});
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_price), false)) {
			mPriceEditText.setSelectAllOnFocus(true);
		}

		mCurrencyTextView = (TextView) v.findViewById(R.id.currency);
		mCurrencyTextView.setText(mCurrencyList);

		mFinishPriceTextView = (TextView) v.findViewById(R.id.finish_price);

		mCategorySpinner.setSpinner(v.findViewById(R.id.category), false);
		updateUseCategory(v);

		mCommentEditText = (EditText) v.findViewById(R.id.comment);
		mCommentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					onElementFocused(mCommentEditText, R.string.settings_key_text_selection_comment);
				}
			}
		});
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_comment), false)) {
			mCommentEditText.setSelectAllOnFocus(true);
		}
	}

	protected SimpleCursorAdapter getCompleteTextAdapter(FilterQueryProvider provider) {
		SimpleCursorAdapter completeTextAdapter = new SpinnerColorAdapter(getActivity(),
				R.layout.spinner_autocomplite, null, true);
		completeTextAdapter.setFilterQueryProvider(provider);
		completeTextAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cursor) {
				return cursor.getString(cursor.getColumnIndex(ItemsInterface.COLUMN_NAME));
			}
		});
		return completeTextAdapter;
	}

	protected void disableError(TextInputLayout field) {
		if (field.getError() != null) {
			field.setError(null);
			field.setErrorEnabled(false);
		}
	}

	protected String getName() {
		return mNameTextView.getText().toString().trim();
	}

	protected ShoppingList refreshItem() {
		double amount = 0;
		if (mAmountEditText.getText().length() > 0) {
			amount = Double.parseDouble(mAmountEditText.getText().toString().replace(',', '.'));
		}
		mItemInList.setAmount(amount);
		mItemInList.setUnit(mUnitSpinner.getSelected());

		double price = 0;
		if (mPriceEditText.getText().length() > 0) {
			price = Double.parseDouble(mPriceEditText.getText().toString().replace(',', '.'));
		}
		mItemInList.setPrice(price);

		mItemInList.setCategory(mCategorySpinner.getSelected());
		mItemInList.setComment(mCommentEditText.getText().toString());

		mItemInList.setBought(mIsBoughtButton.isChecked());

		return mItemInList;
	}

	protected void sendResult(long id) {
		getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra(ID_ITEM, id));
	}

	private void initKeyboardListener(final View v) {
		mIsLandscapePhone = getResources().getBoolean(R.bool.isLandscape) && getResources().getBoolean(R.bool.isPhone);

		AndroidBug5497Workaround.assistActivity(getActivity()).setOnOpenKeyboard(new AndroidBug5497Workaround.OnOpenKeyboardListener() {
			@Override
			public void isOpen(int screenAppHeight, AdView adView) {
				mIsOpenedKeyboard = true;
				mScreenAppHeight = screenAppHeight;

				adView.pause();
				adView.setVisibility(View.GONE);

				if (!mIsLandscapePhone) {
					View focusedView = v.findFocus();

					if (focusedView != null && !(focusedView instanceof LinearLayout) && !isViewVisible(focusedView)) {
						mAppBarLayout.setExpanded(false);
					}
				}
			}

			@Override
			public void isClose(final AdView adView) {
				if (mIsOpenedKeyboard) {
					adView.setVisibility(View.VISIBLE);
					adView.resume();

					mIsOpenedKeyboard = false;
				}

				if (!mIsLandscapePhone) {
					View focusedView = v.findFocus();

					if (focusedView != null) {
						focusedView.clearFocus();
					}

					if (mIsExpandedAppbar) {
						mAppBarLayout.setExpanded(true);
					}
				}
			}
		});
	}

	private void initToolbar(View v) {
		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		boolean isLandscapeTablet = getResources().getBoolean(R.bool.isTablet) && getResources().getBoolean(R.bool.isLandscape);

		if (isLandscapeTablet) {
			toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);

			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().onBackPressed();
				}
			});
		}

		Button save = (Button) v.findViewById(R.id.save_item);
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveItem(false);
			}
		});

		mOnCreateViewListener.onCreateViewListener(this, toolbar);
	}

	private void updateUseCategory(View v) {
		mIsUseCategory = mPrefs.getBoolean(getString(R.string.settings_key_use_category), true);
		LinearLayout categoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
		categoryContainer.setVisibility(mIsUseCategory ? View.VISIBLE : View.GONE);
		((SpinnerColorAdapter) mNameTextView.getAdapter()).updateUseCategoryFromSetting();
	}

	private void getCurrencyFromDb() {
		CurrencyCursor cursor = new CurrenciesDS(getActivity()).getByList(getIdList());
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
					if (!Validation.isAmountValid(s.toString().trim())) {
						mAmountInputLayout.setError(getString(R.string.error_value));
						mFinishPriceTextView.setVisibility(View.GONE);
					} else {
						disableError(mAmountInputLayout);

						if (mPriceEditText.getText().length() > 0) {
							setFinishPrice();
						}
					}
				} else {
					disableError(mAmountInputLayout);
					mFinishPriceTextView.setVisibility(View.GONE);
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
					if (!Validation.isPriceValid(s.toString().trim())) {
						mPriceInputLayout.setError(getString(R.string.error_value));
						mFinishPriceTextView.setVisibility(View.GONE);
					} else {
						disableError(mPriceInputLayout);

						if (mAmountEditText.getText().length() > 0) {
							setFinishPrice();
						}
					}
				} else {
					mFinishPriceTextView.setVisibility(View.GONE);
					disableError(mPriceInputLayout);
				}
			}
		};
	}

	private void setFinishPrice() {
		try {
			double amount = Double.parseDouble(mAmountEditText.getText().toString().replace(',', '.'));
			double price = Double.parseDouble(mPriceEditText.getText().toString().replace(',', '.'));
			String finalPriceText = getString(R.string.finish_price) + localValue(amount * price) + " " + mCurrencyList;
			mFinishPriceTextView.setText(finalPriceText);
			mFinishPriceTextView.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			mFinishPriceTextView.setVisibility(View.GONE);
		}
	}

	private String localValue(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(value);
	}

	private void saveItem(boolean isClose) {
		if (saveData()) {
			if (mOnClickListener != null) {
				closeKeyboard();
				mOnClickListener.onItemSave(mItemInList.getIdItem(), mIsAdded, isClose);
			}
		} else {
			Toast.makeText(getActivity().getApplicationContext(), R.string.info_wrong_value, Toast.LENGTH_LONG).show();
		}
	}

	private boolean isViewVisible(View v) {
		int[] array = new int[2];
		v.getLocationOnScreen(array);

		int viewBottom = array[1] + v.getHeight();
		return viewBottom < mScreenAppHeight;
	}

	private void onElementFocused(EditText v, int preff) {
		if (!mPrefs.getBoolean(getString(preff), false)) {
			v.setSelection(v.getText().length());
		}

		if (mIsOpenedKeyboard && !isViewVisible(v)) {
			mAppBarLayout.setExpanded(false);
		}
	}

	private void closeKeyboard() {
		View view = getActivity().getCurrentFocus();

		if (view != null) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	private void showCaseViews() {
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_ITEM);
		sequence.setConfig(new ShowcaseConfig());

		MaterialShowcaseView.Builder builder = Showcase.createShowcase(getActivity(), mPictureView.getImage(),
				getString(R.string.showcase_update_image_item));

		if (!getResources().getBoolean(R.bool.isLandscape)) {
			builder.withRectangleShape(true);
		}

		sequence.addSequenceItem(builder.setShapePadding(0).build());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), mIsBoughtButton,
				getString(R.string.showcase_bought_item)).build());

		sequence.start();
	}
}
