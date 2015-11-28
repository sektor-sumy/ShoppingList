package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
	public static final String ID_LIST = "idList";
	public static final String IS_BOUGHT_END_IN_LIST = "isBoughtEndInList";
	public static final String DATA_SAVE = "dataSave";

	private static final String EDIT_FRAGMENT_DATE = "editListDialog";
	public static final int EDIT_FRAGMENT_CODE = 3;
	private static final String ANSWER_FRAGMENT_DATE = "answerListDialog";
	public static final int ANSWER_FRAGMENT_CODE = 4;

	private static final int DATA_LOADER = 0;

	private static final int ADD_ACTIVITY_CODE = 0;
	private static final int EDIT_ACTIVITY_CODE = 1;

	private CollapsingToolbarLayout mToolbarLayout;
	private RecyclerView mItemsListRV;
	private TextView mSpentMoney, mTotalMoney;
	private TextView mEmptyText;
	private LinearLayout mListContainer;
	private RecyclerViewAdapter mAdapterRV;

	private ArrayList<ShoppingList> mItemsInList;
	private List mList;
	private double mSaveSpentMoney;
	private boolean mIsBoughtEndInList;
	private int mPositionCrossOffItem = -1;
	private long mIdCrossOffItem;

	private android.view.ActionMode mActionMode;
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.cab_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
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
			mActionMode = null;
		}
	};

	public static ShoppingListFragment newInstance(long id, boolean isBoughtEndInList, String dataSave) {
		Bundle args = new Bundle();
		args.putLong(ID_LIST, id);
		args.putBoolean(IS_BOUGHT_END_IN_LIST, isBoughtEndInList);
		args.putString(DATA_SAVE, dataSave);

		ShoppingListFragment fragment = new ShoppingListFragment();
		fragment.setArguments(args);

		return fragment;
	}

	private static String localValue(double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
		return nf.format(value);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		}

		setHasOptionsMenu(true);

		getList(getArguments().getLong(ID_LIST));

		mIsBoughtEndInList = getArguments().getBoolean(IS_BOUGHT_END_IN_LIST);

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	private void getList(long idList) {
		ListsDataSource.ListCursor cursor = new ListsDataSource(getActivity()).get(idList);
		if (cursor.moveToFirst()) {
			mList = cursor.getEntity();
		}
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
		appBarImage.setImageResource(R.drawable.list);

		FloatingActionButton addItemFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		addItemFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ItemActivity.class);
				i.putExtra(ItemActivity.EXTRA_ID_LIST, mList.getId());
				i.putExtra(ItemActivity.EXTRA_DATA_SAVE, getArguments().getString(DATA_SAVE));

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					startActivityForResult(i, ADD_ACTIVITY_CODE, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
				} else {
					startActivityForResult(i, ADD_ACTIVITY_CODE);
				}
			}
		});

		mSpentMoney = (TextView) v.findViewById(R.id.spent_money);
		mTotalMoney = (TextView) v.findViewById(R.id.total_money);

		mEmptyText = (TextView) v.findViewById(R.id.empty_list);
		mListContainer = (LinearLayout) v.findViewById(R.id.list_container);

		mItemsListRV = (RecyclerView) v.findViewById(R.id.items_list);
		mItemsListRV.setLayoutManager(new LinearLayoutManager(getActivity()));
		mAdapterRV = new RecyclerViewAdapter(mList.getCurrency().getSymbol());
		mItemsListRV.setAdapter(mAdapterRV);
		mItemsListRV.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mItemsListRV, new RecyclerItemClickListener.OnItemClickListener() {
					@Override
					public void onItemClick(View view, int position) {
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
						i.putExtra(ItemActivity.EXTRA_DATA_SAVE, getArguments().getString(DATA_SAVE));

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							startActivityForResult(i, EDIT_ACTIVITY_CODE, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
						} else {
							startActivityForResult(i, EDIT_ACTIVITY_CODE);
						}
					}

					@Override
					public void onItemLongClick(View view, int position) {
						if (mActionMode != null) {
							return;
						}

						mActionMode = getActivity().startActionMode(mActionModeCallback);
						mAdapterRV.toggleSelection(position);
					}

					@Override
					public void onSwipeRight(View view, int position) {
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

						//update recyclerview
						//if set bought items in the end of list - refresh list
						//if it isn`t - try to update only spent sum
						if (mIsBoughtEndInList) {
							mPositionCrossOffItem = position;
							mIdCrossOffItem = item.getIdItem();
							updateData();
						} else {
							mAdapterRV.getItem(position).setBought(isBought);
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

	private void setTitle() {
		mToolbarLayout.setTitle(mList.getName());
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
				QuestionDialogFragment dialogFrag = new QuestionDialogFragment();
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
				startActivity(i);
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
				if (data.moveToFirst()) {
					mItemsInList = ((ShoppingListCursor) data).getEntities();
					//if cross off item in list - find new it position and move
					//else set new data to adapter
					if (mPositionCrossOffItem != -1) {
						int toPosition = getPosition(mIdCrossOffItem);
						if (toPosition != -1) {
							mAdapterRV.notifyItemMoved(mPositionCrossOffItem, toPosition);
							mItemsListRV.scrollToPosition(toPosition);
							mAdapterRV.setData(mItemsInList);
						}
					} else {
						mAdapterRV.setData(mItemsInList, true);       //update data in adapter
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
				if (mAdapterRV != null) {
					//mAdapterRV.swapCursor(null);
				}
				break;
			default:
				break;
		}
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

				getActivity().onBackPressed();
				break;
			case EDIT_FRAGMENT_CODE:
				getList(getArguments().getLong(ID_LIST));
				setTitle();
				mAdapterRV.setCurrency(mList.getCurrency().getSymbol());
				break;
		}
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
		updateSpentSum(sumMoney(true));        //update spent money
		mTotalMoney.setText(localValue(sumMoney(false)));       //update total money
	}

	private void updateSpentSum(double newSum) {
		mSpentMoney.setText(localValue(newSum));
	}

	public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		private ArrayList<ShoppingList> mItems;
		private ArrayList<Integer> mSelectedItems = new ArrayList<>();
		private String mCurrencyList;

		public RecyclerViewAdapter(String currency) {
			mCurrencyList = currency;
			mItems = new ArrayList<>();
		}

		public void setData(ArrayList<ShoppingList> items) {
			mItems = items;
		}

		public void setData(ArrayList<ShoppingList> items, boolean isNeedNotify) {
			setData(items);
			if (isNeedNotify) {
				notifyDataSetChanged();
			}
		}

		public void setCurrency(String currency) {
			mCurrencyList = currency;
			notifyDataSetChanged();
		}

		@Override
		public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._shopping_list_item, parent, false);
			ViewHolder vh = new ViewHolder(v);
			return vh;
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			ShoppingList itemInList = mItems.get(position);

			holder.mView.setSelected(mSelectedItems.contains(position));

			holder.mImage.setImageResource(R.drawable.food);
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
			return mItems.size();
		}

		public void removeItem(int position) {
			mItems.remove(position);
			if (mSelectedItems.contains(position)) {
				mSelectedItems.remove(mSelectedItems.indexOf(position));
			}
			notifyItemRemoved(position);
		}

		public ShoppingList getItem(int position) {
			return mItems.get(position);
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
			for (ShoppingList item : mItems) {
				if (item.isBought() == isBought) {
					int position = mItems.indexOf(item);
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

		public static class ViewHolder extends RecyclerView.ViewHolder {
			public View mView;
			public ImageView mImage;
			public TextView mTextView;
			public TextView mAmount;
			public TextView mPrice;
			public View mIsBought;

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
		private Context mContext;
		private long mIdList;

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
