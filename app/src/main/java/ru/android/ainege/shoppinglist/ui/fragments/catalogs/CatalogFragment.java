package ru.android.ainege.shoppinglist.ui.fragments.catalogs;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CatalogDS;
import ru.android.ainege.shoppinglist.db.entities.Catalog;
import ru.android.ainege.shoppinglist.ui.OnBackPressedListener;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.DeleteDialogFragment;
import ru.android.ainege.shoppinglist.ui.fragments.catalogs.dialog.GeneralDialogFragment;

public abstract class CatalogFragment<T extends Catalog> extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnBackPressedListener {
	public static final String LAST_EDIT = "lastEdit";
	protected static final int ADD = 1;
	protected static final int EDIT = 2;
	protected static final String ADD_DATE = "addItemDialog";
	protected static final String EDIT_DATE = "editItemDialog";
	protected static final int DATA_LOADER = 0;
	private static final int DELETE = 3;
	private static final String DELETE_DATE = "answerListDialog";
	private static final String STATE_LIST = "state_list";
	private static final String STATE_LAST_EDIT_ID = "state_last_edit_id";

	protected ArrayList<T> mCatalog;
	protected ArrayList<T> mSaveListRotate;
	protected RecyclerViewAdapter mAdapterRV;
	protected RecyclerView mCatalogRV;

	protected long mLastEditId = -1;

	protected abstract String getTitle();

	protected abstract View.OnClickListener getAddHandler();

	protected abstract CatalogDS getDS();

	protected abstract RecyclerViewAdapter getAdapter();

	protected abstract boolean isEntityUsed(long id);

	public abstract void onLoadFinished(Loader<Cursor> loader, Cursor data);

	protected abstract void showEditDialog(int position);

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
			mSaveListRotate = (ArrayList<T>) savedInstanceState.getSerializable(STATE_LIST);
			mLastEditId = savedInstanceState.getLong(STATE_LAST_EDIT_ID);
		}

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_catalog, container, false);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.main_toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		int orientation = getResources().getConfiguration().orientation;
		boolean isTablet = getResources().getBoolean(R.bool.isTablet);

		if (isTablet || (!isTablet && orientation == Configuration.ORIENTATION_LANDSCAPE)) {
			Toolbar cardToolbar = (Toolbar) v.findViewById(R.id.toolbar);
			cardToolbar.setTitle(getTitle());
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
			if (actionBar != null) {
				actionBar.setTitle(getTitle());
			}
		}

		FloatingActionButton mFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		mFAB.setOnClickListener(getAddHandler());

		mCatalogRV = (RecyclerView) v.findViewById(R.id.list);
		mCatalogRV.setLayoutManager(new LinearLayoutManager(getActivity()));
		mCatalogRV.setAdapter(mAdapterRV);

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_LIST, mCatalog);
		outState.putLong(STATE_LAST_EDIT_ID, mLastEditId);
	}

	@Override
	public boolean onBackPressed() {
		getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra(LAST_EDIT, mLastEditId));
		return true;
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
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	private void updateData() {
		mSaveListRotate = null;
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();
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
				Catalog catalog = (Catalog) data.getSerializableExtra(DeleteDialogFragment.REPLACEMENT);
				deleteItem(data.getIntExtra(DeleteDialogFragment.POSITION, -1), catalog.getId());

				mLastEditId = catalog.getId();
				break;
		}
	}

	private void deleteItem(int position, long newId) {
		if (position != -1) {
			T d = mCatalog.get(position);

			if (newId != -1) {
				getDS().delete(d.getId(), newId);
			} else {
				getDS().delete(d.getId());
			}

			mAdapterRV.removeItem(position);
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

	public abstract class RecyclerViewAdapter<S extends RecyclerViewAdapter.ViewHolder> extends RecyclerView.Adapter<S> {

		@Override
		public void onBindViewHolder(S holder, int position) {
			holder.mName.setText(mCatalog.get(position).toString());
		}

		@Override
		public int getItemCount() {
			return mCatalog != null ? mCatalog.size() : 0;
		}

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
						showEditDialog(getAdapterPosition());
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
								DeleteDialogFragment dialogFrag = DeleteDialogFragment.newInstance(d, itemPosition);
								dialogFrag.setTargetFragment(CatalogFragment.this, DELETE);
								dialogFrag.show(getFragmentManager(), DELETE_DATE);
							} else {
								deleteItem(itemPosition, -1);
							}
						}
					}
				});

				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showEditDialog(getAdapterPosition());
					}
				});
			}
		}
	}
}
