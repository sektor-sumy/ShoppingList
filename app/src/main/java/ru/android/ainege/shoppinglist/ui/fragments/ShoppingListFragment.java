package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.adapter.ShoppingListAdapter;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.activities.ItemActivity;
import ru.android.ainege.shoppinglist.ui.activities.SettingsActivity;
import ru.android.ainege.shoppinglist.util.Image;

import static ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS.CategoryCursor;
import static ru.android.ainege.shoppinglist.db.dataSources.ListsDS.ListCursor;


public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String APP_PREFERENCES = "shopping_list_settings";
	private static final String APP_PREFERENCES_ID = "idList";
	private static final String ID_LIST = "idList";

	private static final int ADD_ITEM = 0;
	private static final int EDIT_ITEM = 1;
	private static final int EDIT_LIST = 3;
	private static final int IS_DELETE_LIST = 4;
	private static final int SETTINGS = 5;
	private static final String EDIT_ITEM_DATE = "editListDialog";
	private static final String IS_DELETE_LIST_DATE = "answerListDialog";
	private static final int DATA_LOADER = 0;

	private ListsDS mListsDS;
	private List mList;

	private ArrayList<Category> mCategories = new ArrayList<>();
	//private HashMap<Long, Boolean> collapseCategoryStates = new HashMap<>();
	private double mSaveSpentMoney = 0;
	private double mSaveTotalMoney = 0;

	private CollapsingToolbarLayout mToolbarLayout;
	private ImageView mListImage;
	private FloatingActionButton mFAB;
	private RecyclerView mItemsListRV;
	private TextView mSpentMoney, mTotalMoney;
	private ImageView mEmptyImage;
	private LinearLayout mListContainer;
	private ProgressBar mProgressBar;
	private ShoppingListAdapter mAdapterRV;

	private SharedPreferences mSharedPref;
	private boolean mIsUpdateData = false;
	private boolean mIsBoughtEndInList;
	private boolean mIsUseCategory;
	private boolean mIsCollapsedCategory = false;

	//<editor-fold desc="Удалениие - ActionMode -">
	/*private android.view.ActionMode mActionMode;
	private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cab_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mFAB.setVisibility(View.GONE);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.delete:
					deleteSelectedItems();
					return true;
				case R.id.select_bought:
					mAdapterRV.selectItems(true);
					return true;
				case R.id.select_not_bought:
					mAdapterRV.selectItems(false);
					return true;
				default:
					return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mAdapterRV.clearSelections();
			mFAB.setVisibility(View.VISIBLE);
			mActionMode = null;
		}
	};*/
	//</editor-fold>
	//<editor-fold desc="Обучение -">
	/*private ShowcaseView showcaseView;
	private int counter = 1;
	private View mView;*/
	//</editor-fold>

	public static ShoppingListFragment newInstance(long id) {
		Bundle args = new Bundle();
		args.putLong(ID_LIST, id);

		ShoppingListFragment fragment = new ShoppingListFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		} else {
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}

		mListsDS = new ListsDS(getActivity());
		getList(getArguments().getLong(ID_LIST));
		saveId(mList.getId());

		readSettings();

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_shopping_list, container, false);

		mToolbarLayout = (CollapsingToolbarLayout) v.findViewById(R.id.collapsing_toolbar);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		mListImage = (ImageView) v.findViewById(R.id.appbar_image);

		mFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ItemActivity.class);
				i.putExtra(ItemActivity.EXTRA_ID_LIST, mList.getId());

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					startActivityForResult(i, ADD_ITEM, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
				} else {
					startActivityForResult(i, ADD_ITEM);
				}
			}
		});

		mListContainer = (LinearLayout) v.findViewById(R.id.list_container);
		mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
		mEmptyImage = (ImageView) v.findViewById(R.id.empty_list);

		mSpentMoney = (TextView) v.findViewById(R.id.spent_money);
		mTotalMoney = (TextView) v.findViewById(R.id.total_money);

		mItemsListRV = (RecyclerView) v.findViewById(R.id.items_list);
		mItemsListRV.setLayoutManager(new LinearLayoutManager(getActivity()));
		mAdapterRV = new ShoppingListAdapter(getActivity());
		mItemsListRV.setAdapter(mAdapterRV);

		//showCaseView();
		setListData();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mIsUpdateData) {
			updateData();
			mIsUpdateData = false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.items_in_list_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.delete_list:
				QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_delete_list));
				dialogFrag.setTargetFragment(ShoppingListFragment.this, IS_DELETE_LIST);
				dialogFrag.show(getFragmentManager(), IS_DELETE_LIST_DATE);
				return true;
			case R.id.update_list:
				ListDialogFragment editListDialog = ListDialogFragment.newInstance(mList);
				editListDialog.setTargetFragment(ShoppingListFragment.this, EDIT_LIST);
				editListDialog.show(getFragmentManager(), EDIT_ITEM_DATE);
				return true;
			case R.id.settings:
				Intent i = new Intent(getActivity(), SettingsActivity.class);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					startActivityForResult(i, SETTINGS, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
				} else {
					startActivityForResult(i, SETTINGS);
				}

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) return;

		switch (requestCode) {
			case ADD_ITEM:
			case EDIT_ITEM:
			case SETTINGS:
				mIsUpdateData = true;
				getList(getArguments().getLong(ID_LIST));
				updateSums(mSaveSpentMoney, mSaveTotalMoney);
				break;
			case IS_DELETE_LIST:
				mListsDS.delete(mList.getId());
				Image.deleteFile(mList.getImagePath());

				saveId(-1);
				getActivity().onBackPressed();
				break;
			case EDIT_LIST:
				getList(getArguments().getLong(ID_LIST));
				setListData();
				updateSums(mSaveSpentMoney, mSaveTotalMoney);
				//mAdapterRV.setCurrency(mList.getCurrency().getSymbol());
				break;
		}
	}

	//<editor-fold desc="Загрузчик">
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

				if (data.moveToFirst()) {
					if (mIsUseCategory) {
						mCategories = ((CategoryCursor) data).getEntities();
					} else {
						ArrayList<ShoppingList> itemsInList = ((CategoryCursor) data).getItemsInListCursor().getEntities();
						ShoppingList.sort(itemsInList);

						ArrayList<Category> categories = new ArrayList<>();
						categories.add(new Category(itemsInList));
						mCategories = categories;
					}

					mAdapterRV.setData(mCategories, mList.getCurrency().getSymbol(), mIsUseCategory);     //update data in adapter

					updateSums();
					hideEmptyStates();
				} else {
					showEmptyStates();
				}
				data.close();
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

	private void updateData() {
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();
	}
	//</editor-fold>

	//<editor-fold desc="Настройки">
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		mIsUpdateData = true;

		if (key.equals(getString(R.string.settings_key_sort_is_bought))) {
			readIsBoughtSetting();
		} else if (key.equals(getString(R.string.settings_key_use_category))) {
			readCategorySetting();
		} else if (key.equals(getString(R.string.settings_key_collapse_category))) {
			readCollapseCategorySetting();
		} else if (key.equals(getString(R.string.settings_key_sort_type))) {
			readSortTypeSetting();
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
		} else if (regular.contains(getString(R.string.sort_order_up_price))) {
			sortType = ShoppingList.UP_PRICE;
		} else if (regular.contains(getString(R.string.sort_order_down_price))) {
			sortType = ShoppingList.DOWN_PRICE;
		} else if (regular.contains(getString(R.string.sort_order_adding))) {
			sortType = ShoppingList.ORDER_ADDING;
		} else {
			sortType = ShoppingList.ALPHABET;
		}

		ShoppingList.setSortSettings(sortType);
	}
	//</editor-fold>

	//<editor-fold desc="Настройка списка">
	private void getList(long idList) {
		ListCursor cursor = mListsDS.get(idList);

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
		}

		editor.apply();
	}

	private void showEmptyStates() {
		mListContainer.setVisibility(View.GONE);
		mEmptyImage.setVisibility(View.VISIBLE);

		/*if (mActionMode != null) {
			mActionMode.finish();
		}*/
	}

	private void hideEmptyStates() {
		mListContainer.setVisibility(View.VISIBLE);
		mEmptyImage.setVisibility(View.GONE);
	}

	//<editor-fold desc="Удаление -">
	/*private void deleteSelectedItems() {
		ArrayList<ShoppingList> items = mAdapterRV.getSelectedItems();

		for (ShoppingList item : items) {
			new ShoppingListDS(getActivity()).delete(item.getIdItemData());
			mAdapterRV.removeItem(item);
		}

		mAdapterRV.getSelectedItems().clear();

		if (mCategories.size() > 0) {
			updateSums();
			hideEmptyStates();
		} else {
			showEmptyStates();
		}
	}*/
	//</editor-fold>

	//<editor-fold desc="Сумма списка">
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

		for (Category category : mCategories) {
			sum += category.calculateTotalSum();
		}

		mSaveTotalMoney = sum;
		return sum;
	}

	private double sumSpentMoney() {
		double sum = 0;

		for (Category category : mCategories) {
			sum += category.calculateSpentSum();
		}

		mSaveSpentMoney = sum;
		return sum;
	}
	//</editor-fold>

	//<editor-fold desc="Анимация -">
	/*private Animation getAnimationRV(final RecyclerView rv, final int oldHeight, final int newHeight, final boolean isUp) {
		Animation a = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (isUp) {
					rv.getLayoutParams().height = oldHeight - (int) ((oldHeight - newHeight) * interpolatedTime);
				} else {
					rv.getLayoutParams().height = oldHeight + (int) ((newHeight - oldHeight) * interpolatedTime);
				}
				rv.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		a.setDuration(500);

		return a;
	}*/
	//</editor-fold>

	//<editor-fold desc="Обучение -">
	/*private void showCaseView() {
		ShowcaseView showcaseView = new ShowcaseView.Builder(getActivity())
				.setTarget(new ViewTarget(mFAB))
				.setContentTitle(getString(R.string.showcase_create_item))
				.setContentText(getString(R.string.showcase_create_item_desc))
				.setStyle(R.style.Showcase)
				.singleShot(Showcase.SHOT_ADD_ITEM)
				.build();

		Showcase.newInstance(showcaseView, getActivity()).setButton(getString(R.string.ok), false);
	}

	private void showCaseViews() {
		CategoriesAdapter.ViewHolder categoryHolder = (CategoriesAdapter.ViewHolder) mItemsListRV.findViewHolderForLayoutPosition(0);
		CategoriesAdapter.ItemsAdapter.ViewHolder holder = (CategoriesAdapter.ItemsAdapter.ViewHolder) categoryHolder.mItemsInCategory.findViewHolderForLayoutPosition(0);

		mView = holder.mName;
		showcaseView = new ShowcaseView.Builder(getActivity())
				.setTarget(new ViewTarget(mView))
				.setContentTitle(getString(R.string.showcase_edit_item))
				.setContentText(getString(R.string.showcase_edit_item_desc))
				.setOnClickListener(this)
				.setStyle(R.style.Showcase)
				.singleShot(Showcase.SHOT_ITEM_IN_LIST)
				.build();

		showcaseView.forceTextPosition(ShowcaseView.ABOVE_SHOWCASE);
		Showcase.newInstance(showcaseView, getActivity()).setButton(getString(R.string.next), false);
	}


	@Override
	public void onClick(View v) {
		CategoriesAdapter.ViewHolder categoryHolder = (CategoriesAdapter.ViewHolder) mItemsListRV.findViewHolderForLayoutPosition(0);
		CategoriesAdapter.ItemsAdapter.ViewHolder holder = (CategoriesAdapter.ItemsAdapter.ViewHolder) categoryHolder.mItemsInCategory.findViewHolderForLayoutPosition(0);

		switch (counter) {
			case 1:
				showcaseView.setShowcase(new ViewTarget(mView), true);
				showcaseView.setContentTitle(getString(R.string.showcase_swipe_item));
				showcaseView.setContentText(getString(R.string.showcase_swipe_item_desc));
				break;
			case 2:
				holder.mIsBought.setVisibility(View.VISIBLE);
				showcaseView.setShowcase(new ViewTarget(mView), true);
				showcaseView.setContentTitle(getString(R.string.showcase_unswipe_item));
				showcaseView.setContentText(getString(R.string.showcase_unswipe_item_desc));
				break;
			case 3:
				holder.mIsBought.setVisibility(View.GONE);
				showcaseView.setShowcase(new ViewTarget(mView), true);
				showcaseView.setContentTitle(getString(R.string.showcase_delete_item));
				showcaseView.setContentText(getString(R.string.showcase_delete_item_desc));
				showcaseView.setButtonText(getString(R.string.close));
				break;
			case 4:
				showcaseView.hide();
				break;
		}
		counter++;
	}*/
	//</editor-fold>

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

	//<editor-fold desc="Адаптер -">
	/*public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {
		private final ArrayList<ShoppingList> mSelectedItems = new ArrayList<>();
		private HashMap<Long, ViewHolder> dataMap = new HashMap<>();
		private String mCurrencyList;

		public CategoriesAdapter(String currency) {
			mCurrencyList = currency;
		}

		public void setCurrency(String currency) {
			mCurrencyList = currency;
			notifyDataSetChanged();
		}

		@Override
		public CategoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._shopping_list_category, parent, false);
			return new ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			Category category = mCategories.get(position);
			ArrayList<ShoppingList> itemsInCategory = category.getItemsByCategoryInList();
			boolean isAllBought = (category.countBoughtItems() == itemsInCategory.size());

			for (ShoppingList item : itemsInCategory) {
				dataMap.put(item.getIdItem(), holder);
			}

			if (mIsUseCategory) {
				holder.mColor.setBackgroundColor(category.getColor());
				holder.mCategory.setText(category.getName());

				category.calculateSpentSum();
				holder.updateSpentCategorySum(category.getSpentSum());
			}

			if (!mIsCollapsedCategory
					|| (collapseCategoryStates.containsKey(category.getId()) && !collapseCategoryStates.get(category.getId()))
					|| (!collapseCategoryStates.containsKey(category.getId()) && !isAllBought)) {
				ViewGroup.LayoutParams params = holder.mItemsInCategory.getLayoutParams();
				params.height = getResources().getDimensionPixelSize(R.dimen.row_list_item_height) * itemsInCategory.size();
				holder.mItemsInCategory.setLayoutParams(params);
			} else if (isAllBought) {
				holder.mCategory.setPaintFlags(holder.mCategory.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}

			holder.mItemsInCategory.setAdapter(new ItemsAdapter(itemsInCategory));
		}

		@Override
		public int getItemCount() {
			return mCategories.size();
		}

		public int getPosition(long id) {
			int index = -1;
			for (int i = 0; i < mCategories.size(); ++i) {
				if (mCategories.get(i).getId() == id) {
					index = i;
					break;
				}
			}
			return index;
		}

		public ArrayList<ShoppingList> getSelectedItems() {
			return mSelectedItems;
		}

		public void clearSelections() {
			mSelectedItems.clear();
			dataMap.clear();
			notifyDataSetChanged();
		}

		public void toggleSelection(ShoppingList item) {
			toggleSelection(item, true);
		}

		public void toggleSelection(ShoppingList item, boolean removeIfExist) {
			if (mSelectedItems.contains(item)) {
				if (removeIfExist) {
					for (int i = 0; i < mSelectedItems.size(); i++) {
						if (mSelectedItems.get(i).getIdItem() == item.getIdItem()) {
							mSelectedItems.remove(i);
							break;
						}
					}
				}
			} else {
				mSelectedItems.add(item);
			}

			if (dataMap.containsKey(item.getIdItem())) {
				ItemsAdapter adapter = (ItemsAdapter) dataMap.get(item.getIdItem()).mItemsInCategory.getAdapter();
				adapter.notifyItemChanged(adapter.getItems().indexOf(item));
			}
		}

		public void selectItems(boolean isBought) {
			boolean isAllSelected = checkAllSelected(isBought);

			for (Category c : mCategories) {
				for (ShoppingList item : c.getItemsByCategoryInList()) {
					if (item.isBought() == isBought) {
						//if all item selected - remove selection from them
						//else select items
						toggleSelection(item, isAllSelected);
					}
				}
			}

		}

		private boolean checkAllSelected(boolean isBought) {
			for (Category c : mCategories) {
				for (ShoppingList item : c.getItemsByCategoryInList()) {
					if (item.isBought() == isBought) {
						if (!mSelectedItems.contains(item)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		public void removeItem(ShoppingList item) {
			if (dataMap.containsKey(item.getIdItem())) {
				ViewHolder holder = dataMap.get(item.getIdItem());
				RecyclerView rv = holder.mItemsInCategory;
				ItemsAdapter adapter = (ItemsAdapter) rv.getAdapter();

				if (adapter.getItemCount() == 1) {
					long idCategory = adapter.getItem(0).getIdCategory();
					int position = getPosition(idCategory);
					mCategories.remove(position);
					mAdapterRV.notifyItemRemoved(position);
				} else {
					adapter.removeItem(adapter.getItems().indexOf(item));

					Category category = adapter.getItem(0).getCategory();
					category.setSpentSum(category.getSpentSum() - item.getSum());
					holder.updateSpentCategorySum(category.getSpentSum());

					ViewGroup.LayoutParams params = rv.getLayoutParams();
					final int oldHeight = params.height;
					final int newHeight = getResources().getDimensionPixelSize(R.dimen.row_list_item_height) * rv.getAdapter().getItemCount();
					rv.startAnimation(getAnimationRV(rv, oldHeight, newHeight, true));

				}
			} else {
				for (int i = 0; i < mCategories.size(); i++) {
					ArrayList<ShoppingList> items = mCategories.get(i).getItemsByCategoryInList();

					if (items.size() == 1) {
						mCategories.remove(mCategories.get(i));
					} else {
						for (int j = 0; j < items.size(); j++) {
							if (items.get(j).getIdItem() == item.getIdItem()) {
								items.remove(j);
								break;
							}
						}
					}
				}
			}
		}

		@Override
		public void onViewAttachedToWindow(ViewHolder holder) {
			super.onViewAttachedToWindow(holder);

			holder.mCategory.post(new Runnable() {
				@Override
				public void run() {
					if (Showcase.shouldBeShown(getActivity(), Showcase.SHOT_ITEM_IN_LIST)) {
						showCaseViews();
					}
				}
			});
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public final TextView mCategory;
			public final RecyclerView mItemsInCategory;
			public LinearLayout mCategoryContainer;
			public TextView mColor;
			public TextView mSumCategory;

			public ViewHolder(View v) {
				super(v);
				mCategoryContainer = (LinearLayout) v.findViewById(R.id.category_container);
				mColor = (TextView) v.findViewById(R.id.color);
				mCategory = (TextView) v.findViewById(R.id.category);
				mSumCategory = (TextView) v.findViewById(R.id.sum_category);
				mItemsInCategory = (RecyclerView) v.findViewById(R.id.items_in_category);

				if (mIsUseCategory) {
					mCategoryContainer.setVisibility(View.VISIBLE);
				} else {
					mCategoryContainer.setVisibility(View.GONE);
				}

				mCategory.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!mIsCollapsedCategory) {
							return;
						}

						int itemPosition = getAdapterPosition();
						Category category = mCategories.get(itemPosition);
						Animation a;

						if (mItemsInCategory.getHeight() != 0) {
							a = getAnimationRV(mItemsInCategory, mItemsInCategory.getHeight(), 0, true);
							collapseCategoryStates.put(category.getId(), true);
						} else {
							int newHeight = getResources().getDimensionPixelSize(R.dimen.row_list_item_height) * mItemsInCategory.getAdapter().getItemCount();
							a = getAnimationRV(mItemsInCategory, mItemsInCategory.getHeight(), newHeight, false);
							collapseCategoryStates.put(category.getId(), false);
						}

						mItemsInCategory.clearAnimation();
						mItemsInCategory.startAnimation(a);
					}
				});

				mItemsInCategory.setLayoutManager(new LinearLayoutManager(getActivity()));
				mItemsInCategory.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mItemsInCategory, new RecyclerItemClickListener.OnItemClickListener() {
					@Override
					public void onItemClick(int position) {
						ItemsAdapter adapter = (ItemsAdapter) mItemsInCategory.getAdapter();
						ShoppingList item = adapter.getItem(position);

						if (mActionMode != null) {
							toggleSelection(item);
							return;
						}

						Intent i = new Intent(getActivity(), ItemActivity.class);
						i.putExtra(ItemActivity.EXTRA_ITEM, item);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							startActivityForResult(i, EDIT_ITEM, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
						} else {
							startActivityForResult(i, EDIT_ITEM);
						}
					}

					@Override
					public void onItemLongClick(int position) {
						if (mActionMode != null) {
							return;
						}

						mActionMode = getActivity().startActionMode(mActionModeCallback);

						ItemsAdapter adapter = (ItemsAdapter) mItemsInCategory.getAdapter();
						toggleSelection(adapter.getItem(position));
					}

					@Override
					public void onSwipeRight(int position) {
						if (mActionMode != null) {
							return;
						}

						ItemsAdapter adapter = (ItemsAdapter) mItemsInCategory.getAdapter();
						ShoppingList item = adapter.getItem(position);
						Category category = item.getCategory();
						boolean isBought = !item.isBought();

						//update data in db
						new ShoppingListDS(getActivity()).setIsBought(isBought, item.getIdItem(), mList.getId());

						//update adapter
						item.setBought(isBought);
						adapter.notifyItemChanged(position);

						//if set bought items in the end of list - refresh position
						if (mIsBoughtEndInList) {
							ShoppingList.sort(adapter.getItems());
							int toPosition = adapter.getItems().indexOf(item);

							if (toPosition != -1 && position != toPosition) {
								adapter.notifyItemMoved(position, toPosition);
								mItemsInCategory.scrollToPosition(toPosition);
							}
						}

						//update spent sum
						double sum = item.getSum();
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
						updateSpentCategorySum(categorySpentMoney);

						//if all items bought in the category - cross off the category else delete cross
						if (boughtItems == adapter.getItemCount()) {
							mCategory.setPaintFlags(mCategory.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
						} else {
							mCategory.setPaintFlags(mCategory.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
						}
					}
				}));

			}



			private void updateSpentCategorySum(double sum) {
				String sumText = localValue(sum) + " " + mCurrencyList;
				mSumCategory.setText(sumText);
			}
		}

		public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
			private ArrayList<ShoppingList> mItems = new ArrayList<>();

			public ItemsAdapter(ArrayList<ShoppingList> itemsInCategory) {
				mItems = itemsInCategory;
			}

			@Override
			public ItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._shopping_list_item, parent, false);
				return new ViewHolder(v);
			}

			@Override
			public void onBindViewHolder(ViewHolder holder, int position) {
				ShoppingList itemInList = mItems.get(position);

				if (mIsUseCategory) {
					holder.mColor.setBackgroundColor(itemInList.getCategory().getColor());
				}

				holder.mView.setSelected(mSelectedItems.contains(itemInList));
				Image.create().insertImageToView(getActivity(), itemInList.getItem().getImagePath(), holder.mImage);
				holder.mName.setText(itemInList.getItem().getName());

				if (itemInList.getAmount() == 0) {
					holder.mAmount.setText("-");
				} else {
					String amount = NumberFormat.getInstance().format(itemInList.getAmount())
							+ " " + itemInList.getUnit().getName();
					holder.mAmount.setText(amount);
				}

				String price = localValue(itemInList.getPrice()) + " " + mCurrencyList;
				holder.mPrice.setText(price);
				int visibility = View.GONE;

				if (itemInList.isBought()) {
					visibility = View.VISIBLE;
				}

				holder.mIsBought.setVisibility(visibility);
			}

			@Override
			public int getItemCount() {
				return mItems.size();
			}

			public ArrayList<ShoppingList> getItems() {
				return mItems;
			}

			public ShoppingList getItem(int position) {
				return mItems.get(position);
			}

			public void removeItem(int position) {
				mItems.remove(position);
				notifyItemRemoved(position);
			}

			public class ViewHolder extends RecyclerView.ViewHolder {
				public final View mView;
				public final ImageView mImage;
				public final TextView mName;
				public final TextView mAmount;
				public final TextView mPrice;
				public final View mIsBought;
				public TextView mColor;

				public ViewHolder(View v) {
					super(v);
					mView = v;
					mColor = (TextView) v.findViewById(R.id.color);
					mImage = (ImageView) v.findViewById(R.id.item_image_list);
					mName = (TextView) v.findViewById(R.id.item_name_list);
					mAmount = (TextView) v.findViewById(R.id.item_amount_list);
					mPrice = (TextView) v.findViewById(R.id.item_price_list);
					mIsBought = v.findViewById(R.id.is_bought_list);

					if (mIsUseCategory) {
						FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) mIsBought.getLayoutParams();
						param.setMargins(getResources().getDimensionPixelSize(R.dimen.padding_24dp), 0, getResources().getDimensionPixelSize(R.dimen.padding_16dp), 0);
						mIsBought.setLayoutParams(param);

						mColor.setVisibility(View.VISIBLE);
					} else {
						mColor.setVisibility(View.GONE);
					}

				}
			}
		}
	}*/
	//</editor-fold>
}
