package ru.android.ainege.shoppinglist.ui.fragments;

import android.annotation.TargetApi;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.ui.activities.SettingsActivity;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.android.ainege.shoppinglist.db.dataSources.ListsDS.ListCursor;

public class ListsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String APP_PREFERENCES = "shopping_list_settings";
	private static final String APP_PREFERENCES_ID = "idList";
	private static final String STATE_LISTS = "state_lists";

	private static final int ADD_LIST = 1;
	private static final int EDIT_LIST = 2;
	private static final int IS_DELETE_LIST = 3;
	private static final String ADD_LIST_DATE = "addListDialog";
	private static final String EDIT_LIST_DATE = "editListDialog";
	private static final String IS_DELETE_LIST_DATE = "answerListDialog";
	private static final int DATA_LOADER = 0;

	private OnListsChangeListener mListsChangeListener;

	private ArrayList<List> mLists;
	private ArrayList<List> mSaveListRotate;
	private ListsDS mListsDS;
	private int mPositionForDelete;
	private boolean mIsUpdateData = false;

	private RecyclerView mListsRV;
	private ListsAdapter mAdapterRV;
	private FloatingActionButton mAddItemFAB;
	private ProgressBar mProgressBar;
	private ImageView mEmptyImage;

	public interface OnListsChangeListener {
		void onListSelected(long id);
	}

	@TargetApi(23)
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mListsChangeListener = (OnListsChangeListener) getActivity();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			mListsChangeListener = (OnListsChangeListener) getActivity();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mListsDS = new ListsDS(getActivity());
		mAdapterRV = new ListsAdapter();

		if (savedInstanceState != null) {
			mSaveListRotate = (ArrayList<List>) savedInstanceState.getSerializable(STATE_LISTS);
		}

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_lists, container, false);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(R.string.you_lists);
		}

		mAddItemFAB = (FloatingActionButton) v.findViewById(R.id.add_fab);
		mAddItemFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ListDialogFragment addListDialog = new ListDialogFragment();
				addListDialog.setTargetFragment(ListsFragment.this, ADD_LIST);
				addListDialog.show(getFragmentManager(), ADD_LIST_DATE);
			}
		});

		mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
		mEmptyImage = (ImageView) v.findViewById(R.id.empty_lists);

		mListsRV = (RecyclerView) v.findViewById(R.id.lists);
		mListsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
		mListsRV.setAdapter(mAdapterRV);

		showcaseView();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mIsUpdateData) {
			updateData();
			mIsUpdateData = false;
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListsChangeListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_LISTS, mLists);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) return;

		switch (requestCode) {
			case ADD_LIST:
			case EDIT_LIST:
				updateData();
				break;
			case IS_DELETE_LIST:
				List list = mLists.get(mPositionForDelete);

				mListsDS.delete(list.getId());
				mAdapterRV.removeItem(mPositionForDelete);
				Image.deleteFile(list.getImagePath());

				if (mLists.size() > 0) {
					hideEmptyStates();
				} else {
					showEmptyStates();
				}

				deleteSaveListFromSettings(list.getId());
				break;
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

				if (mSaveListRotate != null && mSaveListRotate.size() > 0) {
					mLists = mSaveListRotate;
					mAdapterRV.setDate();
					hideEmptyStates();
				} else if (mSaveListRotate == null && data.moveToFirst()) {
					mLists = ((ListCursor) data).getEntities();
					mAdapterRV.setDate();
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

	}

	public void updateData() {
		mSaveListRotate = null;
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();
	}

	private void showEmptyStates() {
		mListsRV.setVisibility(View.GONE);
		mEmptyImage.setVisibility(View.VISIBLE);
	}

	private void hideEmptyStates() {
		mListsRV.setVisibility(View.VISIBLE);
		mEmptyImage.setVisibility(View.GONE);
	}

	private void deleteSaveListFromSettings(long id) {
		SharedPreferences mSettings = getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

		if (id == mSettings.getLong(APP_PREFERENCES_ID, -1)) {
			SharedPreferences.Editor editor = mSettings.edit();
			editor.remove(APP_PREFERENCES_ID);
			editor.apply();
		}
	}

	//<editor-fold desc="work with showcases">
	private void showcaseView() {
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), String.valueOf(Showcase.SHOT_ADD_LIST));
		sequence.setConfig(new ShowcaseConfig());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), mAddItemFAB,
				getString(R.string.showcase_create_list_desc)).build());

		sequence.start();

		sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
			@Override
			public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
				if (i == 0) {
					ListsAdapter.ViewHolder holder = (ListsAdapter.ViewHolder) mListsRV.findViewHolderForLayoutPosition(0);

					if (holder != null) {
						showCaseList(holder);
					}
				}
			}
		});
	}

	private void showCaseList(ListsAdapter.ViewHolder holder) {
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_LIST);
		sequence.setConfig(new ShowcaseConfig());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), holder.mButtonsContainer,
				getString(R.string.showcase_manage_list_desc)).build());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), holder.mImage,
				getString(R.string.showcase_open_list_desc)).build());

		sequence.start();
	}
	//</editor-fold>

	private static class ListsCursorLoader extends CursorLoader {
		private final Context mContext;

		public ListsCursorLoader(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public Cursor loadInBackground() {
			return new ListsDS(mContext).getAll();
		}
	}

	public class ListsAdapter extends RecyclerView.Adapter<ListsAdapter.ViewHolder> {
		private boolean mIsShowShowcase = false;

		public void setDate() {
			if (new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_ADD_LIST).hasFired()) {
				mIsShowShowcase = true;
			}
			notifyDataSetChanged();
		}

		@Override
		public ListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_lists_item, parent, false);
			return new ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			List list = mLists.get(position);

			Image.create().insertImageToView(getActivity(), list.getImagePath(), holder.mImage);
			holder.mName.setText(list.getName());
			holder.mStatisticsShopping.setText(getStatisticsShopping(list));
		}

		@Override
		public int getItemCount() {
			return mLists != null ? mLists.size() : 0;
		}

		@Override
		public void onViewAttachedToWindow(final ViewHolder holder) {
			super.onViewAttachedToWindow(holder);

			if (mIsShowShowcase) {
				mIsShowShowcase = false;
				holder.itemView.post(new Runnable() {
					@Override
					public void run() {
						showCaseList(holder);
					}
				});
			}
		}

		public void removeItem(int position) {
			mLists.remove(position);
			notifyItemRemoved(position);
		}

		private String getStatisticsShopping(List list) {
			int amountItems = list.getAmountItems();
			String statisticsShopping;

			if (amountItems == 0) {
				statisticsShopping = getString(R.string.list_empty);
			} else {
				int amountBoughtItems = list.getAmountBoughtItems();

				if (amountBoughtItems == 0) {
					statisticsShopping = getString(R.string.nothing_bought) + amountItems;
				} else {
					if (amountItems == amountBoughtItems) {
						statisticsShopping = getString(R.string.all_bought);
					} else {
						statisticsShopping = getString(R.string.bought) + amountBoughtItems + getString(R.string.of) + amountItems;
					}
				}
			}

			return statisticsShopping;
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public ImageView mImage;
			public TextView mName;
			public TextView mStatisticsShopping;
			public LinearLayout mButtonsContainer;
			public ImageButton mEdit;
			public ImageButton mDelete;

			public ViewHolder(View v) {
				super(v);
				mImage = (ImageView) v.findViewById(R.id.image_list);
				mName = (TextView) v.findViewById(R.id.name_list);
				mStatisticsShopping = (TextView) v.findViewById(R.id.statistics_shopping);
				mButtonsContainer = (LinearLayout) v.findViewById(R.id.buttons_container);
				mEdit = (ImageButton) v.findViewById(R.id.edit_list);
				mDelete = (ImageButton) v.findViewById(R.id.delete_list);

				mEdit.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						List list = mLists.get(itemPosition);

						ListDialogFragment editListDialog = ListDialogFragment.newInstance(list);
						editListDialog.setTargetFragment(ListsFragment.this, EDIT_LIST);
						editListDialog.show(getFragmentManager(), EDIT_LIST_DATE);
					}
				});

				mDelete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPositionForDelete = getAdapterPosition();

						QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_delete_list));
						dialogFrag.setTargetFragment(ListsFragment.this, IS_DELETE_LIST);
						dialogFrag.show(getFragmentManager(), IS_DELETE_LIST_DATE);
					}
				});

				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mIsUpdateData = true;
						int itemPosition = getAdapterPosition();

						if (itemPosition != RecyclerView.NO_POSITION) {
							List list = mLists.get(itemPosition);
							mListsChangeListener.onListSelected(list.getId());
						}
					}
				});
			}
		}
	}
}
