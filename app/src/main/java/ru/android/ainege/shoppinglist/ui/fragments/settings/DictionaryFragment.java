package ru.android.ainege.shoppinglist.ui.fragments.settings;

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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.DictionaryDataSource;
import ru.android.ainege.shoppinglist.db.entities.Dictionary;
import ru.android.ainege.shoppinglist.ui.fragments.QuestionDialogFragment;

public abstract class DictionaryFragment<T extends Dictionary> extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	static final int ADD_FRAGMENT_CODE = 1;
	static final int EDIT_FRAGMENT_CODE = 2;
	private static final int ANSWER_FRAGMENT_CODE = 3;
	static final String ADD_FRAGMENT_DATE = "addItemDialog";
	static final String EDIT_FRAGMENT_DATE = "editItemDialog";
	static final int DATA_LOADER = 0;
	private static final String ANSWER_FRAGMENT_DATE = "answerListDialog";
	ArrayList<T> mDictionary = new ArrayList<>();
	RecyclerViewAdapter mAdapterRV;
	RecyclerView mDictionaryRV;

	protected abstract String getTitle();

	protected abstract View.OnClickListener getAddHandler();

	protected abstract DictionaryDataSource getDS();

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
		ActionBar appBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

		if (appBar != null) {
			appBar.setTitle(getTitle());
			appBar.setHomeButtonEnabled(true);
			appBar.setDisplayHomeAsUpEnabled(true);
		}

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dictionary, container, false);

		Button add = (Button) v.findViewById(R.id.add);
		add.setOnClickListener(getAddHandler());

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
			case ADD_FRAGMENT_CODE:
				updateData();
				break;
			case EDIT_FRAGMENT_CODE:
				updateData();
				break;
			case ANSWER_FRAGMENT_CODE:
				deleteItem(data.getLongExtra(QuestionDialogFragment.ID, -1), data.getIntExtra(QuestionDialogFragment.POSITION, -1));
				break;
		}
	}

	private void deleteItem(long id, int position) {
		if (id != -1 && position != -1) {
			getDS().delete(id);
			mAdapterRV.removeItem(position);
		}
	}

	private static class DataCursorLoader extends CursorLoader {
		private final DictionaryDataSource mDS;

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
							Toast.makeText(getActivity(), getString(R.string.error_one_item), Toast.LENGTH_SHORT).show();
						} else {
							int itemPosition = getAdapterPosition();
							T d = mDictionary.get(itemPosition);

							if (isEntityUsed(d.getId())) {
								QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_delete_item), d.getId(), itemPosition);
								dialogFrag.setTargetFragment(DictionaryFragment.this, ANSWER_FRAGMENT_CODE);
								dialogFrag.show(getFragmentManager(), ANSWER_FRAGMENT_DATE);
							} else {
								deleteItem(d.getId(), itemPosition);
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
