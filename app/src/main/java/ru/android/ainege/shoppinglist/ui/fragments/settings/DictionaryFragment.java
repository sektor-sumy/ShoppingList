package ru.android.ainege.shoppinglist.ui.fragments.settings;

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
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.DictionaryDS;
import ru.android.ainege.shoppinglist.db.entities.Dictionary;

public abstract class DictionaryFragment<T extends Dictionary> extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String LAST_EDIT = "lastEdit";
	protected static final int ADD = 1;
	protected static final int EDIT = 2;
	protected static final String ADD_DATE = "addItemDialog";
	protected static final String EDIT_DATE = "editItemDialog";
	protected static final int DATA_LOADER = 0;
	private static final int DELETE = 3;
	private static final String DELETE_DATE = "answerListDialog";

	protected ArrayList<T> mDictionary = new ArrayList<>();
	protected RecyclerViewAdapter mAdapterRV;
	protected RecyclerView mDictionaryRV;

	protected long mLastEditId = -1;

	protected abstract String getTitle();

	protected abstract View.OnClickListener getAddHandler();

	protected abstract DictionaryDS getDS();

	protected abstract RecyclerViewAdapter getAdapter();

	protected abstract boolean isEntityUsed(long id);

	public abstract void onLoadFinished(Loader<Cursor> loader, Cursor data);

	protected abstract void showEditDialog(int position);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
		} else {
			getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		}

		setHasOptionsMenu(true);

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dictionary, container, false);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.main_toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			Toolbar cardToolbar = (Toolbar) v.findViewById(R.id.toolbar);
			cardToolbar.setTitle(getTitle());
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
			if (actionBar != null) {
				actionBar.setTitle(getTitle());
			}
		}

		FloatingActionButton mFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		mFAB.setOnClickListener(getAddHandler());

		mDictionaryRV = (RecyclerView) v.findViewById(R.id.list);
		mDictionaryRV.setLayoutManager(new LinearLayoutManager(getActivity()));

		mAdapterRV = getAdapter();
		mDictionaryRV.setAdapter(mAdapterRV);

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra(LAST_EDIT, mLastEditId));
				getActivity().finish();
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
				Dictionary dictionary = (Dictionary) data.getSerializableExtra(DeleteDialogFragment.REPLACEMENT);
				deleteItem(data.getIntExtra(DeleteDialogFragment.POSITION, -1), dictionary.getId());
				break;
		}
	}

	private void deleteItem(int position, long newId) {
		if (position != -1) {
			T d = mDictionary.get(position);

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

		for (T t : mDictionary) {
			if (t.getId() == id) {
				position = mDictionary.indexOf(t);
			}
		}

		return position;
	}

	private static class DataCursorLoader extends CursorLoader {
		private final DictionaryDS mDS;

		public DataCursorLoader(Context context, DictionaryDS ds) {
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
			holder.mName.setText(mDictionary.get(position).toString());
		}

		@Override
		public int getItemCount() {
			return mDictionary.size();
		}

		public void removeItem(int position) {
			mDictionary.remove(position);
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
						if (mDictionary.size() == 1) {
							Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_one_item), Toast.LENGTH_SHORT).show();
						} else {
							int itemPosition = getAdapterPosition();
							T d = mDictionary.get(itemPosition);

							if (isEntityUsed(d.getId())) {
								DeleteDialogFragment dialogFrag = DeleteDialogFragment.newInstance(d, itemPosition);
								dialogFrag.setTargetFragment(DictionaryFragment.this, DELETE);
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
