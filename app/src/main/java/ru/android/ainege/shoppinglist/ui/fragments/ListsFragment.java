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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.ui.Image;
import ru.android.ainege.shoppinglist.ui.activities.SettingsActivity;
import ru.android.ainege.shoppinglist.ui.activities.ShoppingListActivity;

import static ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource.ListCursor;

public class ListsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String APP_PREFERENCES = "shopping_list_settings";
	private static final String APP_PREFERENCES_ID = "idList";

	private static final int ADD_FRAGMENT_CODE = 1;
	private static final int EDIT_FRAGMENT_CODE = 2;
	private static final int ANSWER_FRAGMENT_CODE = 3;
	private static final int DATA_LOADER = 0;
	private static final String ADD_FRAGMENT_DATE = "addListDialog";
	private static final String EDIT_FRAGMENT_DATE = "editListDialog";
	private static final String ANSWER_FRAGMENT_DATE = "answerListDialog";
	ArrayList<List> mLists = new ArrayList<>();
	private int mPositionForDelete;
	private ListsDataSource mListsDS;
	private RecyclerView mListsRV;
	private ImageView mEmptyImage;
	private RecyclerViewAdapter mAdapterRV;
	private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		mListsDS = new ListsDataSource(getActivity());
		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_lists, container, false);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(R.string.you_lists);
		}

		FloatingActionButton addItemFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		addItemFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ListDialogFragment addListDialog = new ListDialogFragment();
				addListDialog.setTargetFragment(ListsFragment.this, ADD_FRAGMENT_CODE);
				addListDialog.show(getFragmentManager(), ADD_FRAGMENT_DATE);
			}
		});

		mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);

		mListsRV = (RecyclerView) v.findViewById(R.id.lists);
		mListsRV.setLayoutManager(new LinearLayoutManager(getActivity()));

		mEmptyImage = (ImageView) v.findViewById(R.id.empty_lists);

		mAdapterRV = new RecyclerViewAdapter();
		mListsRV.setAdapter(mAdapterRV);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		mAdapterRV.notifyItemChanged(0); //update adapter or crash app
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
				if (mProgressBar.getVisibility() == View.VISIBLE) {
					mProgressBar.setVisibility(View.GONE);
				}

				if (data.moveToFirst()) {
					mLists = ((ListCursor) data).getEntities();
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
			case ANSWER_FRAGMENT_CODE:
				List list = mLists.get(mPositionForDelete);

				mListsDS.delete(list.getId());
				mAdapterRV.removeItem(mPositionForDelete);

				Image.deleteFile(list.getImagePath());

				if (mLists.size() > 0) {
					hideEmptyStates();
				} else {
					showEmptyStates();
				}

				deleteSettings();
				break;
		}
	}

	private void deleteSettings() {
		SharedPreferences mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mSettings.edit();
		editor.remove(APP_PREFERENCES_ID);
		editor.apply();
	}

	private void showEmptyStates() {
		mListsRV.setVisibility(View.GONE);
		mEmptyImage.setVisibility(View.VISIBLE);
	}

	private void hideEmptyStates() {
		mListsRV.setVisibility(View.VISIBLE);
		mEmptyImage.setVisibility(View.GONE);
	}

	private static class ListsCursorLoader extends CursorLoader {
		private final Context mContext;

		public ListsCursorLoader(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public Cursor loadInBackground() {
			return new ListsDataSource(mContext).getAll();
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

			Image.create().insertImageToView(getActivity(), list.getImagePath(), holder.mImage);

			holder.mName.setText(list.getName());

			int amountItem = list.getAmountItems();
			String statisticsShopping;

			if (amountItem == 0) {
				statisticsShopping = getString(R.string.list_empty);
			} else {
				int amountBoughtItems = list.getAmountBoughtItems();

				if (amountBoughtItems == 0) {
					statisticsShopping = getString(R.string.nothing_bought) + amountItem;
				} else {
					if (amountItem == amountBoughtItems) {
						statisticsShopping = getString(R.string.all_bought);
					} else {
						statisticsShopping = getString(R.string.bought) + amountBoughtItems + getString(R.string.of) + amountItem;
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
			public final View mView;
			public final ImageView mImage;
			public final TextView mName;
			public final TextView mStatisticsShopping;
			public final ImageButton mEdit;
			public final ImageButton mDelete;

			public ViewHolder(View v) {
				super(v);
				mView = v;
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
						mPositionForDelete = getAdapterPosition();

						QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_delete_list));
						dialogFrag.setTargetFragment(ListsFragment.this, ANSWER_FRAGMENT_CODE);
						dialogFrag.show(getFragmentManager(), ANSWER_FRAGMENT_DATE);
					}
				});
				mView.setOnClickListener(new View.OnClickListener() {
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
