package ru.android.ainege.shoppinglist.ui.fragments.item;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS.CurrencyCursor;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.dataSources.UnitsDS;
import ru.android.ainege.shoppinglist.db.entities.Dictionary;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.db.tables.CategoriesTable;
import ru.android.ainege.shoppinglist.db.tables.ItemsTable;
import ru.android.ainege.shoppinglist.db.tables.UnitsTable;
import ru.android.ainege.shoppinglist.ui.ImageFragmentInterface;
import ru.android.ainege.shoppinglist.ui.activities.ItemActivity;
import ru.android.ainege.shoppinglist.ui.fragments.QuestionDialogFragment;
import ru.android.ainege.shoppinglist.util.AndroidBug5497Workaround;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.Showcase;
import ru.android.ainege.shoppinglist.util.Validation;

import static ru.android.ainege.shoppinglist.db.dataSources.GenericDS.EntityCursor;

public abstract class ItemFragment extends Fragment implements ImageFragmentInterface, ItemActivity.OnBackPressedInterface, View.OnClickListener {
	private static final String ID_ITEM = "idItem";
	private static final int TAKE_PHOTO = 0;
	private static final int LOAD_IMAGE = 1;
	private static final int IS_SAVE_CHANGES = 2;
	private static final String IS_SAVE_CHANGES_DATE = "answerDialog";

	private CoordinatorLayout mCoordinatorLayout;
	private AppBarLayout mAppBarLayout;
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
	protected ToggleButton mIsBought;

	protected ItemDS mItemDS;
	protected ShoppingListDS mItemsInListDS;
	protected ShoppingList mItemInList;
	protected boolean mIsImageLoaded = true;
	protected boolean mIsProposedItem = false;

	private String mCurrencyList;
	private File mFile;
	private String mPhotoPath;
	private TextView mFinishPrice;

	private boolean mIsOpenedKeyboard = false;
	private boolean mIsExpandedAppbar;

	private boolean mIsUseCategory;
	private ShowcaseView showcaseView;
	private int counter = 1;

	protected abstract TextWatcher getNameChangedListener();

	protected abstract SimpleCursorAdapter getCompleteTextAdapter();

	protected abstract boolean saveData();

	protected abstract long getIdList();

	protected abstract void updatedItem();

	protected abstract void resetImage();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidBug5497Workaround.assistActivity(getActivity());
		setHasOptionsMenu(true);

		mItemDS = new ItemDS(getActivity());
		mItemsInListDS = new ShoppingListDS(getActivity());

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mIsUseCategory = prefs.getBoolean(getString(R.string.settings_key_use_category), true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_item, container, false);

		setCurrency();

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
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

		setupView(v);
		showCaseViews();

