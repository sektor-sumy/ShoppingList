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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.ui.activities.SettingsActivity;
import ru.android.ainege.shoppinglist.ui.activities.ShoppingListActivity;

public class ListsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int ADD_FRAGMENT_CODE = 1;
	private static final int EDIT_FRAGMENT_CODE = 2;
	private static final int DATA_LOADER = 0;
	private static final String ADD_FRAGMENT_DATE = "addListDialog";
	private static final String EDIT_FRAGMENT_DATE = "editListDialog";
	private RecyclerView mListsRV;
	private TextView mEmptyText;
	private RecyclerViewAdapter mAdapterRV;
	ArrayList<List> mLists = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_lists, container, false);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

		FloatingActionButton addItemFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		addItemFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ListDialogFragment addListDialog = new ListDialogFragment();
				addListDialog.setTargetFragment(ListsFragment.this, ADD_FRAGMENT_CODE);
				addListDialog.show(getFragmentManager(), ADD_FRAGMENT_DATE);
			}
		});

		mListsRV = (RecyclerView) v.findViewById(R.id.lists);
		mListsRV.setLayoutManager(new LinearLayoutManager(getActivity()));

		mEmptyText = (TextView) v.findViewById(R.id.empty_list);

		mAdapterRV = new RecyclerViewAdapter();
		mListsRV.setAdapter(mAdapterRV);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateData();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.list_of_lists_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
				loader = new ListsCursorLoader(getActivity());
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
					mLists = ((ListsDataSource.ListCursor) data).getEntities();
					mAdapterRV.notifyDataSetChanged();
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

	}

	private void updateData() {
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) return;

		switch (requestCode) {
			case ADD_FRAGMENT_CODE:
				updateData();
				break;
			case EDIT_FRAGMENT_CODE:
				updateData();
				break;
		}
	}

	private void showEmptyStates() {
		mListsRV.setVisibility(View.GONE);
		mEmptyText.setVisibility(View.VISIBLE);
	}

	private void hideEmptyStates() {
		mListsRV.setVisibility(View.VISIBLE);
		mEmptyText.setVisibility(View.GONE);
	}

	private static class ListsCursorLoader extends CursorLoader {
		private final Context mContext;

		public ListsCursorLoader(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public Cursor loadInBackground() {
			ListsDataSource mListsDS = new ListsDataSource(mContext);

			return mListsDS.getAll();
		}
	}

	public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

		@Override
		public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._list_item, parent, false);
			return new ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			List list = mLists.get(position);

			holder.mImage.setImageURI(Uri.parse(list.getImagePath()));

			holder.mName.setText(list.getName());

			int amountItem = list.getAmountItems();
			String statisticsShopping;

			if (amountItem == 0) {
				statisticsShopping = "Список пуст";
			} else {
				int amountBoughtItems = list.getAmountBoughtItems();

				if (amountBoughtItems == 0) {
					statisticsShopping = "Ничего не куплено. Всего " + amountItem;
				} else {
					if (amountItem == amountBoughtItems) {
						statisticsShopping = "Все куплено";
					} else {
						statisticsShopping = "Куплено " + amountBoughtItems + " из " + amountItem;
					}
				}
			}

			holder.mStatisticsShopping.setText(statisticsShopping);
		}

		@Override
		public int getItemCount() {
			return mLists.size();
		}

		public void removeItem(int position) {
			mLists.remove(position);
			notifyItemRemoved(position);
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public final View view;
			public final ImageView mImage;
			public final TextView mName;
			public final TextView mStatisticsShopping;
			public final ImageButton mEdit;
			public final ImageButton mDelete;

			public ViewHolder(View v) {
				super(v);
				view = v;
				mImage = (ImageView) v.findViewById(R.id.image_list);
				mName = (TextView) v.findViewById(R.id.name_list);
				mStatisticsShopping = (TextView) v.findViewById(R.id.statistics_shopping);
				mEdit = (ImageButton) v.findViewById(R.id.edit_list);
				mDelete = (ImageButton) v.findViewById(R.id.delete_list);

				mEdit.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						List list = mLists.get(itemPosition);

						ListDialogFragment editListDialog = ListDialogFragment.newInstance(list);
						editListDialog.setTargetFragment(ListsFragment.this, EDIT_FRAGMENT_CODE);
						editListDialog.show(getFragmentManager(), EDIT_FRAGMENT_DATE);
					}
				});

				mDelete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						List list = mLists.get(itemPosition);

						ListsDataSource listsDS = new ListsDataSource(getActivity());
						listsDS.delete(list.getId());
						removeItem(itemPosition);

						if (mLists.size() > 0) {
							hideEmptyStates();
						} else {
							showEmptyStates();
						}
					}
				});
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						List list = mLists.get(itemPosition);

						Intent i = new Intent(v.getContext(), ShoppingListActivity.class);
						i.putExtra(ShoppingListActivity.EXTRA_ID_LIST, list.getId());
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
						} else {
							startActivity(i);
						}
					}
				});
			}
		}
	}
}
