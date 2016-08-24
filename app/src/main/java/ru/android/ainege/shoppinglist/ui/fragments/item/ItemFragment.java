package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdView;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.TableInterface.CategoriesInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.ItemsInterface;
import ru.android.ainege.shoppinglist.db.TableInterface.UnitsInterface;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS.CurrencyCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Catalog;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.OnFinishedImageListener;
import ru.android.ainege.shoppinglist.ui.activities.CatalogsActivity;
import ru.android.ainege.shoppinglist.ui.activities.SingleFragmentActivity;
import ru.android.ainege.shoppinglist.ui.fragments.OnCreateViewListener;
import ru.android.ainege.shoppinglist.ui.fragments.QuestionDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.RetainedFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.CategoryDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.UnitDialogFragment;
import ru.android.ainege.shoppinglist.util.AndroidBug5497Workaround;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.Showcase;
import ru.android.ainege.shoppinglist.util.Validation;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.android.ainege.shoppinglist.db.dataSources.GenericDS.EntityCursor;

public abstract class ItemFragment extends Fragment implements OnBackPressedListener, SharedPreferences.OnSharedPreferenceChangeListener {
	public static final String ID_ITEM = "idItem";
	protected static final String UNIT_ADD_DATE = "addUnitDialog";
	protected static final String CATEGORY_ADD_DATE = "addCategoryDialog";
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
	protected static final String STATE_CATEGORY_ID = "state_category_id";

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
	private ToggleButton mIsBought;
	private AppBarLayout mAppBarLayout;
	private TextView mCurrency;
	private LinearLayout mCategoryContainer;
	private TextView mFinishPrice;

	protected ItemDS mItemDS;
	protected ShoppingListDS mItemsInListDS;

	private OnCreateViewListener mOnCreateViewListener;
	private OnClickListener mOnClickListener;
	private OnItemChangedListener mOnItemChangedListener;

	protected ShoppingList mItemInList;
	protected long mIdSelectedUnit;
	protected long mIdSelectedCategory;
	protected boolean mIsProposedItem = false;
	protected boolean mIsAdded = false;
	private String mCurrencyList;
	private boolean mIsOpenedKeyboard = false;
	private boolean mIsExpandedAppbar = true;
	private boolean mIsCollapsedMode;
	private int mScreenAppHeight;

	protected SharedPreferences mPrefs;
	protected boolean mIsUseNewItemInSpinner;
	protected boolean mIsUseCategory;

	protected RetainedFragment dataFragment;
	private File mFile;

	protected abstract TextWatcher getNameChangedListener();

	protected abstract SimpleCursorAdapter getCompleteTextAdapter();

	protected abstract boolean saveData();

	protected abstract long getIdList();

	protected abstract void resetImage();

	protected abstract boolean isDeleteImage(String newPath);

	public interface OnClickListener {
		void onItemSave(long id, boolean isAdded, boolean isClose);
		void onImageClick();
		void onNotSave();
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

		mItemDS = new ItemDS(getActivity());
		mItemsInListDS = new ShoppingListDS(getActivity());

		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		mIsUseNewItemInSpinner = mPrefs.getBoolean(getString(R.string.settings_key_fast_edit), false);

		if (savedInstanceState != null) {
			mFile = (File) savedInstanceState.getSerializable(STATE_FILE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_item, container, false);

		if (getResources().getBoolean(R.bool.isLandscape) && getResources().getBoolean(R.bool.isPhone)) {
			//TODO: 15.08.2016  AdMob on it
			mIsCollapsedMode = false;
		} else {
			mIsCollapsedMode = true;
			View view = ((FrameLayout)  getActivity().findViewById(android.R.id.content)).getChildAt(0);
			final AdView adView = (AdView) ((RelativeLayout) ((DrawerLayout) view).getChildAt(0)).getChildAt(1);

			AndroidBug5497Workaround.assistActivity(getActivity()).setOnOpenKeyboard(new AndroidBug5497Workaround.OnOpenKeyboardListener() {
				@Override
				public void isOpen(int screenAppHeight) {
					mIsOpenedKeyboard = true;
					mScreenAppHeight = screenAppHeight;

					adView.setVisibility(View.GONE);
					View focusedView = v.findFocus();

					if (focusedView != null && !(focusedView instanceof LinearLayout) && !isViewVisible(focusedView)) {
						mAppBarLayout.setExpanded(false);
					}
				}

				@Override
				public void isClose() {
					if (mIsOpenedKeyboard) {
						adView.setVisibility(View.VISIBLE);
					}

					mIsOpenedKeyboard = false;
					View focusedView = v.findFocus();

					if (focusedView != null) {
						focusedView.clearFocus();
					}

					if (mIsExpandedAppbar) {
						mAppBarLayout.setExpanded(true);
					}
				}
			});
		}

		getCurrencyFromDb();

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
			dataFragment = new RetainedFragment(getActivity());
			fm.beginTransaction().add(dataFragment, RETAINED_FRAGMENT).commit();
		}

