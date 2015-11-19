package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
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
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.ui.RecyclerItemClickListener;
import ru.android.ainege.shoppinglist.ui.activities.SettingsActivity;

public class ListsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int DATA_LOADER = 0;

	private RecyclerView mListsRV;
	private RecyclerViewAdapter mAdapterRV;
	private ArrayList<List> mLists;

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

			}
		});

		mListsRV = (RecyclerView) v.findViewById(R.id.lists);
		mListsRV.setLayoutManager(new LinearLayoutManager(getActivity()));

		mAdapterRV = new RecyclerViewAdapter();
		mListsRV.setAdapter(mAdapterRV);

		mListsRV.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), mListsRV, new RecyclerItemClickListener.OnItemClickListener() {
					@Override
					public void onItemClick(View view, int position) {

					}

					@Override
					public void onItemLongClick(View view, int position) {

					}

					@Override
					public void onSwipeRight(View view, int position) {

					}
				})
		);

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.list_menu, menu);
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
					mLists = ((ListsDataSource.ListCursor) data).getLists();
					mAdapterRV.setData(mLists);
				} else {
				}
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

	public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
		private ArrayList<List> mLists;
		private ArrayList<Integer> mSelectedLists = new ArrayList<>();

		public RecyclerViewAdapter() {
			mLists = new ArrayList<>();
		}

		public void setData(ArrayList<List> lists) {
			mLists = lists;
		}

		public void setData(ArrayList<List> lists, boolean isNeedNotify) {
			setData(lists);
			if (isNeedNotify) {
				notifyDataSetChanged();
			}
		}

		@Override
		public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout._list_item, parent, false);
			ViewHolder vh = new ViewHolder(v);
			return vh;
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			List list = mLists.get(position);

			holder.mImage.setImageResource(R.drawable.list);
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

			holder.mStaticticShoping.setText(statisticsShopping);

			holder.mEdit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});

			holder.mDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
		}

		@Override
		public void onViewAttachedToWindow(final ViewHolder holder) {
			super.onViewAttachedToWindow(holder);

			holder.mImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					int imageWidth = holder.mImage.getWidth();
					if (imageWidth > 0) {
						holder.mImage.getLayoutParams().height = imageWidth * 9 / 16;
					}

					return true;
				}
			});
		}

		@Override
		public int getItemCount() {
			return mLists.size();
		}

		public static class ViewHolder extends RecyclerView.ViewHolder {
			public ImageView mImage;
			public TextView mName;
			public TextView mStaticticShoping;
			public ImageButton mEdit;
			public ImageButton mDelete;

			public ViewHolder(View v) {
				super(v);
				mImage = (ImageView) v.findViewById(R.id.image_list);
				mName = (TextView) v.findViewById(R.id.name_list);
				mStaticticShoping = (TextView) v.findViewById(R.id.statistics_shopping);
				mEdit = (ImageButton) v.findViewById(R.id.edit_list);
				mDelete = (ImageButton) v.findViewById(R.id.delete_list);
			}
		}
	}

	private static class ListsCursorLoader extends CursorLoader {
		private Context mContext;

		public ListsCursorLoader(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public Cursor loadInBackground() {
			ListsDataSource mListsDS = new ListsDataSource(mContext);

			return mListsDS.getAllWithStatictic();
		}
	}
}
