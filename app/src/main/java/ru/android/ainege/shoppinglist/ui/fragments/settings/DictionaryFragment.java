package ru.android.ainege.shoppinglist.ui.fragments.settings;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.DictionaryDataSource;
import ru.android.ainege.shoppinglist.db.entities.Dictionary;

public abstract class DictionaryFragment<T extends Dictionary> extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final int ADD_FRAGMENT_CODE = 1;
	public static final int EDIT_FRAGMENT_CODE = 2;
	protected static final String ADD_FRAGMENT_DATE = "addItemDialog";
	protected static final String EDIT_FRAGMENT_DATE = "editItemDialog";
	protected static final int DATA_LOADER = 0;
	protected ArrayList<T> mDictionary = new ArrayList<>();
	protected RecyclerViewAdapter mAdapterRV;

	protected abstract View.OnClickListener getAddHandler();

	protected abstract DictionaryDataSource getDS();

	protected abstract RecyclerViewAdapter getAdapter();

	public abstract void onLoadFinished(Loader<Cursor> loader, Cursor data);

	protected abstract void showEditDialog(int position);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_settings_data, container, false);

		Button add = (Button) v.findViewById(R.id.add);
		add.setOnClickListener(getAddHandler());

		RecyclerView mDictionaryRV = (RecyclerView) v.findViewById(R.id.list);
		mDictionaryRV.setLayoutManager(new LinearLayoutManager(getActivity()));

		mAdapterRV = getAdapter();
		mDictionaryRV.setAdapter(mAdapterRV);

		return v;
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
			case ADD_FRAGMENT_CODE:
				updateData();
				break;
			case EDIT_FRAGMENT_CODE:
				updateData();
				break;
		}
	}

	private static class DataCursorLoader extends CursorLoader {
		private DictionaryDataSource mDS;

		public DataCursorLoader(Context context, DictionaryDataSource ds) {
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
			public TextView mName;
			public ImageButton mEdit;
			public ImageButton mDelete;

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
						int itemPosition = getAdapterPosition();
						T d = mDictionary.get(itemPosition);

						if (mDictionary.size() > 1) {
							getDS().delete(d.getId());
							removeItem(itemPosition);
						} else {
							Toast.makeText(getActivity(), getString(R.string.error_one_item), Toast.LENGTH_SHORT).show();
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