		dataFragment.setOnLoadedFinish(new OnFinishedImageListener() {
			@Override
			public void onFinished(boolean isSuccess, String path) {
				loadImage(path);
			}

			@Override
			public Activity getActivity() {
				return getActivity();
			}
		});

		setupView(v, savedInstanceState);
		showCaseViews();
		mOnCreateViewListener.onCreateViewListener(this, toolbar);

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

		closeKeyboard();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		dataFragment.setImagePath(mItemInList.getItem().getImagePath());

		mOnCreateViewListener = null;
		mOnClickListener = null;
		mOnItemChangedListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_FILE, mFile);
		outState.putLong(STATE_CATEGORY_ID, ((CategoriesDS.CategoryCursor) mCategory.getSelectedItem()).getEntity().getId());
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

	private void showCaseViews() {
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_ITEM);
		sequence.setConfig(new ShowcaseConfig());

		MaterialShowcaseView.Builder builder = Showcase.createShowcase(getActivity(), mAppBarImage,
				getString(R.string.showcase_update_image_item));

		if (!getResources().getBoolean(R.bool.isLandscape)) {
			builder.withRectangleShape(true);
		}

		sequence.addSequenceItem(builder.setShapePadding(0).build());

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
				if (mOnClickListener != null) {
					mOnClickListener.onImageClick();
				}
				if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					takePhoto();
				} else {
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, TAKE_PHOTO);
				}
				break;
			case R.id.select_from_gallery:
				if (mOnClickListener != null) {
					mOnClickListener.onImageClick();
				}
				if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					selectFromGallery();
				} else {
					requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, LOAD_IMAGE);
				}
				break;
			case R.id.default_image:
				resetImage();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			switch (requestCode) {
				case IS_SAVE_CHANGES:
					if (mOnClickListener != null) {
						mOnClickListener.onNotSave();
					}
					break;
				case UNIT_ADD:
					setSelectionUnit(-1);
					break;
				case CATEGORY_ADD:
					setSelectionCategory(-1);
					break;
			}

			return;
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		switch (requestCode) {
			case TAKE_PHOTO:
				Image.create().deletePhotoFromGallery(getActivity(), mFile);
				dataFragment.execute(mAppBarImage, mItemInList.getItem().getImagePath(), mFile, metrics.widthPixels - 30);
				break;
			case LOAD_IMAGE:
				File file = Image.create().createImageFile(getActivity());

				try {
					if (file != null) {
						Uri selectedImage = data.getData();
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
						dataFragment.execute(mAppBarImage, mItemInList.getItem().getImagePath(), file, bitmap, metrics.widthPixels - 30);
					} else {
						Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
					}
				} catch (OutOfMemoryError | Exception e) {
					e.printStackTrace();
					FirebaseCrash.report(new Exception(getResources().getString(R.string.catched_exception), e));
					Image.deleteFile(file.getAbsolutePath());
				}

				break;
			case IS_SAVE_CHANGES:
				saveItem(true);
				break;
			case UNIT_SETTINGS:
				if (mOnItemChangedListener != null) {
					mOnItemChangedListener.updateCurrentList();
				}
				setUnit(data.getLongExtra(CatalogsActivity.LAST_EDIT, -1));
				break;
			case CATEGORY_SETTINGS:
				if (mOnItemChangedListener != null) {
					mOnItemChangedListener.updateCurrentList();
				}
				setCategory(data.getLongExtra(CatalogsActivity.LAST_EDIT, -1));
				break;
			case UNIT_ADD:
				mUnit.setAdapter(getUnitsAdapter());
				setSelectionUnit(data.getLongExtra(GeneralDialogFragment.ID_ITEM, 1));
				break;
			case CATEGORY_ADD:
				mCategory.setAdapter(getCategoriesAdapter());
				setSelectionCategory(data.getLongExtra(GeneralDialogFragment.ID_ITEM, 1));
				break;
			case SingleFragmentActivity.CATALOGS:
			case SingleFragmentActivity.SETTINGS:
				HashMap<Integer, Long> modifyCatalog = (HashMap<Integer, Long>) data.getSerializableExtra(CatalogsActivity.LAST_EDIT);

				if (modifyCatalog != null) {
					if (modifyCatalog.containsKey(R.string.catalogs_key_item)) {

					}

					if (modifyCatalog.containsKey(R.string.catalogs_key_category)) {
						setCategory(modifyCatalog.get(R.string.catalogs_key_category));
					}

					if (modifyCatalog.containsKey(R.string.catalogs_key_unit)) {
						setUnit(modifyCatalog.get(R.string.catalogs_key_unit));
					}

					if (modifyCatalog.containsKey(R.string.catalogs_key_currency)) {
						setCurrency();
					}

					if (mOnItemChangedListener != null) {
						mOnItemChangedListener.updateCurrentList();
					}
				}

				break;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case TAKE_PHOTO:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					takePhoto();
				}
				break;
			case LOAD_IMAGE:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					selectFromGallery();
				}
				break;

			default:
				break;
		}
	}

	@Override
	public boolean onBackPressed() {
		boolean result = true;

		if (mName.length() != 0) {
			QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_save_item), -1);
			dialogFrag.setTargetFragment(this, IS_SAVE_CHANGES);
			dialogFrag.show(getFragmentManager(), IS_SAVE_CHANGES_DATE);

			result = false;
		}

		return result;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (isAdded()) {
			if (key.equals(getString(R.string.settings_key_use_category))) {
				setCategory();
			} else if (key.equals(getString(R.string.settings_key_fast_edit))) {
				updateSpinners();
			}
		}
	}

	public void setIsBought(boolean isBought) {
		mIsBought.setChecked(isBought);
	}

	public void updateSpinners() {
		mIsUseNewItemInSpinner = mPrefs.getBoolean(getString(R.string.settings_key_fast_edit), true);

		setUnit(-1);
		setCategory(-1);
	}

	public void setCategory() {
		mIsUseCategory = mPrefs.getBoolean(getString(R.string.settings_key_use_category), true);
		mCategoryContainer.setVisibility(mIsUseCategory ? View.VISIBLE : View.GONE);
	}

	public void setCurrency() {
		getCurrencyFromDb();
		mCurrency.setText(mCurrencyList);

		if (mFinishPrice.getVisibility() == View.VISIBLE) {
			setFinishPrice();
		}
	}

	public void setUnit(long idUnit) {
		mUnit.setAdapter(getUnitsAdapter());
		setSelectionUnit(idUnit);
	}

	public void setCategory(long idCategory) {
		mCategory.setAdapter(getCategoriesAdapter());
		setSelectionCategory(idCategory);
	}

	protected void setupView(View v, final Bundle savedInstanceState) {
		if (!mIsCollapsedMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
				if (hasFocus){
					onElementFocused(mAmount, R.string.settings_key_text_selection_amount);
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
							openAddItemDialog(UNIT_ADD, UNIT_ADD_DATE);
						} else {
							mIdSelectedUnit = ((UnitsDS.UnitCursor) mUnit.getSelectedItem()).getEntity().getId();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}
				});
			}
		});

		mPriceInputLayout = (TextInputLayout) v.findViewById(R.id.price_input_layout);
		mPrice = (EditText) v.findViewById(R.id.new_item_price);
		mPrice.addTextChangedListener(getPriceChangedListener());
		mPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					onElementFocused(mPrice, R.string.settings_key_text_selection_price);
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
							openAddItemDialog(CATEGORY_ADD, CATEGORY_ADD_DATE);
						} else {
							mIdSelectedCategory = ((CategoriesDS.CategoryCursor) mCategory.getSelectedItem()).getEntity().getId();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}
				});
			}
		});
		mCategoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
		setCategory();

		mComment = (EditText) v.findViewById(R.id.comment);
		mComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					onElementFocused(mComment, R.string.settings_key_text_selection_comment);
				}
			}
		});
		if (mPrefs.getBoolean(getString(R.string.settings_key_text_selection_comment), false)) {
			mComment.setSelectAllOnFocus(true);
		}

		mIsBought = (ToggleButton) v.findViewById(R.id.is_bought);
	}

	protected void loadImage(String path) {
		if (isDeleteImage(path)) {
			Image.deleteFile(mItemInList.getItem().getImagePath());
		}

		mItemInList.getItem().setImagePath(path);

		Image.create().insertImageToView(getActivity(), path, mAppBarImage);
		mCollapsingToolbarLayout.setTitle("");
	}

	protected void setSelectionUnit(long idUnit) {
		if (idUnit != -1) {
			mIdSelectedUnit = idUnit;
		}

		mUnit.setSelection(getPosition(mUnit, mIdSelectedUnit));
	}

	protected void setSelectionCategory(long idCategory) {
		if (idCategory != -1) {
			mIdSelectedCategory = idCategory;
		}

		mCategory.setSelection(getPosition(mCategory, mIdSelectedCategory));
	}

	protected SimpleCursorAdapter getCompleteTextAdapter(FilterQueryProvider provider) {
		SimpleCursorAdapter completeTextAdapter = new ColorAdapter(getActivity(),
				R.layout.spinner_autocomplite, null);
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

	protected ShoppingList updatedItem() {
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
			if (((EntityCursor<Catalog>) spinner.getItemAtPosition(i)).getEntity().getId() == id) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			index = mIsUseNewItemInSpinner ? 1 : 0;
		}
		return index;
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
						mFinishPrice.setVisibility(View.GONE);
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
						mFinishPrice.setVisibility(View.GONE);
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
				new String[]{UnitsInterface.COLUMN_NAME},
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
		try {
			double amount = Double.parseDouble(mAmount.getText().toString().replace(',', '.'));
			double price = Double.parseDouble(mPrice.getText().toString().replace(',', '.'));
			String finalPriceText = getString(R.string.finish_price) + localValue(amount * price) + " " + mCurrencyList;
			mFinishPrice.setText(finalPriceText);
			mFinishPrice.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			mFinishPrice.setVisibility(View.GONE);
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

	private boolean hasPermission(String permission){
		return ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
	}

	private void takePhoto(){
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mFile = Image.create().createImageFile(getActivity());

		if (mFile != null) {
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
			startActivityForResult(cameraIntent, TAKE_PHOTO);
		} else {
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
		}
	}

	private void selectFromGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(galleryIntent, LOAD_IMAGE);
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

	private void openAddItemDialog (int requestCode, String tag) {
		GeneralDialogFragment addItemDialog;

		switch (requestCode) {
			case UNIT_ADD:
				addItemDialog = new UnitDialogFragment();
				break;
			case CATEGORY_ADD:
				addItemDialog = new CategoryDialogFragment();
				break;
			default:
				return;
		}

		addItemDialog.setTargetFragment(ItemFragment.this, requestCode);
		addItemDialog.show(getFragmentManager(), tag);

		FirebaseAnalytic.getInstance(getActivity(), FirebaseAnalytic.ADD_CATALOG_IN_ITEM)
				.putString(FirebaseAnalytic.CONTENT_TYPE, tag)
				.addEvent();
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

			if (cursor.getColumnIndex(ItemsInterface.COLUMN_NAME) != -1) {
				name = cursor.getString(cursor.getColumnIndex(ItemsInterface.COLUMN_NAME));
			} else if (cursor.getColumnIndex(CategoriesInterface.COLUMN_NAME) != -1) {
				name = cursor.getString(cursor.getColumnIndex(CategoriesInterface.COLUMN_NAME));
			}

			holder.mName.setText(name);

			if (mIsUseCategory) {
				holder.mColor.setBackgroundColor(cursor.getInt(cursor.getColumnIndex(CategoriesInterface.COLUMN_COLOR)));
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
