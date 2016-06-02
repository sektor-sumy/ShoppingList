package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ITable.ICategories;
import ru.android.ainege.shoppinglist.db.ITable.IItems;
import ru.android.ainege.shoppinglist.db.ITable.IUnits;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS.CurrencyCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Dictionary;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.activities.ItemActivity;
import ru.android.ainege.shoppinglist.ui.activities.ListsActivity;
import ru.android.ainege.shoppinglist.ui.activities.SettingsDictionaryActivity;
import ru.android.ainege.shoppinglist.ui.fragments.QuestionDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.RetainedFragment;
import ru.android.ainege.shoppinglist.ui.fragments.settings.CategoryDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.settings.DictionaryFragment;
import ru.android.ainege.shoppinglist.ui.fragments.settings.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.settings.UnitDialogFragment;
import ru.android.ainege.shoppinglist.util.AndroidBug5497Workaround;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.Showcase;
import ru.android.ainege.shoppinglist.util.Validation;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.android.ainege.shoppinglist.db.dataSources.GenericDS.EntityCursor;

public abstract class ItemFragment extends Fragment implements ItemActivity.OnBackPressedInterface {
	public static final String ID_ITEM = "idItem";
	protected static final String UNIT_ADD_DATE = "addItemDialog";
	protected static final String CATEGORY_ADD_DATE = "addItemDialog";
	private static final int TAKE_PHOTO = 0;
	private static final int LOAD_IMAGE = 1;
	private static final int IS_SAVE_CHANGES = 2;
	private static final int UNIT_SETTINGS = 3;
	private static final int CATEGORY_SETTINGS = 4;
	private static final int UNIT_ADD = 5;
	private static final int CATEGORY_ADD = 6;
	private static final String IS_SAVE_CHANGES_DATE = "answerDialog";
	private static final String RETAINED_FRAGMENT = "retained_fragment_item";
	private static final String STATE_FILE = "state_file";

	protected ImageView mAppBarImage;
	protected CollapsingToolbarLayout mCollapsingToolbarLayout;
	protected TextInputLayout mNameInputLayout;
	protected AutoCompleteTextView mName;
	protected TextView mInfo;
	protected TextInputLayout mAmountInputLayout;
	protected EditText mAmount;
	protected Spinner mUnit;
	protected TextInputLayout mPriceInputLayout;
	protected EditText mPrice;
	protected Spinner mCategory;
	protected EditText mComment;
	protected ItemDS mItemDS;
	protected ShoppingListDS mItemsInListDS;
	protected ShoppingList mItemInList;
	protected boolean mIsProposedItem = false;
	protected SharedPreferences mPrefs;
	protected boolean mIsUseNewItemInSpinner;
	protected RetainedFragment dataFragment;
	private ToggleButton mIsBought;
	private AppBarLayout mAppBarLayout;
	private TextView mCurrency;
	private ImageButton mCategorySettings;
	private ImageButton mUnitSettings;
	private LinearLayout mCategoryContainer;
	private String mCurrencyList;
	private File mFile;
	private TextView mFinishPrice;
	private long mUnitPosition;
	private long mCategoryPosition;
	private boolean mIsOpenedKeyboard = false;
	private boolean mIsExpandedAppbar = true;
	private boolean mIsUseCategory;
	private boolean mIsPortraitOrientation;
	private OnItemChangeListener mItemChangeListener;

	protected abstract TextWatcher getNameChangedListener();

	protected abstract SimpleCursorAdapter getCompleteTextAdapter();

	protected abstract boolean saveData();

	protected abstract long getIdList();

	protected abstract void updatedItem();

	protected abstract void resetImage();

	public interface OnItemChangeListener {
		void onItemSave(long id);
	}

