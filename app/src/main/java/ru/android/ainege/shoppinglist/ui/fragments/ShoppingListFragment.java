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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Slide;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ShoppingListDataSource.ShoppingListCursor;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.entities.ShoppingList;
import ru.android.ainege.shoppinglist.ui.RecyclerItemClickListener;
import ru.android.ainege.shoppinglist.ui.activities.ItemActivity;
import ru.android.ainege.shoppinglist.ui.activities.SettingsActivity;


public class ShoppingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String ID_LIST = "idList";
	private static final int EDIT_FRAGMENT_CODE = 3;
	private static final int ANSWER_FRAGMENT_CODE = 4;
	private static final String EDIT_FRAGMENT_DATE = "editListDialog";
	private static final String ANSWER_FRAGMENT_DATE = "answerListDialog";
	private static final int DATA_LOADER = 0;

	private static final int ADD_ACTIVITY_CODE = 0;
	private static final int EDIT_ACTIVITY_CODE = 1;

	private ArrayList<ShoppingList> mItemsInList = new ArrayList<>();
	private CollapsingToolbarLayout mToolbarLayout;
	private FloatingActionButton mFAB;
	private RecyclerView mItemsListRV;
	private TextView mSpentMoney, mTotalMoney;
	private TextView mEmptyText;
	private FrameLayout mListContainer;
	private ProgressBar mProgressBar;
	private RecyclerViewAdapter mAdapterRV;
	private List mList;
	private double mSaveSpentMoney = 0;
	private double mSaveTotalMoney = 0;
	private boolean mIsBoughtEndInList;

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
				case R.id.move:
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
			getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
		}

		setHasOptionsMenu(true);
		getSettings();
		getList(getArguments().getLong(ID_LIST));

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

		ImageView appBarImage = (ImageView) v.findViewById(R.id.appbar_image);
		Image.create().insertImageToView(getActivity(),
				mList.getImagePath(),
				appBarImage);

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

		mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);

		mSpentMoney = (TextView) v.findViewById(R.id.spent_money);
		mTotalMoney = (TextView) v.findViewById(R.id.total_money);

		mEmptyText = (TextView) v.findViewById(R.id.empty_list);
		mListContainer = (FrameLayout) v.findViewById(R.id.list_container);

		mItemsListRV = (RecyclerView) v.findViewById(R.id.items_list);
		mItemsListRV.setLayoutManager(new LinearLayoutManager(getActivity()));
		mAdapterRV = new RecyclerViewAdapter(mList.getCurrency().getSymbol());
		mItemsListRV.setAdapter(mAdapterRV);
		mItemsListRV.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mItemsListRV, new RecyclerItemClickListener.OnItemClickListener() {
					@Override
					public void onItemClick(int position) {
						if (mActionMode != null) {
							mAdapterRV.toggleSelection(position);
							return;
						}

						ShoppingList item = mItemsInList.get(position);
						ShoppingList itemInList = new ShoppingList(item.getItem(),
								mList.getId(),
								item.isBought(),
								item.getAmount(),
								item.getUnit(),
								item.getPrice(),
								item.getComment());

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
						mAdapterRV.toggleSelection(position);
					}

					@Override
					public void onSwipeRight(int position) {
						if (mActionMode != null) {
							return;
						}

						//show is item cross off or not
						RecyclerViewAdapter.ViewHolder holder = (RecyclerViewAdapter.ViewHolder) mItemsListRV.findViewHolderForAdapterPosition(position);
						boolean isBought;
						if (holder.mIsBought.getVisibility() == View.VISIBLE) {
							holder.mIsBought.setVisibility(View.GONE);
							isBought = false;
						} else {
							holder.mIsBought.setVisibility(View.VISIBLE);
							isBought = true;
						}

						//update data in db
						ShoppingListDataSource itemsInListDS;
						try {
							itemsInListDS = ShoppingListDataSource.getInstance();
						} catch (NullPointerException e) {
							itemsInListDS = ShoppingListDataSource.getInstance(getActivity());
						}
						ShoppingList item = mItemsInList.get(position);
						itemsInListDS.setIsBought(isBought, item.getIdItem(), mList.getId());

						//update recyclerView
						//if set bought items in the end of list - refresh list
						//if it isn`t - try to update only spent sum
						mAdapterRV.getItem(position).setBought(isBought);

						if (mIsBoughtEndInList) {
							ShoppingList.sort(mItemsInList);
							int toPosition = getPosition(item.getIdItem());
							if (toPosition != -1) {
								mAdapterRV.notifyItemMoved(position, toPosition);
								mItemsListRV.scrollToPosition(toPosition);
							}
						}

						double sum = sumOneItem(item);
						if (mSaveSpentMoney != 0) {
							if (isBought) {
								mSaveSpentMoney += sum;
							} else {
								mSaveSpentMoney -= sum;
							}
							updateSpentSum(mSaveSpentMoney);
						} else {
							updateData();
						}
					}
				})
		);

		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
				Toast.makeText(getActivity(), swipeDir + " swipe for delete", Toast.LENGTH_SHORT).show();
			}

		});
		itemTouchHelper.attachToRecyclerView(mItemsListRV);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		getSettings();
		updateData();
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
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
					mItemsInList = ((ShoppingListCursor) data).getEntities();
					ShoppingList.sort(mItemsInList);

					mAdapterRV.notifyDataSetChanged();     //update data in adapter

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
				ListsDataSource listsDS = new ListsDataSource(getActivity());
				listsDS.delete(mList.getId());
				Image.deleteFile(mList.getImagePath());

				getActivity().onBackPressed();
				break;
			case EDIT_FRAGMENT_CODE:
				getList(getArguments().getLong(ID_LIST));
				setTitle();
				updateSums(mSaveSpentMoney, mSaveTotalMoney);
				mAdapterRV.setCurrency(mList.getCurrency().getSymbol());
				break;
		}
	}

	private void getSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		mIsBoughtEndInList = prefs.getBoolean(getString(R.string.settings_key_sort_is_bought), true);
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
	}

	private String localValue(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(value);
	}

	private void getList(long idList) {
		ListsDataSource.ListCursor cursor = new ListsDataSource(getActivity()).get(idList);
		if (cursor.moveToFirst()) {
			mList = cursor.getEntity();
		}
		cursor.close();
	}

	private void setTitle() {
		mToolbarLayout.setTitle(mList.getName());
	}

	private void showEmptyStates() {
		mListContainer.setVisibility(View.GONE);
		mEmptyText.setVisibility(View.VISIBLE);

		if (mActionMode != null) {
			mActionMode.finish();
		}
	}

	private void hideEmptyStates() {
		mListContainer.setVisibility(View.VISIBLE);
		mEmptyText.setVisibility(View.GONE);
	}

	private void updateData() {
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();
	}

	private void deleteSelectedItems() {
		ArrayList<Integer> items = mAdapterRV.getSelectedItems();
		Collections.sort(items);

		ShoppingListDataSource itemInListDS = ShoppingListDataSource.getInstance();
		for (int i = items.size() - 1; i >= 0; i--) {
			int position = items.get(i);
			itemInListDS.delete(mItemsInList.get(position).getIdItem(), mList.getId());
			mAdapterRV.removeItem(position);

			if (mItemsInList.size() > 0) {
				updateSums();
				hideEmptyStates();
			} else {
				showEmptyStates();
			}
		}
	}

	private int getPosition(long id) {
		int index = -1;
		for (int i = 0; i < mItemsInList.size(); ++i) {
			if (mItemsInList.get(i).getIdItem() == id) {
				index = i;
				break;
			}
		}
		return index;
	}

	private double sumMoney(boolean onlyBought) {
		double sum = 0;
		for (ShoppingList item : mItemsInList) {
			if ((onlyBought && item.isBought()) || !onlyBought) {
				sum += sumOneItem(item);
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
		double price = item.getPrice();
		double amount = item.getAmount();
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

	public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		private final ArrayList<Integer> mSelectedItems = new ArrayList<>();
		private String mCurrencyList;

		public RecyclerViewAdapter(String currency) {
			mCurrencyList = currency;
		}

		public void setCurrency(String currency) {
			mCurrencyList = currency;
			notifyDataSetChanged();
		}

		@Override
		public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._shopping_list_item, parent, false);
			return new ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			ShoppingList itemInList = mItemsInList.get(position);

			holder.mView.setSelected(mSelectedItems.contains(position));
			Image.create().insertImageToView(getActivity(), itemInList.getItem().getImagePath(), holder.mImage);
			holder.mTextView.setText(itemInList.getItem().getName());

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
			return mItemsInList.size();
		}

		public void removeItem(int position) {
			mItemsInList.remove(position);
			if (mSelectedItems.contains(position)) {
				mSelectedItems.remove(mSelectedItems.indexOf(position));
			}
			notifyItemRemoved(position);
		}

		public ShoppingList getItem(int position) {
			return mItemsInList.get(position);
		}

		public void toggleSelection(int position) {
			if (mSelectedItems.contains(position)) {
				for (int i = 0; i < mSelectedItems.size(); i++) {
					if (mSelectedItems.get(i) == position) {
						mSelectedItems.remove(i);
						break;
					}
				}
			} else {
				mSelectedItems.add(position);
			}
			notifyItemChanged(position);
		}

		public void clearSelections() {
			mSelectedItems.clear();
			notifyDataSetChanged();
		}

		public void selectItems(boolean isBought) {
			for (ShoppingList item : mItemsInList) {
				if (item.isBought() == isBought) {
					int position = mItemsInList.indexOf(item);
					if (!mSelectedItems.contains(position)) {
						mSelectedItems.add(position);
						notifyItemChanged(position);
					}
				}
			}
		}

		public ArrayList<Integer> getSelectedItems() {
			return mSelectedItems;
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public final View mView;
			public final ImageView mImage;
			public final TextView mTextView;
			public final TextView mAmount;
			public final TextView mPrice;
			public final View mIsBought;

			public ViewHolder(View v) {
				super(v);
				mView = v;
				mImage = (ImageView) v.findViewById(R.id.item_image_list);
				mTextView = (TextView) v.findViewById(R.id.item_name_list);
				mAmount = (TextView) v.findViewById(R.id.item_amount_list);
				mPrice = (TextView) v.findViewById(R.id.item_price_list);
				mIsBought = v.findViewById(R.id.is_bought_list);
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
			ShoppingListDataSource mItemsInListDS;
			try {
				mItemsInListDS = ShoppingListDataSource.getInstance();
			} catch (NullPointerException e) {
				mItemsInListDS = ShoppingListDataSource.getInstance(mContext);
			}

			return mItemsInListDS.getItemsInList(mIdList);
		}
	}
}