		return v;
	}

	private void showCaseViews() {
		showcaseView = new ShowcaseView.Builder(getActivity())
				.setTarget(new ViewTarget(mAppBarImage))
				.setContentTitle(getString(R.string.showcase_update_image_item))
				.setContentText(getString(R.string.showcase_update_image_item_desc))
				.setOnClickListener(this)
				.setStyle(R.style.Showcase)
				.singleShot(Showcase.SHOT_ITEM)
				.build();

		showcaseView.forceTextPosition(ShowcaseView.BELOW_SHOWCASE);
		Showcase.newInstance(showcaseView, getActivity()).setButton(getString(R.string.next), false);
	}

	@Override
	public void onClick(View v) {
		switch (counter) {
			case 1:
				showcaseView.setShowcase(new ViewTarget(mIsBought), true);
				showcaseView.setContentTitle(getString(R.string.showcase_bought_item));
				showcaseView.setContentText(getString(R.string.showcase_bought_item_desc));
				showcaseView.setButtonText(getString(R.string.close));
				showcaseView.forceTextPosition(ShowcaseView.ABOVE_SHOWCASE);
				break;
			case 2:
				showcaseView.hide();
				break;
		}
		counter++;
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
				mIsImageLoaded = false;
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				mFile = Image.create().createImageFile();
				if (mFile != null) {
					mPhotoPath = Image.PATH_PROTOCOL + mFile.getAbsolutePath();

					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
					startActivityForResult(cameraIntent, TAKE_PHOTO);
				} else {
					Toast.makeText(getActivity(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.select_from_gallery:
				mIsImageLoaded = false;
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
			mIsImageLoaded = true;

			if (requestCode == IS_SAVE_CHANGES) {
				getActivity().finish();
			}

			return;
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		switch (requestCode) {
			case TAKE_PHOTO:
				mItemInList.getItem().setImagePath(mPhotoPath);
				deletePhotoFromGallery();
				new Image.BitmapWorkerTask(mFile, metrics.widthPixels - 30, this).execute();
				break;
			case LOAD_IMAGE:
				Uri selectedImage = data.getData();

				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

					Image image = Image.create();
					File file = image.createImageFile();

					if (file != null) {
						mPhotoPath = Image.PATH_PROTOCOL + file.getAbsolutePath();
						new Image.BitmapWorkerTask(file, bitmap, metrics.widthPixels - 30, this).execute();

						mItemInList.getItem().setImagePath(mPhotoPath);
					} else {
						Toast.makeText(getActivity(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case IS_SAVE_CHANGES:
				saveItem();
				break;
		}
	}

	@Override
	public void updateImage() {
		mIsImageLoaded = true;
		loadImage();
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

	protected void setupView(View v) {
		mCoordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.coordinatorLayout);
		mCoordinatorLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int heightDiff = mCoordinatorLayout.getRootView().getHeight() - mCoordinatorLayout.getHeight();
				if (heightDiff > 100 && !mIsOpenedKeyboard) { // 99% of the time the height diff will be due to a keyboard.
					mIsOpenedKeyboard = true;

					if (mIsExpandedAppbar) {
						mAppBarLayout.setExpanded(false);
					}
				} else if(heightDiff <= 100 && mIsOpenedKeyboard){
					mIsOpenedKeyboard = false;

					if (mIsExpandedAppbar) {
						mAppBarLayout.setExpanded(true);
					}
				}
			}
		});

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

		mFinishPrice = (TextView) v.findViewById(R.id.finish_price);

		mAmountInputLayout = (TextInputLayout) v.findViewById(R.id.amount_input_layout);
		mAmount = (EditText) v.findViewById(R.id.new_amount_item);
		mAmount.addTextChangedListener(getAmountChangedListener());

		mUnit = (Spinner) v.findViewById(R.id.amount_unit);
		mUnit.setAdapter(getUnitsAdapter());

		mPriceInputLayout = (TextInputLayout) v.findViewById(R.id.price_input_layout);
		mPrice = (EditText) v.findViewById(R.id.new_item_price);
		mPrice.addTextChangedListener(getPriceChangedListener());

		TextView mCurrency = (TextView) v.findViewById(R.id.currency);
		mCurrency.setText(mCurrencyList);

		mCategory = (Spinner) v.findViewById(R.id.category);
		mCategory.setAdapter(getCategoriesAdapter());
		if (!mIsUseCategory) {
			LinearLayout categoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
			categoryContainer.setVisibility(View.GONE);
		}

		mComment = (EditText) v.findViewById(R.id.comment);

		mIsBought = (ToggleButton) v.findViewById(R.id.is_bought);
	}

	protected void loadImage() {
		Image.create().insertImageToView(getActivity(), mItemInList.getItem().getImagePath(), mAppBarImage);
	}

	protected SimpleCursorAdapter getCompleteTextAdapter(FilterQueryProvider provider) {
		SimpleCursorAdapter completeTextAdapter = new ColorAdapter(getActivity(),
				R.layout.spinner_autocomplite, null);
		completeTextAdapter.setFilterQueryProvider(provider);
		completeTextAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cursor) {
				return cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_NAME));
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
		int index = 0;
		for (int i = 0; i < spinner.getCount(); i++) {
			if (((EntityCursor<Dictionary>) spinner.getItemAtPosition(i)).getEntity().getId() == id) {
				index = i;
				break;
			}
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
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_spinner_item,
				new UnitsDS(getActivity()).getAll(),
				new String[]{UnitsTable.COLUMN_NAME},
				new int[]{android.R.id.text1}, 0);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return spinnerAdapter;
	}

	private SimpleCursorAdapter getCategoriesAdapter() {
		SimpleCursorAdapter spinnerAdapter = new ColorAdapter(getActivity(),
				R.layout.spinner_color_item,
				new CategoriesDS(getActivity()).getAll());
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
			getActivity().finish();
		} else {
			Toast.makeText(getActivity(), R.string.info_wrong_value, Toast.LENGTH_LONG).show();
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

			if (cursor.getColumnIndex(ItemsTable.COLUMN_NAME) != -1) {
				name = cursor.getString(cursor.getColumnIndex(ItemsTable.COLUMN_NAME));
			} else if (cursor.getColumnIndex(CategoriesTable.COLUMN_NAME) != -1) {
				name = cursor.getString(cursor.getColumnIndex(CategoriesTable.COLUMN_NAME));
			}

			holder.mName.setText(name);

			if (mIsUseCategory) {
				holder.mColor.setBackgroundColor(cursor.getInt(cursor.getColumnIndex(CategoriesTable.COLUMN_COLOR)));
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
