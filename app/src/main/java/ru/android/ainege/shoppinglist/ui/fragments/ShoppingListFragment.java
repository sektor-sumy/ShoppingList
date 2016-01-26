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
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.dataSources.ItemDataDS;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDS.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.Image;
import ru.android.ainege.shoppinglist.ui.RecyclerItemClickListener;
import ru.android.ainege.shoppinglist.ui.Showcase;
import ru.android.ainege.shoppinglist.ui.activities.ItemActivity;
import ru.android.ainege.shoppinglist.ui.activities.SettingsActivity;

import static ru.android.ainege.shoppinglist.db.dataSources.ListsDS.ListCursor;


public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
	private static final String APP_PREFERENCES = "shopping_list_settings";
	private static final String APP_PREFERENCES_ID = "idList";

	private static final String ID_LIST = "idList";
	private static final int EDIT_FRAGMENT_CODE = 3;
	private static final int ANSWER_FRAGMENT_CODE = 4;
	private static final String EDIT_FRAGMENT_DATE = "editListDialog";
	private static final String ANSWER_FRAGMENT_DATE = "answerListDialog";
	private static final int DATA_LOADER = 0;

	private static final int ADD_ACTIVITY_CODE = 0;
	private static final int EDIT_ACTIVITY_CODE = 1;

	private ListsDS mListsDS;

	private ArrayList<Category> mCategories = new ArrayList<>();
	private CollapsingToolbarLayout mToolbarLayout;
	private ImageView mListImage;
	private FloatingActionButton mFAB;
	private RecyclerView mItemsListRV;
	private TextView mSpentMoney, mTotalMoney;
	private ImageView mEmptyImage;
	private LinearLayout mListContainer;
	private ProgressBar mProgressBar;
	private CategoriesAdapter mAdapterRV;
	private List mList;
	private double mSaveSpentMoney = 0;
	private double mSaveTotalMoney = 0;
	private boolean mIsBoughtEndInList;
	private boolean mIsUseCategory;

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
	};

	private ShowcaseView showcaseView;
	private int counter = 1;
	private View mView;

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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		} else {
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}

		setHasOptionsMenu(true);

		mListsDS = new ListsDS(getActivity());
		getList(getArguments().getLong(ID_LIST));
		getSettings();

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
		setTitle();

		mListImage = (ImageView) v.findViewById(R.id.appbar_image);
		loadImage();

		mFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ItemActivity.class);
				i.putExtra(ItemActivity.EXTRA_ID_LIST, mList.getId());

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					startActivityForResult(i, ADD_ACTIVITY_CODE, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
				} else {
					startActivityForResult(i, ADD_ACTIVITY_CODE);
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
		mAdapterRV = new CategoriesAdapter(mList.getCurrency().getSymbol());
		mItemsListRV.setAdapter(mAdapterRV);

		showCaseView();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		getList(getArguments().getLong(ID_LIST));

		getSettings();
		updateData();
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
				dialogFrag.setTargetFragment(ShoppingListFragment.this, ANSWER_FRAGMENT_CODE);
				dialogFrag.show(getFragmentManager(), ANSWER_FRAGMENT_DATE);
				return true;
			case R.id.update_list:
				ListDialogFragment editListDialog = ListDialogFragment.newInstance(mList);
				editListDialog.setTargetFragment(ShoppingListFragment.this, EDIT_FRAGMENT_CODE);
				editListDialog.show(getFragmentManager(), EDIT_FRAGMENT_DATE);
				return true;
			case R.id.settings:
				Intent i = new Intent(getActivity(), SettingsActivity.class);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
				} else {
					startActivity(i);
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
			case ADD_ACTIVITY_CODE:
				updateData();
				break;
			case EDIT_ACTIVITY_CODE:
				updateData();
				break;
			case ANSWER_FRAGMENT_CODE:
				mListsDS.delete(mList.getId());
				Image.deleteFile(mList.getImagePath());

				saveId(-1);
				getActivity().onBackPressed();
				break;
			case EDIT_FRAGMENT_CODE:
				getList(getArguments().getLong(ID_LIST));
				loadImage();
				setTitle();
				updateSums(mSaveSpentMoney, mSaveTotalMoney);
				mAdapterRV.setCurrency(mList.getCurrency().getSymbol());
				break;
		}
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
	}

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
					ArrayList<ShoppingList> itemsInList = ((CategoriesDS.CategoryCursor) data).getItemsInListCursor().getEntities();
					if (mIsUseCategory) {
						mCategories = ((CategoriesDS.CategoryCursor) data).getCategoriesWithItems(itemsInList);
					} else {
						ShoppingList.sort(itemsInList);
						ArrayList<Category> categories = new ArrayList<>();
						categories.add(new Category(itemsInList));
						mCategories = categories;
					}

					mAdapterRV.setCurrency(mList.getCurrency().getSymbol());     //update data in adapter

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

	private void getSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		mIsBoughtEndInList = prefs.getBoolean(getString(R.string.settings_key_sort_is_bought), true);
		mIsUseCategory = prefs.getBoolean(getString(R.string.settings_key_use_category), true);
		String sortType;
		String regular = prefs.getString(getString(R.string.settings_key_sort_type), "");

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

		ShoppingList.setSortSettings(mIsBoughtEndInList, sortType);
		saveId(mList.getId());
	}

	private void getList(long idList) {
		ListCursor cursor = mListsDS.get(idList);

		if (cursor.moveToFirst()) {
			mList = cursor.getEntity();
		}

		cursor.close();
	}

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

	private void setTitle() {
		mToolbarLayout.setTitle(mList.getName());
	}

	protected void loadImage() {
		Image.create().insertImageToView(getActivity(),
				mList.getImagePath(),
				mListImage);
	}

	private void showEmptyStates() {
		mListContainer.setVisibility(View.GONE);
		mEmptyImage.setVisibility(View.VISIBLE);

		if (mActionMode != null) {
			mActionMode.finish();
		}
	}

	private void hideEmptyStates() {
		mListContainer.setVisibility(View.VISIBLE);
		mEmptyImage.setVisibility(View.GONE);
	}

	private void deleteSelectedItems() {
		ArrayList<ShoppingList> items = mAdapterRV.getSelectedItems();

		for (ShoppingList item : items) {
			new ItemDataDS(getActivity()).delete(item.getIdItemData());
			mAdapterRV.removeItem(item);
		}

		mAdapterRV.getSelectedItems().clear();

		if (mCategories.size() > 0) {
			updateSums();
			hideEmptyStates();
		} else {
			showEmptyStates();
		}
	}

	private String localValue(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(value);
	}

	private double sumMoney(boolean onlyBought) {
		double sum = 0;

		for (Category category : mCategories) {
			for (ShoppingList item : category.getItemsByCategoryInList()) {
				if ((onlyBought && item.isBought()) || !onlyBought) {
					sum += sumOneItem(item);
				}
			}
		}

		if (onlyBought) {
			mSaveSpentMoney = sum;
		} else {
			mSaveTotalMoney = sum;
		}

		return sum;
	}

	private double sumOneItem(ShoppingList item) {
		double price = item.getItemData().getPrice();
		double amount = item.getItemData().getAmount();
		double sum = price * (amount == 0 ? 1 : amount);
		return new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	private void updateSums() {
		updateSums(sumMoney(true), sumMoney(false));
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

	private void showCaseView() {
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

	public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {
		private HashMap<Long, RecyclerView> dataMap = new HashMap<>();
		private final ArrayList<ShoppingList> mSelectedItems = new ArrayList<>();
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

			if (mIsUseCategory) {
				String categoryName = category.getName();
				holder.mCategory.setText(categoryName);
				holder.mCategory.setVisibility(View.VISIBLE);
			} else {
				holder.mCategory.setVisibility(View.GONE);
			}


			setRV(category.getItemsByCategoryInList(), holder);
		}

		private void setRV(ArrayList<ShoppingList> itemsInCategory, final ViewHolder holder) {
			for (ShoppingList item : itemsInCategory) {
				dataMap.put(item.getIdItem(), holder.mItemsInCategory);
			}

			ViewGroup.LayoutParams params = holder.mItemsInCategory.getLayoutParams();
			params.height = getResources().getDimensionPixelSize(R.dimen.row_list_height) * itemsInCategory.size();
			holder.mItemsInCategory.setLayoutParams(params);

			holder.mItemsInCategory.setLayoutManager(new LinearLayoutManager(getActivity()));
			holder.mItemsInCategory.setAdapter(new ItemsAdapter(itemsInCategory));
			holder.mItemsInCategory.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), holder.mItemsInCategory, new RecyclerItemClickListener.OnItemClickListener() {
				@Override
				public void onItemClick(int position) {
					ItemsAdapter adapter = (ItemsAdapter) holder.mItemsInCategory.getAdapter();
					ShoppingList item = adapter.getItem(position);

					if (mActionMode != null) {
						toggleSelection(item);
						return;
					}

					ShoppingList itemInList = new ShoppingList(item.getItem(),
							mList.getId(),
							item.isBought(),
							item.getItemData());

					Intent i = new Intent(getActivity(), ItemActivity.class);
					i.putExtra(ItemActivity.EXTRA_ITEM, itemInList);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						startActivityForResult(i, EDIT_ACTIVITY_CODE, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
					} else {
						startActivityForResult(i, EDIT_ACTIVITY_CODE);
					}
				}

				@Override
				public void onItemLongClick(int position) {
					if (mActionMode != null) {
						return;
					}

					mActionMode = getActivity().startActionMode(mActionModeCallback);

					ItemsAdapter adapter = (ItemsAdapter) holder.mItemsInCategory.getAdapter();
					toggleSelection(adapter.getItem(position));
				}

				@Override
				public void onSwipeRight(int position) {
					if (mActionMode != null) {
						return;
					}

					RecyclerView rv = holder.mItemsInCategory;
					ItemsAdapter adapter = (ItemsAdapter) rv.getAdapter();
					ShoppingList item = adapter.getItem(position);
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
							rv.scrollToPosition(toPosition);
						}
					}

					//update spent sum
					double sum = sumOneItem(item);

					if (isBought) {
						mSaveSpentMoney += sum;
					} else {
						mSaveSpentMoney -= sum;
					}

					updateSpentSum(mSaveSpentMoney);
				}
			}));
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
				ItemsAdapter adapter = (ItemsAdapter) dataMap.get(item.getIdItem()).getAdapter();
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
				RecyclerView rv = dataMap.get(item.getIdItem());
				ItemsAdapter adapter = (ItemsAdapter) rv.getAdapter();

				if (adapter.getItemCount() == 1) {
					long idCategory = adapter.getItem(0).getItemData().getIdCategory();
					int position = getPosition(idCategory);
					mCategories.remove(position);
					mAdapterRV.notifyItemRemoved(position);
				} else {
					int position = adapter.getItems().indexOf(item);
					adapter.getItems().remove(position);
					adapter.notifyItemRemoved(position);

					setHeightRV(rv);
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
							}
						}
					}
				}
			}
		}

		private void setHeightRV(final RecyclerView rv) {
			ViewGroup.LayoutParams params = rv.getLayoutParams();
			final int oldHeight = params.height;
			final int newHeight = getResources().getDimensionPixelSize(R.dimen.row_list_height) * rv.getAdapter().getItemCount();

			Animation a = new Animation() {
				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					rv.getLayoutParams().height = oldHeight - (int) ((oldHeight - newHeight) * interpolatedTime);
					rv.requestLayout();
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};
			a.setDuration(500);
			rv.startAnimation(a);
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

			public ViewHolder(View v) {
				super(v);
				mCategory = (TextView) v.findViewById(R.id.category);
				mItemsInCategory = (RecyclerView) v.findViewById(R.id.items_in_category);
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

				holder.mView.setSelected(mSelectedItems.contains(itemInList));
				Image.create().insertImageToView(getActivity(), itemInList.getItem().getImagePath(), holder.mImage);
				holder.mName.setText(itemInList.getItem().getName());

				if (itemInList.getItemData().getAmount() == 0) {
					holder.mAmount.setText("-");
				} else {
					String amount = NumberFormat.getInstance().format(itemInList.getItemData().getAmount())
							+ " " + itemInList.getItemData().getUnit().getName();
					holder.mAmount.setText(amount);
				}

				String price = localValue(itemInList.getItemData().getPrice()) + " " + mCurrencyList;
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

			public class ViewHolder extends RecyclerView.ViewHolder {
				public final View mView;
				public final ImageView mImage;
				public final TextView mName;
				public final TextView mAmount;
				public final TextView mPrice;
				public final View mIsBought;

				public ViewHolder(View v) {
					super(v);
					mView = v;
					mImage = (ImageView) v.findViewById(R.id.item_image_list);
					mName = (TextView) v.findViewById(R.id.item_name_list);
					mAmount = (TextView) v.findViewById(R.id.item_amount_list);
					mPrice = (TextView) v.findViewById(R.id.item_price_list);
					mIsBought = v.findViewById(R.id.is_bought_list);
				}
			}
		}
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
			CategoriesDS.CategoryCursor categoryCursor = new CategoriesDS(mContext).getCategoriesInList(mIdList);

			if (categoryCursor.moveToFirst()) {
				ShoppingListCursor itemsCursor = new ShoppingListDS(mContext).getItemsInList(mIdList);
				categoryCursor.setItemsInListCursor(itemsCursor);

			}

			return categoryCursor;
		}
	}
}
