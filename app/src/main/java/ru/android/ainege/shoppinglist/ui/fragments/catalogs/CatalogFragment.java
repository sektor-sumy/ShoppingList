package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.entities.Catalog;
import ru.android.ainege.shoppinglist.ui.fragments.OnCreateViewListener;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.DeleteDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;

public abstract class CatalogFragment<T extends Catalog> extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	protected static final int ADD = 1;
	protected static final int EDIT = 2;
	protected static final String ADD_DATE = "addItemDialog";
	protected static final String EDIT_DATE = "editItemDialog";
	protected static final int DATA_LOADER = 0;
	protected static final int DELETE = 3;
	protected static final String DELETE_DATE = "answerListDialog";
	private static final String STATE_SCROLL_POSITION = "state_scroll_position";
	private static final String STATE_LAST_EDIT_ID = "state_last_edit_id";

	private OnCreateViewListener mOnCreateViewListener;

	protected ArrayList<T> mCatalog;
	protected CatalogAdapter mAdapterRV;
	protected RecyclerView mCatalogRV;
	protected ImageView mEmptyImage;

	protected HashMap<Integer, Long> mLastEditIds;
	protected long mLastEditId = -1;
	protected int mScrollToPosition = -1;

	public abstract int getKey();
	protected abstract String getTitle(Toolbar toolbar);
	protected abstract CatalogAdapter getAdapter();
	protected abstract CatalogDS getDS();
	protected abstract GeneralDialogFragment getAddDialog();
	protected abstract GeneralDialogFragment getEditDialog();
	protected abstract ArrayList getCatalog(Cursor data);

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

		mAdapterRV = getAdapter();

		if (savedInstanceState != null) {
			mLastEditId = savedInstanceState.getLong(STATE_LAST_EDIT_ID);
			mScrollToPosition = savedInstanceState.getInt(STATE_SCROLL_POSITION);
		}

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_catalog, container, false);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.main_toolbar);

		boolean isLandscape = getResources().getBoolean(R.bool.isLandscape);
		boolean isTablet = getResources().getBoolean(R.bool.isTablet);

		if (isTablet || (!isTablet && isLandscape)) {
			Toolbar cardToolbar = (Toolbar) v.findViewById(R.id.toolbar);
			cardToolbar.setTitle(getTitle(cardToolbar));
		} else if (!isLandscape) {
			toolbar.setTitle(getTitle(toolbar));
		}

		FloatingActionButton mFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				GeneralDialogFragment addItemDialog = getAddDialog();
				addItemDialog.setTargetFragment(CatalogFragment.this, ADD);
				addItemDialog.show(getFragmentManager(), ADD_DATE);
			}
		});

		mCatalogRV = (RecyclerView) v.findViewById(R.id.list);
		mCatalogRV.setLayoutManager(new LinearLayoutManager(getActivity()));
		mCatalogRV.setAdapter((RecyclerView.Adapter) mAdapterRV);

		mEmptyImage = (ImageView) v.findViewById(R.id.empty_list);
		mOnCreateViewListener.onCreateViewListener(this, toolbar);

		return v;
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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		int firstVisiblePosition = ((LinearLayoutManager) mCatalogRV.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
		outState.putInt(STATE_SCROLL_POSITION, firstVisiblePosition);
		outState.putLong(STATE_LAST_EDIT_ID, mLastEditId);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Loader<Cursor> loader = null;
		switch (id) {
			case DATA_LOADER:
				loader = new DataCursorLoader(getActivity(), getDS());
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
					mCatalog = getCatalog(data);
					loadData();

					mCatalogRV.setVisibility(View.VISIBLE);
					mEmptyImage.setVisibility(View.GONE);
				} else {
					mCatalogRV.setVisibility(View.GONE);
					mEmptyImage.setVisibility(View.VISIBLE);
				}

				break;
			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	protected void loadData() {
		mAdapterRV.setData(mCatalog, true);

		if (mScrollToPosition != -1) {
			mCatalogRV.scrollToPosition(mScrollToPosition);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) return;

		switch (requestCode) {
			case ADD:
				mLastEditId = data.getLongExtra(GeneralDialogFragment.ID_ITEM, -1);
				updateData();
				break;
			case EDIT:
				mLastEditId = data.getLongExtra(GeneralDialogFragment.ID_ITEM, -1);
				updateData();
				break;
			case DELETE:
				int position = data.getIntExtra(DeleteDialogFragment.POSITION, -1);
				long oldId = data.getLongExtra(DeleteDialogFragment.OLD_ID, -1);
				long newId = data.getLongExtra(DeleteDialogFragment.NEW_ID, -1);
				deleteItem(position, oldId, newId);

				if (newId != -1) {
					mLastEditId = newId;
				}
				break;
		}
	}

	public HashMap<Integer, Long> getLastEditIds() {
		if (mLastEditId != -1) {
			if (mLastEditIds == null) {
				mLastEditIds = new HashMap<>();
			}

			updateLastEditIds();
		}

		return mLastEditIds;
	}

	public void setLastEditIds(HashMap<Integer, Long> lastEditIds) {
		if (lastEditIds != null) {
			mLastEditIds = lastEditIds;
		}
	}

	protected int getPosition(long id) {
		int position = 0;

		for (T t : mCatalog) {
			if (t.getId() == id) {
				position = mCatalog.indexOf(t);
			}
		}

		return position;
	}

	protected boolean isEntityUsed(long id) {
		return getDS().isUsed(id);
	}

	protected void showEditDialog(T catalog) {
		GeneralDialogFragment editItemDialog = GeneralDialogFragment.newInstance(getEditDialog(), catalog);
		editItemDialog.setTargetFragment(CatalogFragment.this, EDIT);
		editItemDialog.show(getFragmentManager(), EDIT_DATE);
	}

	private void updateLastEditIds() {
		mLastEditIds.put(getKey(), mLastEditId);
	}

	private void updateData() {
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();
	}

	protected void deleteItem(int position, long oldId, long newId) {
		if (position != -1) {

			if (newId != -1) {
				getDS().delete(oldId, newId);
			} else {
				getDS().delete(oldId);
			}

			mAdapterRV.removeItem(position);
		}
	}

	interface CatalogAdapter {
		void setData(ArrayList catalog, boolean isUpdate);
		void removeItem(int position);
	}

	private static class DataCursorLoader extends CursorLoader {
		private final CatalogDS mDS;

		public DataCursorLoader(Context context, CatalogDS ds) {
			super(context);
			mDS = ds;
		}

		@Override
		public Cursor loadInBackground() {
			return mDS.getAll();
		}
	}

	public abstract class RecyclerViewAdapter<S extends RecyclerViewAdapter.ViewHolder> extends RecyclerView.Adapter<S>
														implements CatalogAdapter {
		protected ArrayList<T> mCatalog;

		public abstract S onCreateViewHolder(ViewGroup parent, int viewType);

		@Override
		public void setData(ArrayList catalog, boolean isUpdate) {
			mCatalog = catalog;
			notifyDataSetChanged();
		}

		@Override
		public void onBindViewHolder(S holder, int position) {
			holder.mName.setText(mCatalog.get(position).toString());
		}

		@Override
		public int getItemCount() {
			return mCatalog != null ? mCatalog.size() : 0;
		}

		@Override
		public void removeItem(int position) {
			mCatalog.remove(position);
			notifyItemRemoved(position);
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public final TextView mName;
			public final ImageButton mEdit;
			public final ImageButton mDelete;

			public ViewHolder(View v) {
				super(v);

				mName = (TextView) v.findViewById(R.id.name);
				mEdit = (ImageButton) v.findViewById(R.id.edit);
				mDelete = (ImageButton) v.findViewById(R.id.delete);

				mEdit.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showEditDialog(mCatalog.get(getAdapterPosition()));
					}
				});

				mDelete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mCatalog.size() == 1) {
							Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_one_item), Toast.LENGTH_SHORT).show();
						} else {
							int itemPosition = getAdapterPosition();
							T d = mCatalog.get(itemPosition);

							if (isEntityUsed(d.getId())) {
								DeleteDialogFragment dialogFrag = DeleteDialogFragment.newInstance(itemPosition, d, null);
								dialogFrag.setTargetFragment(CatalogFragment.this, DELETE);
								dialogFrag.show(getFragmentManager(), DELETE_DATE);
							} else {
								deleteItem(itemPosition, d.getId(), -1);
							}
						}
					}
				});

				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showEditDialog(mCatalog.get(getAdapterPosition()));
					}
				});
			}
		}
	}
}