	@TargetApi(23)
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		if (getActivity() instanceof ListsActivity) {
			mItemChangeListener = (OnItemChangeListener) getActivity();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && getActivity() instanceof ListsActivity) {
			mItemChangeListener = (OnItemChangeListener) getActivity();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mItemDS = new ItemDS(getActivity());
		mItemsInListDS = new ShoppingListDS(getActivity());

		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mIsUseNewItemInSpinner = mPrefs.getBoolean(getString(R.string.settings_key_fast_edit), false);

		if (savedInstanceState != null) {
			mFile = (File) savedInstanceState.getSerializable(STATE_FILE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_item, container, false);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mIsPortraitOrientation = false;
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mIsPortraitOrientation = true;
			AndroidBug5497Workaround.assistActivity(getActivity()).setOnOpenKeyboard(new AndroidBug5497Workaround.OnOpenKeyboard() {
				@Override
				public void isOpen() {
					mIsOpenedKeyboard = true;

					if (mIsExpandedAppbar) {
						mAppBarLayout.setExpanded(false);
					}
				}

				@Override
				public void isClose() {
					mIsOpenedKeyboard = false;

					if (mIsExpandedAppbar) {
						mAppBarLayout.setExpanded(true);
					}
				}
			});
		}

		setCurrency();

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

		if (mItemChangeListener != null && getResources().getBoolean(R.bool.isLandscapeTablet)) {
			toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
		} else {
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		}

		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		Button save = (Button) v.findViewById(R.id.save_item);
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveItem();
			}
		});

		mAppBarImage = (ImageView) v.findViewById(R.id.appbar_image);
		registerForContextMenu(mAppBarImage);
		mAppBarImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.showContextMenu();
			}
		});

		FragmentManager fm = getFragmentManager();
		dataFragment = (RetainedFragment) fm.findFragmentByTag(RETAINED_FRAGMENT);

		if (dataFragment == null || savedInstanceState == null) {
			dataFragment = new RetainedFragment();
			fm.beginTransaction().add(dataFragment, RETAINED_FRAGMENT).commit();
		}

		dataFragment.setOnLoadedFinish(new RetainedFragment.ImageLoad() {
			@Override
			public void finish(String path) {
				loadImage(path);
			}
		});

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
	public void onDestroy() {
		super.onDestroy();
		dataFragment.setImagePath(mItemInList.getItem().getImagePath());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_FILE, mFile);
	}

	private void showCaseViews() {
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_ITEM);
		sequence.setConfig(new ShowcaseConfig());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), mAppBarImage,
				getString(R.string.showcase_update_image_item))
				.withRectangleShape(true)
				.build());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), mIsBought,
				getString(R.string.showcase_bought_item)).build());

		sequence.start();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		getActivity().getMenuInflater().inflate(R.menu.image_menu, menu);
		menu.findItem(R.id.default_image).setVisible(true);
		menu.setHeaderTitle(getString(R.string.item_image));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.take_photo:
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mFile = Image.create().createImageFile();

				if (mFile != null) {
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
					startActivityForResult(cameraIntent, TAKE_PHOTO);
				} else {
					Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
				}

				return true;
			case R.id.select_from_gallery:
				Intent galleryIntent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(galleryIntent, LOAD_IMAGE);
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
			switch (requestCode) {
				case IS_SAVE_CHANGES:
					getActivity().finish();
					break;
				case UNIT_ADD:
					mUnit.setSelection(getPosition(mUnit, mUnitPosition));
					break;
				case CATEGORY_ADD:
					mCategory.setSelection(getPosition(mCategory, mCategoryPosition));
					break;
			}

			return;
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		switch (requestCode) {
			case TAKE_PHOTO:
				deletePhotoFromGallery();
				dataFragment.execute(mFile, metrics.widthPixels - 30);
				break;
			case LOAD_IMAGE:
				try {
					File file = Image.create().createImageFile();

					if (file != null) {
						Uri selectedImage = data.getData();
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
						dataFragment.execute(file, bitmap, metrics.widthPixels - 30);
					} else {
						Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				break;
			case IS_SAVE_CHANGES:
				saveItem();
				break;
			case UNIT_SETTINGS:
				mUnit.setAdapter(getUnitsAdapter());
				long idUnit = data.getLongExtra(DictionaryFragment.LAST_EDIT, -1);
				mUnit.setSelection(idUnit == -1 ? getPosition(mUnit, mUnitPosition) : getPosition(mUnit, idUnit));
				break;
			case CATEGORY_SETTINGS:
				mCategory.setAdapter(getCategoriesAdapter());
				long idCategory = data.getLongExtra(DictionaryFragment.LAST_EDIT, -1);
				mCategory.setSelection(idCategory == -1 ? getPosition(mCategory, mCategoryPosition) : getPosition(mCategory, idCategory));
				break;
			case UNIT_ADD:
				mUnit.setAdapter(getUnitsAdapter());
				mUnit.setSelection(getPosition(mUnit, data.getLongExtra(GeneralDialogFragment.ID_ITEM, 1)));
				break;
			case CATEGORY_ADD:
				mCategory.setAdapter(getCategoriesAdapter());
				mCategory.setSelection(getPosition(mCategory, data.getLongExtra(GeneralDialogFragment.ID_ITEM, 1)));
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (mName.length() != 0) {
			QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_save_item));
			dialogFrag.setTargetFragment(ItemFragment.this, IS_SAVE_CHANGES);
			dialogFrag.show(getFragmentManager(), IS_SAVE_CHANGES_DATE);
		} else {
			getActivity().finish();
		}
	}

	public void setIsBought(boolean isBought) {
		mIsBought.setChecked(isBought);
	}

	public void updateSpinners() {
		mIsUseNewItemInSpinner = mPrefs.getBoolean(getString(R.string.settings_key_fast_edit), false);

		long idUnit = ((UnitsDS.UnitCursor) mUnit.getSelectedItem()).getEntity().getId();
		mUnit.setAdapter(getUnitsAdapter());
		mUnit.setSelection(getPosition(mUnit, idUnit));

		long idCategory = ((CategoriesDS.CategoryCursor) mCategory.getSelectedItem()).getEntity().getId();
		mCategory.setAdapter(getCategoriesAdapter());
		mCategory.setSelection(getPosition(mCategory, idCategory));
	}

	public void setTransitionButtons() {
		if (mPrefs.getBoolean(getString(R.string.settings_key_transition), false)) {
			mUnitSettings.setVisibility(View.VISIBLE);
			mCategorySettings.setVisibility(View.VISIBLE);
			mCurrency.getLayoutParams().width = getResources().getDimensionPixelOffset(R.dimen.width_currency);
		} else {
			mUnitSettings.setVisibility(View.GONE);
			mCategorySettings.setVisibility(View.GONE);
			mCurrency.getLayoutParams().width = getResources().getDimensionPixelOffset(R.dimen.width_spinner);
		}
	}

	public void setCategory() {
		mIsUseCategory = mPrefs.getBoolean(getString(R.string.settings_key_use_category), true);
		mCategoryContainer.setVisibility(mIsUseCategory ? View.VISIBLE : View.GONE);
	}

	protected void setupView(View v, final Bundle savedInstanceState) {
		mAppBarLayout = (AppBarLayout) v.findViewById(R.id.appbar);

		if (mIsPortraitOrientation) {
			mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
				@Override
				public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
					if (!mIsOpenedKeyboard) {
						mIsExpandedAppbar = (verticalOffset == 0);
					}
				}
			});
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.primary_dark));
			}
		}

		mCollapsingToolbarLayout = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);

		mInfo = (TextView) v.findViewById(R.id.info);

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (AutoCompleteTextView) v.findViewById(R.id.new_item_name);
		mName.addTextChangedListener(getNameChangedListener());
		mName.setAdapter(getCompleteTextAdapter());
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_name), false)) {
			mName.setSelectAllOnFocus(true);
		}

		mFinishPrice = (TextView) v.findViewById(R.id.finish_price);

		mAmountInputLayout = (TextInputLayout) v.findViewById(R.id.amount_input_layout);
		mAmount = (EditText) v.findViewById(R.id.new_amount_item);
		mAmount.addTextChangedListener(getAmountChangedListener());
		mAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && !mPrefs.getBoolean(getString(R.string.settings_key_text_selection_amount), false)) {
					mAmount.setSelection(mAmount.getText().length());
				}
			}
		});
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_amount), false)) {
			mAmount.setSelectAllOnFocus(true);
		}

		mUnit = (Spinner) v.findViewById(R.id.amount_unit);
		mUnit.setAdapter(getUnitsAdapter());
		mUnit.post(new Runnable() {
			public void run() {
				mUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						if (position == 0 && id == -1) {
							GeneralDialogFragment addItemDialog = new UnitDialogFragment();
							addItemDialog.setTargetFragment(ItemFragment.this, UNIT_ADD);
							addItemDialog.show(getFragmentManager(), UNIT_ADD_DATE);
						} else {
							mUnitPosition = ((UnitsDS.UnitCursor) mUnit.getSelectedItem()).getEntity().getId();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}
				});
			}
		});
		mUnitSettings = (ImageButton) v.findViewById(R.id.unit_settings);
		mUnitSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				settingOnClickListener(getString(R.string.settings_key_unit), UNIT_SETTINGS);
			}
		});

		mPriceInputLayout = (TextInputLayout) v.findViewById(R.id.price_input_layout);
		mPrice = (EditText) v.findViewById(R.id.new_item_price);
		mPrice.addTextChangedListener(getPriceChangedListener());
		mPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && !mPrefs.getBoolean(getString(R.string.settings_key_text_selection_price), false)) {
					mPrice.setSelection(mPrice.getText().length());
				}
			}
		});
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_price), false)) {
			mPrice.setSelectAllOnFocus(true);
		}

		mCurrency = (TextView) v.findViewById(R.id.currency);
		mCurrency.setText(mCurrencyList);

		mCategory = (Spinner) v.findViewById(R.id.category);
		mCategory.setAdapter(getCategoriesAdapter());
		mCategory.post(new Runnable() {
			public void run() {
				mCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						if (position == 0 && id == -1) {
							GeneralDialogFragment addItemDialog = new CategoryDialogFragment();
							addItemDialog.setTargetFragment(ItemFragment.this, CATEGORY_ADD);
							addItemDialog.show(getFragmentManager(), CATEGORY_ADD_DATE);
						} else {
							mCategoryPosition = ((CategoriesDS.CategoryCursor) mCategory.getSelectedItem()).getEntity().getId();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}
				});
			}
		});
		mCategorySettings = (ImageButton) v.findViewById(R.id.category_settings);
		mCategorySettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				settingOnClickListener(getString(R.string.settings_key_category), CATEGORY_SETTINGS);
			}
		});
		mCategoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
		setCategory();

		mComment = (EditText) v.findViewById(R.id.comment);
		mComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && !mPrefs.getBoolean(getString(R.string.settings_key_text_selection_comment), false)) {
					mComment.setSelection(mComment.getText().length());
				}
			}
		});
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_comment), false)) {
			mComment.setSelectAllOnFocus(true);
		}

		mIsBought = (ToggleButton) v.findViewById(R.id.is_bought);

		setTransitionButtons();
	}

	protected void loadImage(String path) {
		mItemInList.getItem().setImagePath(path);

		Image.create().insertImageToView(getActivity(), path, mAppBarImage);
		mCollapsingToolbarLayout.setTitle("");
	}

	protected SimpleCursorAdapter getCompleteTextAdapter(FilterQueryProvider provider) {
		SimpleCursorAdapter completeTextAdapter = new ColorAdapter(getActivity(),
				R.layout.spinner_autocomplite, null);
		completeTextAdapter.setFilterQueryProvider(provider);
		completeTextAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cursor) {
				return cursor.getString(cursor.getColumnIndex(IItems.COLUMN_NAME));
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

	protected ShoppingList updateItemInList() {
		updatedItem();

		double amount = 0;
		if (mAmount.getText().length() > 0) {
			amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
		}
		mItemInList.setAmount(amount);
		mItemInList.setUnit(((UnitsDS.UnitCursor) mUnit.getSelectedItem()).getEntity());

		double price = 0;
		if (mPrice.getText().length() > 0) {
			price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
		}
		mItemInList.setPrice(price);

		mItemInList.setCategory(((CategoriesDS.CategoryCursor) mCategory.getSelectedItem()).getEntity());
		mItemInList.setComment(mComment.getText().toString());

		mItemInList.setBought(mIsBought.isChecked());
		return mItemInList;
	}

	protected String getName() {
		return mName.getText().toString().trim();
	}

	protected void sendResult(long id) {
		getActivity().setResult(android.app.Activity.RESULT_OK, new Intent().putExtra(ID_ITEM, id));
	}

	protected int getPosition(Spinner spinner, long id) {
		int index = -1;
		for (int i = 0; i < spinner.getCount(); i++) {
			if (((EntityCursor<Dictionary>) spinner.getItemAtPosition(i)).getEntity().getId() == id) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			index = mIsUseNewItemInSpinner ? 1 : 0;
		}
		return index;
	}

	private void setCurrency() {
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
					if (!Validation.isPriceValid(s.toString().trim())) {
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

	private SimpleCursorAdapter getUnitsAdapter() {
		Cursor cursor;

		if (mIsUseNewItemInSpinner) {
			cursor = new UnitsDS(getActivity()).getAllForSpinner();
		} else {
			cursor = new UnitsDS(getActivity()).getAll();
		}

		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.spinner_unit,
				cursor,
				new String[]{IUnits.COLUMN_NAME},
				new int[]{android.R.id.text1}, 0);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spinnerAdapter;
	}

	private SimpleCursorAdapter getCategoriesAdapter() {
		Cursor cursor;

		if (mIsUseNewItemInSpinner) {
			cursor = new CategoriesDS(getActivity()).getAllForSpinner();
		} else {
			cursor = new CategoriesDS(getActivity()).getAll();
		}

		SimpleCursorAdapter spinnerAdapter = new ColorAdapter(getActivity(),
				R.layout.spinner_color_item,
				cursor);
		spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_color_item);
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

	private void saveItem() {
		if (saveData()) {
			if (mItemChangeListener != null) {
				mItemChangeListener.onItemSave(mItemInList.getIdItem());
			} else {
				getActivity().finish();
			}
		} else {
			Toast.makeText(getActivity().getApplicationContext(), R.string.info_wrong_value, Toast.LENGTH_LONG).show();
		}
	}

	private void deletePhotoFromGallery() {
		String[] projection = {BaseColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN};

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

	private void settingOnClickListener(String value, int code) {
		Intent i = new Intent(getActivity(), SettingsDictionaryActivity.class);
		i.putExtra(SettingsDictionaryActivity.EXTRA_TYPE, value);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			startActivityForResult(i, code, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
		} else {
			startActivityForResult(i, code);
		}
	}

	private class ColorAdapter extends SimpleCursorAdapter {

		public ColorAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c, new String[]{}, new int[]{}, 0);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return setHolderToView(super.newView(context, cursor, parent));
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);

			ViewHolder holder = (ViewHolder) view.getTag();
			String name = "";

			if (cursor.getColumnIndex(IItems.COLUMN_NAME) != -1) {
				name = cursor.getString(cursor.getColumnIndex(IItems.COLUMN_NAME));
			} else if (cursor.getColumnIndex(ICategories.COLUMN_NAME) != -1) {
				name = cursor.getString(cursor.getColumnIndex(ICategories.COLUMN_NAME));
			}

			holder.mName.setText(name);

			if (mIsUseCategory) {
				holder.mColor.setBackgroundColor(cursor.getInt(cursor.getColumnIndex(ICategories.COLUMN_COLOR)));
			}
		}

		@Override
		public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
			return setHolderToView(super.newDropDownView(context, cursor, parent));
		}

		private View setHolderToView(View v) {
			ViewHolder holder = new ViewHolder(v);
			v.setTag(holder);

			return v;
		}

		private class ViewHolder {
			public TextView mName;
			public TextView mColor;

			ViewHolder(View v) {
				mName = (TextView) v.findViewById(R.id.name);
				mColor = (TextView) v.findViewById(R.id.color);
			}
		}
	}
}
