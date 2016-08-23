package ru.android.ainege.shoppinglist.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.adapter.ShoppingListAdapter;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.OnDialogShownListener;
import ru.android.ainege.shoppinglist.ui.RecyclerItemClickListener;
import ru.android.ainege.shoppinglist.ui.activities.CatalogsActivity;
import ru.android.ainege.shoppinglist.ui.activities.SingleFragmentActivity;
import ru.android.ainege.shoppinglist.ui.fragments.item.ItemFragment;
import ru.android.ainege.shoppinglist.ui.fragments.list.EditListDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.list.ListDialogFragment;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS.CategoryCursor;
import static ru.android.ainege.shoppinglist.db.dataSources.ListsDS.ListCursor;

public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, ShoppingListAdapter.ShowcaseListener {
	private static final String APP_PREFERENCES = "shopping_list_settings";
	private static final String APP_PREFERENCES_ID = "idList";
	private static final String ID_LIST = "idList";
	private static final String STATE_COLLAPSE = "state_collapse";
	private static final String STATE_ITEMS = "state_items";
	private static final String STATE_SPENT_SUM = "state_spent_sum";
	private static final String STATE_TOTAL_SUM = "state_total_sum";
	private static final String STATE_ACTION_MODE = "state_action_mode";
	private static final String STATE_ACTION_DATA = "state_action_data";

	public static final int ADD_ITEM = 0;
	public static final int EDIT_ITEM = 1;
	public static final int EDIT_LIST = 3;
	private static final int IS_DELETE_LIST = 4;
	private static final String EDIT_ITEM_DATE = "editListDialog";
	private static final String IS_DELETE_LIST_DATE = "answerListDialog";
	private static final int DATA_LOADER = 0;

	private ListsDS mListsDS;
	private List mList;

	private OnCreateViewListener mOnCreateViewListener;
	private OnClickListener mOnClickListener;
	private OnListChangedListener mOnListChangedListener;
	private OnItemChangedListener mOnItemChangedListener;
	private OnDialogShownListener mOnDialogShownListener;

	private java.util.List<Object> mSaveListRotate;
	private boolean mIsStartActionMode;
	private double mSaveSpentMoney = 0;
	private double mSaveTotalMoney = 0;
	private long mItemDetailsId = -1;

	private CollapsingToolbarLayout mToolbarLayout;
	private ImageView mListImage;
	private FloatingActionButton mFAB;
	private RecyclerView mItemsListRV;
	private TextView mSpentMoney, mTotalMoney;
	private LinearLayout mMoneyContainer;
	private ImageButton mSLMenu;
	private ImageView mEmptyImage;
	private FrameLayout mFrameLayout;
	private LinearLayout mListContainer;
	private ProgressBar mProgressBar;
	private ShoppingListAdapter mAdapterRV;

	private SharedPreferences mSharedPref;
	private boolean mIsUpdateData = false;
	private boolean mIsBoughtEndInList;
	private boolean mIsUseCategory;
	private boolean mIsCollapsedCategory = false;
	private boolean mIsLandscapeTablet;

	private android.view.ActionMode mActionMode;
	private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cab_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			if (mActionMode != null) {
				return false;
			}

			if (mOnDialogShownListener != null) {
				mOnDialogShownListener.onOpenDialog(mList.getId());
			}

			mFAB.setVisibility(View.GONE);

			if (!mIsStartActionMode) {
				mAdapterRV.extendAllCategory(false);
			}

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.delete:
					deleteSelectedItems();
					return true;
				case R.id.select_bought:
					mAdapterRV.selectAllItems(true);
					return true;
				case R.id.select_not_bought:
					mAdapterRV.selectAllItems(false);
					return true;
				default:
					return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mFAB.setVisibility(View.VISIBLE);
			mActionMode = null;
			mIsStartActionMode = false;

			mAdapterRV.recoveryCollapseAllCategory();
			mAdapterRV.clearSelections();

			if (mOnDialogShownListener != null) {
				mOnDialogShownListener.onCloseDialog();
			}
		}
	};

	public static ShoppingListFragment newInstance(long id) {
		Bundle args = new Bundle();
		args.putLong(ID_LIST, id);

		ShoppingListFragment fragment = new ShoppingListFragment();
		fragment.setArguments(args);

		return fragment;
	}

	public interface OnClickListener {
		void onItemAdd(long id);
		void onItemSelect(ShoppingList item);
	}

	public interface OnListChangedListener {
		void onListUpdated();
		void onListDeleted(long idDeletedList);
	}

	public interface OnItemChangedListener {
		void onItemSetBought(ShoppingList item);
		void onItemDelete();
		void updateCatalogs(String catalogKey);
		long getLastSelectedItemId();
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

		mIsLandscapeTablet = getResources().getBoolean(R.bool.isTablet) && getResources().getBoolean(R.bool.isLandscape);
		mListsDS = new ListsDS(getActivity());
		setList();
		saveId(mList.getId());

		readSettings();
		mAdapterRV = new ShoppingListAdapter(getActivity(), this);

		if (savedInstanceState != null) {
			mAdapterRV.setCollapseCategoryStates((HashMap<Long, Boolean>) savedInstanceState.getSerializable(STATE_COLLAPSE));
			mSaveListRotate = (java.util.List<Object>) savedInstanceState.getSerializable(STATE_ITEMS);
			mSaveSpentMoney = savedInstanceState.getDouble(STATE_SPENT_SUM);
			mSaveTotalMoney = savedInstanceState.getDouble(STATE_TOTAL_SUM);

			if (savedInstanceState.getBoolean(STATE_ACTION_MODE)) {
				mIsStartActionMode = true;
				mAdapterRV.setSelectedItems((ArrayList<ShoppingList>) savedInstanceState.getSerializable(STATE_ACTION_DATA));
			}
		}

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

		mToolbarLayout = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.items_in_list_menu);
		toolbar.setOnMenuItemClickListener(onMenuItemClickListener());

		mListImage = (ImageView) v.findViewById(R.id.appbar_image);

		mFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnClickListener != null) {
					mOnClickListener.onItemAdd(mList.getId());
				}
			}
		});

		mFrameLayout = (FrameLayout) v.findViewById(R.id.main_content);
		mListContainer = (LinearLayout) v.findViewById(R.id.list_container);
		mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
		mEmptyImage = (ImageView) v.findViewById(R.id.empty_list);

		mMoneyContainer = (LinearLayout) v.findViewById(R.id.money_container);
		mSpentMoney = (TextView) v.findViewById(R.id.spent_money);
		mTotalMoney = (TextView) v.findViewById(R.id.total_money);

		mSLMenu = (ImageButton) v.findViewById(R.id.sl_menu);
		mSLMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPopupMenu(v);
			}
		});
		updateMenu();

		mItemsListRV = (RecyclerView) v.findViewById(R.id.items_list);
		mItemsListRV.setAdapter(mAdapterRV);
		LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		llm.setAutoMeasureEnabled(false);
		mItemsListRV.setLayoutManager(llm);
		mItemsListRV.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mItemsListRV, new RecyclerItemClickListener.OnItemClickListener() {
			@Override
			public void onClick(int position) {
				Object item = mAdapterRV.getListItem(position);

				if (item instanceof ShoppingList) {
					onItemClick((ShoppingList) item);
				} else if (item instanceof Category) {
					onItemClick((Category) item, position);
				}
			}

			@Override
			public void onLongClick(int position) {
				Object item = mAdapterRV.getListItem(position);

				if (item instanceof ShoppingList) {
					onItemLongClick((ShoppingList) item);
				} else if (item instanceof Category) {
					onItemLongClick((Category) item);
				}
			}

			@Override
			public void onSwipeRight(int position) {
				Object item = mAdapterRV.getListItem(position);

				if (item instanceof ShoppingList) {
					onItemSwipeRight((ShoppingList) item, position);
				}
			}
		}));

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			((AppBarLayout) v.findViewById(R.id.appbar)).setExpanded(false);
		}

		if (new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_LIST).hasFired()) {
			showCaseView();
		}

		setListData();
		mOnCreateViewListener.onCreateViewListener(this, toolbar);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mIsUpdateData) {
			updateData();
			mIsUpdateData = false;
		} else if (mIsStartActionMode) {
			mActionMode = getActivity().startActionMode(mActionModeCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mOnCreateViewListener = null;
		mOnClickListener = null;
		mOnListChangedListener = null;
		mOnItemChangedListener = null;
		mOnDialogShownListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_COLLAPSE, mAdapterRV.getCollapseCategoryStates());
		outState.putSerializable(STATE_ITEMS, (Serializable) mAdapterRV.getItemList());
		outState.putDouble(STATE_SPENT_SUM, mSaveSpentMoney);
		outState.putDouble(STATE_TOTAL_SUM, mSaveTotalMoney);
		outState.putBoolean(STATE_ACTION_MODE, mActionMode != null);
		outState.putSerializable(STATE_ACTION_DATA, mAdapterRV.getSelectedItems());
	}

	private Toolbar.OnMenuItemClickListener onMenuItemClickListener () {
		return new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.delete_list:
						QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_delete_list) + " \"" + mList.getName() + "\"?", -1);
						dialogFrag.setTargetFragment(ShoppingListFragment.this, IS_DELETE_LIST);
						dialogFrag.show(getFragmentManager(), IS_DELETE_LIST_DATE);

						if (mOnDialogShownListener != null) {
							mOnDialogShownListener.onOpenDialog(mList.getId());
						}

						addAnalytics(FirebaseAnalytic.DELETE_LIST_IN_SL, null);
						return true;
					case R.id.update_list:
						ListDialogFragment editListDialog = EditListDialogFragment.newInstance(mList);
						editListDialog.setTargetFragment(ShoppingListFragment.this, EDIT_LIST);
						editListDialog.show(getFragmentManager(), EDIT_ITEM_DATE);

						if (mOnDialogShownListener != null) {
							mOnDialogShownListener.onOpenDialog(mList.getId());
						}

						addAnalytics(FirebaseAnalytic.UPDATE_LIST_IN_SL, null);

						return true;
					default:
						return false;
				}
			}
		};
	}

	private void showPopupMenu(View v) {
		PopupMenu popupMenu = new PopupMenu(getActivity(), v);
		popupMenu.inflate(R.menu.shopping_list_menu);

		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.collapse_all:
						mAdapterRV.collapseAllCategory(true);

						addAnalytics(FirebaseAnalytic.COLLAPSE_CATEGORY, FirebaseAnalytic.COLLAPSE);

						return true;
					case R.id.expanded_all:
						mAdapterRV.extendAllCategory(true);

						addAnalytics(FirebaseAnalytic.COLLAPSE_CATEGORY, FirebaseAnalytic.EXTEND);

						return true;
					default:
						return false;
				}
			}
		});

		popupMenu.show();
	}

	private void updateMenu() {
		if (mIsUseCategory && mIsCollapsedCategory) {
			mSLMenu.setVisibility(View.VISIBLE);
		} else {
			mSLMenu.setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mOnDialogShownListener != null && (requestCode == EDIT_LIST || requestCode == IS_DELETE_LIST)) {
			mOnDialogShownListener.onCloseDialog();
		}
		if (resultCode != Activity.RESULT_OK) return;

		switch (requestCode) {
			case ADD_ITEM:
			case EDIT_ITEM:
				mItemDetailsId = data.getLongExtra(ItemFragment.ID_ITEM, -1);
				mIsUpdateData = true;
				break;
			case IS_DELETE_LIST:
				mListsDS.delete(mList.getId());
				Image.deleteFile(mList.getImagePath());
				saveId(-1);

				if (mOnListChangedListener != null) {
					mOnListChangedListener.onListDeleted(mList.getId());
				}

				break;
			case EDIT_LIST:
				updateList();
				if (mOnListChangedListener != null) {
					mOnListChangedListener.onListUpdated();
				}

				if (mOnItemChangedListener != null) {
					mOnItemChangedListener.updateCatalogs(getString(R.string.catalogs_key_currency));
				}

				break;
			case SingleFragmentActivity.CATALOGS:
			case SingleFragmentActivity.SETTINGS:
				HashMap<Integer, Long> modifyCatalog = (HashMap<Integer, Long>) data.getSerializableExtra(CatalogsActivity.LAST_EDIT);

				if (modifyCatalog != null) {
					setList();
					updateData();
				}

				break;
		}
	}

	public void setListeners(OnClickListener onClickListener, OnListChangedListener onListChangedListener,
	                         OnDialogShownListener onDialogShownListener) {
		setOnClickListener(onClickListener);
		setOnListChangedListener(onListChangedListener);
		setOnDialogShownListener(onDialogShownListener);
	}

	public void setListeners(OnClickListener onClickListener, OnListChangedListener onListChangedListener) {
		setListeners(onClickListener, onListChangedListener, null);
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		mOnClickListener = onClickListener;
	}

	public void setOnListChangedListener(OnListChangedListener onListChangedListener) {
		mOnListChangedListener = onListChangedListener;
	}

	public void setOnItemChangedListener(OnItemChangedListener onItemChangedListener) {
		mOnItemChangedListener = onItemChangedListener;
	}

	public void setOnDialogShownListener(OnDialogShownListener onDialogShownListener) {
		mOnDialogShownListener = onDialogShownListener;
	}

	public void updateList() {
		setList();
		setListData();
		updateSums(mSaveSpentMoney, mSaveTotalMoney);
		mAdapterRV.setCurrency(mList.getCurrency().getSymbol(), true);
	}

	public void closeActionMode(){
		if (mActionMode != null) {
			mActionMode.finish();
		}
	}

	public void setItemDetailsId(long id) {
		mItemDetailsId = id;
	}

	//<editor-fold desc="Work with loader">
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> loader = null;
		switch (id) {
			case DATA_LOADER:
				loader = new ItemsInListCursorLoader(getActivity(), mList.getId());
				break;
			default:
				break;
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
			case DATA_LOADER:
				if (mProgressBar.getVisibility() == View.VISIBLE) {
					mProgressBar.setVisibility(View.GONE);
				}

				if (mSaveListRotate != null && mSaveListRotate.size() > 0) {
					mAdapterRV.setData(mSaveListRotate, mList.getCurrency().getSymbol(), mIsUseCategory);

					updateSums(mSaveSpentMoney, mSaveTotalMoney);
					hideEmptyStates();
				} else if (mSaveListRotate == null && data.moveToFirst()) {
					ArrayList<Category> categories = getItemsList((CategoryCursor) data); //create array for adapter
					ShoppingList item  = getDetailsItem(categories);

					if (item != null) {
						mAdapterRV.setCollapseCategoryStates(item.getIdCategory(), false);
					}

					mAdapterRV.setData(categories, mList.getCurrency().getSymbol(), mIsUseCategory, mIsCollapsedCategory);     //update data in adapter

					if (item != null) {
						int itemPosition = mAdapterRV.mItemList.indexOf(item);
						int position = itemPosition != -1 ? itemPosition : mAdapterRV.mItemList.indexOf(item.getCategory());
						mItemsListRV.scrollToPosition(position);
					}

					updateSums();
					hideEmptyStates();
				} else {
					showEmptyStates();
				}

				break;
			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
			case DATA_LOADER:
				break;
			default:
				break;
		}
	}

	public void updateData() {
		mSaveListRotate = null;
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();

		if (mOnListChangedListener != null) {
			mOnListChangedListener.onListUpdated();
		}
	}

	private ArrayList<Category> getItemsList(CategoryCursor data) {
		ArrayList<Category> categories = new ArrayList<>();

		if (mIsUseCategory) {
			categories = data.getEntities();
		} else {
			ArrayList<ShoppingList> itemsInList = data.getItemsInListCursor().getEntities();
			ShoppingList.sort(itemsInList);

			categories.add(new Category(itemsInList));

			for (ShoppingList item : itemsInList) {
				item.getCategory().setItemsByCategoryInList(itemsInList);
			}
		}

		return categories;
	}

	private ShoppingList getDetailsItem(ArrayList<Category> categories) {
		if (mItemDetailsId != -1) {
			for (Category c : categories) {
				for (ShoppingList item : c.getItemsByCategoryInList()) {
					if (item.getIdItem() == mItemDetailsId) {
						mItemDetailsId = -1;
						return item;
					}
				}
			}

			mItemDetailsId = -1;
		}

		return null;
	}
	//</editor-fold>

	//<editor-fold desc="Work with preferences">
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (isAdded()) {
			if (key.equals(getString(R.string.settings_key_sort_type))) {
				mIsUpdateData = true;
				readSortTypeSetting();
			} else if (key.equals(getString(R.string.settings_key_sort_is_bought))) {
				mIsUpdateData = true;
				readIsBoughtSetting();
			} else if (key.equals(getString(R.string.settings_key_use_category))) {
				mIsUpdateData = true;
				readCategorySetting();
				updateMenu();
			} else if (key.equals(getString(R.string.settings_key_collapse_category))) {
				mIsUpdateData = true;
				readCollapseCategorySetting();
				updateMenu();
			}
		}
	}

	private void readSettings() {
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mSharedPref.registerOnSharedPreferenceChangeListener(this);

		readIsBoughtSetting();
		readCategorySetting();
		readCollapseCategorySetting();
		readSortTypeSetting();
	}

	private void readIsBoughtSetting() {
		mIsBoughtEndInList = mSharedPref.getBoolean(getString(R.string.settings_key_sort_is_bought), true);
		ShoppingList.setSortSettings(mIsBoughtEndInList);
	}

	private void readCategorySetting() {
		mIsUseCategory = mSharedPref.getBoolean(getString(R.string.settings_key_use_category), true);
	}

	private void readCollapseCategorySetting() {
		mIsCollapsedCategory = mSharedPref.getBoolean(getString(R.string.settings_key_collapse_category), true);
	}

	private void readSortTypeSetting() {
		String sortType;
		String regular = mSharedPref.getString(getString(R.string.settings_key_sort_type), "");

		if (regular.contains(getString(R.string.sort_order_alphabet))) {
			sortType = ShoppingList.ALPHABET;
		} else if (regular.contains(getString(R.string.sort_order_adding))) {
			sortType = ShoppingList.ORDER_ADDING;
		} else if (regular.contains(getString(R.string.sort_order_up_price))) {
			sortType = ShoppingList.UP_PRICE;
		} else if (regular.contains(getString(R.string.sort_order_down_price))) {
			sortType = ShoppingList.DOWN_PRICE;
		} else if (regular.contains(getString(R.string.sort_order_up_purchase_price))) {
			sortType = ShoppingList.UP_PURCHASE_PRICE;
		} else if (regular.contains(getString(R.string.sort_order_down_purchase_price))) {
			sortType = ShoppingList.DOWN_PURCHASE_PRICE;
		} else {
			sortType = ShoppingList.ALPHABET;
		}

		ShoppingList.setSortSettings(sortType);
	}
	//</editor-fold>

	//<editor-fold desc="Customize the list">
	public void setList() {
		ListCursor cursor = mListsDS.get(getArguments().getLong(ID_LIST));

		if (cursor.moveToFirst()) {
			mList = cursor.getEntity();
		}

		cursor.close();
	}

	private void setListData() {
		setTitle();
		loadImage();
	}

	private void setTitle() {
		mToolbarLayout.setTitle(mList.getName());
	}

	private void loadImage() {
		Image.create().insertImageToView(getActivity(),
				mList.getImagePath(),
				mListImage);
	}
	//</editor-fold>

	private void saveId(long id) {
		SharedPreferences mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mSettings.edit();

		if (id != -1) {
			editor.putLong(APP_PREFERENCES_ID, id);
		} else {
			editor.remove(APP_PREFERENCES_ID);

			if (mOnCreateViewListener != null) {
				mOnCreateViewListener.onDeleteSavedList();
			}
		}

		editor.apply();
	}

	private void showEmptyStates() {
		mListContainer.setVisibility(View.GONE);
		mEmptyImage.setVisibility(View.VISIBLE);
		mFrameLayout.setBackgroundResource(R.color.image_background);
	}

	private void hideEmptyStates() {
		mListContainer.setVisibility(View.VISIBLE);
		mEmptyImage.setVisibility(View.GONE);
		mFrameLayout.setBackgroundResource(android.R.color.white);
	}

	private void onItemClick(ShoppingList itemInList) {
		if (mActionMode != null) {
			mAdapterRV.selectItem(itemInList);
			return;
		}

		if (mOnClickListener != null) {
			mOnClickListener.onItemSelect(itemInList);
		}
	}

	private void onItemClick(Category category, int position) {
		if (mActionMode != null) {
			mAdapterRV.selectAllItemsInCategory(category);
			return;
		}

		if (mIsCollapsedCategory) {
			mAdapterRV.setOnClick(position);
		}
	}

	private void onItemLongClick(ShoppingList itemInList) {
		if (mActionMode != null) {
			mAdapterRV.selectItem(itemInList);
			return;
		}

		mActionMode = getActivity().startActionMode(mActionModeCallback);
		mAdapterRV.selectItem(itemInList);
		mItemsListRV.scrollToPosition(mAdapterRV.mItemList.indexOf(itemInList));
	}

	private void onItemLongClick(Category category) {
		if (mActionMode != null) {
			mAdapterRV.selectAllItemsInCategory(category);
			return;
		}

		mActionMode = getActivity().startActionMode(mActionModeCallback);
		mAdapterRV.selectAllItemsInCategory(category);
		mItemsListRV.scrollToPosition(mAdapterRV.mItemList.indexOf(category));
	}

	private void onItemSwipeRight(ShoppingList itemInList, int position) {
		if (mActionMode != null) {
			return;
		}

		ShoppingListAdapter.ItemViewHolder holder = (ShoppingListAdapter.ItemViewHolder) mItemsListRV.findViewHolderForAdapterPosition(position);
		int categoryPosition = mAdapterRV.mItemList.indexOf(itemInList.getCategory());
		Category category = itemInList.getCategory();
		boolean isBought = !itemInList.isBought();

		//update data in db
		new ShoppingListDS(getActivity()).setIsBought(isBought, itemInList.getIdItem(), mList.getId());

		//update adapter
		itemInList.setBought(isBought);
		holder.markBought(isBought);

		//if set bought items in the end of list - refresh position
		if (mIsBoughtEndInList) {
			int oldPositionInCategory = category.getItemsByCategoryInList().indexOf(itemInList);
			ShoppingList.sort(category.getItemsByCategoryInList());
			int newPositionInCategory = category.getItemsByCategoryInList().indexOf(itemInList);
			int toPosition = position + (newPositionInCategory - oldPositionInCategory);

			mAdapterRV.mItemList.remove(itemInList);
			mAdapterRV.mItemList.add(toPosition, itemInList);

			if (toPosition != -1 && position != toPosition) {
				mAdapterRV.notifyItemMoved(position, toPosition);
				mItemsListRV.scrollToPosition(toPosition);
			}
		}

		//update spent sum
		double sum = itemInList.getSum();
		double categorySpentMoney = category.getSpentSum();
		int boughtItems = category.getBoughtItemsCount();

		if (isBought) {
			mSaveSpentMoney += sum;
			category.setSpentSum(categorySpentMoney + sum);
			category.setBoughtItemsCount(++boughtItems);
		} else {
			mSaveSpentMoney -= sum;
			category.setSpentSum(categorySpentMoney - sum);
			category.setBoughtItemsCount(--boughtItems);
		}

		updateSpentSum(mSaveSpentMoney);
		mAdapterRV.notifyItemChanged(categoryPosition);

		if (mOnListChangedListener != null) {
			mOnListChangedListener.onListUpdated();
		}

		if (mOnItemChangedListener != null) {
			mOnItemChangedListener.onItemSetBought(itemInList);
		}
	}

	private void deleteSelectedItems() {
		boolean isDeleteOpenItem = false;

		if (mOnItemChangedListener != null) {
			isDeleteOpenItem = mAdapterRV.isContainsInSelected(mOnItemChangedListener.getLastSelectedItemId());
		}

		mAdapterRV.removeSelected();

		if (mAdapterRV.getItemCount() > 0) {
			updateSums();
			hideEmptyStates();

			if (isDeleteOpenItem && mOnClickListener != null) {
				ShoppingList item = (ShoppingList) (mIsUseCategory ? mAdapterRV.getListItem(1) : mAdapterRV.getListItem(0));
				mOnClickListener.onItemSelect(item);
			}
		} else {
			showEmptyStates();
			mActionMode.finish();

			if (mOnItemChangedListener != null) {
				mOnItemChangedListener.onItemDelete();
			}
		}

		if (mOnListChangedListener != null) {
			mOnListChangedListener.onListUpdated();
		}
	}

	//<editor-fold desc="Counting the amount of list">
	private void updateSums() {
		updateSums(sumSpentMoney(), sumTotalMoney());
	}

	private void updateSums(double spentMoney, double totalMoney) {
		updateSpentSum(spentMoney);

		String total = localValue(totalMoney) + " " + mList.getCurrency().getSymbol();
		mTotalMoney.setText(total);
	}

	private void updateSpentSum(double newSum) {
		String spentMoney = localValue(newSum) + " " + mList.getCurrency().getSymbol();
		mSpentMoney.setText(spentMoney);
	}

	private String localValue(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(value);
	}

	private double sumTotalMoney() {
		double sum = 0;

		for (Object obj : mAdapterRV.getItemList()) {
			if (mIsUseCategory) {
				if (obj instanceof Category) {
					sum += ((Category) obj).calculateTotalSum();
				}
			} else {
				sum += ((ShoppingList) obj).getCategory().calculateTotalSum();
				break;
			}
		}

		mSaveTotalMoney = sum;
		return sum;
	}

	private double sumSpentMoney() {
		double sum = 0;

		for (Object obj : mAdapterRV.getItemList()) {
			if (mIsUseCategory) {
				if (obj instanceof Category) {
					sum += ((Category) obj).calculateSpentSum();
				}
			} else {
				sum += ((ShoppingList) obj).getCategory().calculateSpentSum();
				break;
			}
		}

		mSaveSpentMoney = sum;
		return sum;
	}
	//</editor-fold>

	//<editor-fold desc="Work with showcases">
	public void showCaseView() {
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), String.valueOf(Showcase.SHOT_ADD_ITEM));
		sequence.setConfig(new ShowcaseConfig());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), mFAB,
				getString(R.string.showcase_create_item)).build());

		sequence.start();

		sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
			@Override
			public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
				if (i == 0) {
					onAttachedAdapter(null);
				}
			}
		});
	}

	@Override
	public void onAttachedAdapter(String idActiveSequence) {
		if (getActivity() == null) {
			return;
		}

		MaterialShowcaseSequence itemSequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_ITEM_IN_LIST);

		if (!Showcase.SHOT_ITEM_IN_LIST.equals(idActiveSequence) && !itemSequence.hasFired()) {
			for (int i = 0; i < mAdapterRV.getItemCount(); i++) {
				RecyclerView.ViewHolder nextHolder = mItemsListRV.findViewHolderForLayoutPosition(i);

				if (nextHolder != null && nextHolder instanceof ShoppingListAdapter.ItemViewHolder) {
					showcaseItem(itemSequence, (ShoppingListAdapter.ItemViewHolder) nextHolder);
					return;
				}
			}
		}

		if (mIsUseCategory) {
			MaterialShowcaseSequence categorySequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_CATEGORY);
			MaterialShowcaseSequence categoryCollapseSequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_CATEGORY_COLLAPSE);

			if (!Showcase.SHOT_CATEGORY.equals(idActiveSequence) && !categorySequence.hasFired()) {
				RecyclerView.ViewHolder holder = mItemsListRV.findViewHolderForLayoutPosition(0);

				if (holder != null && holder instanceof ShoppingListAdapter.CategoryViewHolder) {
					showcaseCategory(categorySequence, (ShoppingListAdapter.CategoryViewHolder) holder);
				}
			} else if (mIsCollapsedCategory && !Showcase.SHOT_CATEGORY_COLLAPSE.equals(idActiveSequence) && !categoryCollapseSequence.hasFired()) {
				RecyclerView.ViewHolder holder = mItemsListRV.findViewHolderForLayoutPosition(0);

				if (holder != null && holder instanceof ShoppingListAdapter.CategoryViewHolder) {
					showcaseCollapseCategory((ShoppingListAdapter.CategoryViewHolder) holder);
				}
			}
		}
	}

	private void showcaseItem(final MaterialShowcaseSequence sequence, final ShoppingListAdapter.ItemViewHolder holder) {
		sequence.setConfig(new ShowcaseConfig());

		showcase(sequence, holder.itemView, getString(R.string.showcase_edit_item));

		String swipe1, swipe2;
		if (holder.mIsBought.getVisibility() == View.GONE) {
			swipe1 = getString(R.string.showcase_swipe_item_1);
			swipe2 = getString(R.string.showcase_unswipe_item_1);
		} else {
			swipe1 = getString(R.string.showcase_swipe_item_2);
			swipe2 = getString(R.string.showcase_unswipe_item_2);
		}

		showcase(sequence, holder.itemView, swipe1);
		showcase(sequence, holder.itemView, swipe2);
		showcase(sequence, holder.itemView, getString(R.string.showcase_delete_item));
		showcase(sequence,  mMoneyContainer, getString(R.string.showcase_spent_sum));

		sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
			@Override
			public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
				switch (i) {
					case 1:
					case 2:
						holder.mIsBought.setVisibility(holder.mIsBought.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
						break;
					case 4:
						onAttachedAdapter(Showcase.SHOT_ITEM_IN_LIST);
						break;
				}
			}
		});

		sequence.start();
	}

	private void showcase(MaterialShowcaseSequence sequence, View item, String text) {
		MaterialShowcaseView.Builder builder = Showcase.createShowcase(getActivity(), item, text);

		if (mIsLandscapeTablet) {
			builder.withRectangleShape().setShapePadding(2);
		} else {
			builder.withRectangleShape(true);
		}

		sequence.addSequenceItem(builder.build());
	}

	private void showcaseCategory(final MaterialShowcaseSequence sequence, ShoppingListAdapter.CategoryViewHolder holder) {
		sequence.setConfig(new ShowcaseConfig());
		showcase(sequence, holder.mCategoryContainer, getString(R.string.showcase_category));
		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), holder.mSumCategory,
				getString(R.string.showcase_sum_category))
				.withRectangleShape()
				.build());

		sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
			@Override
			public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
				if (i == 1) {
					onAttachedAdapter(Showcase.SHOT_CATEGORY);
				}
			}
		});

		sequence.start();
	}

	private void showcaseCollapseCategory(ShoppingListAdapter.CategoryViewHolder holder) {
		MaterialShowcaseView.Builder b = Showcase.createShowcase(getActivity(),
				holder.mCategoryContainer, getString(R.string.showcase_collapse_category));

		if (mIsLandscapeTablet) {
			b.withRectangleShape().setShapePadding(2);
		} else {
			b.withRectangleShape(true).build();
		}

		b.singleUse(Showcase.SHOT_CATEGORY_COLLAPSE).show();
	}
	//</editor-fold>

	private void addAnalytics(String name, String value) {
		FirebaseAnalytic.getInstance(getActivity(), name)
				.putString(FirebaseAnalytic.CONTENT_TYPE, value)
				.addEvent();
	}

	private static class ItemsInListCursorLoader extends CursorLoader {
		private final Context mContext;
		private final long mIdList;

		public ItemsInListCursorLoader(Context context, long idList) {
			super(context);
			mContext = context;
			mIdList = idList;
		}

		@Override
		public Cursor loadInBackground() {
			CategoryCursor categoryCursor = new CategoriesDS(mContext).getCategoriesInList(mIdList);

			if (categoryCursor.moveToFirst()) {
				ShoppingListCursor itemsCursor = new ShoppingListDS(mContext).getItemsInList(mIdList);
				categoryCursor.setItemsInListCursor(itemsCursor);
			}

			return categoryCursor;
		}
	}
}
