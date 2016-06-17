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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
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
	private static final int HANDLER_LOAD_FINISHED = 1;

	private OnListsChangeListener mListsChangeListener;
	private OnListsLoadFinishedListener mOnListsLoadFinishedListener;

	private ArrayList<List> mLists;
	private ArrayList<List> mSaveListRotate;
	private ListsDS mListsDS;
	private int mPositionForDelete;
	private boolean mIsUpdateData = false;
	private boolean mIsLandscapeTablet;
	private long mAddIdList = -1;

	private RecyclerView mListsRV;
	private ListsAdapter mAdapterRV;
	private ImageButton mAddButton;
	private ProgressBar mProgressBar;
	private ImageView mEmptyImage;

	private Handler mHandler = new Handler()  {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == HANDLER_LOAD_FINISHED && mOnListsLoadFinishedListener != null) {
				mOnListsLoadFinishedListener.onLoadFinished(mLists);
			}
		}
	};


	public interface OnListsChangeListener {
		void onListSelect(long id);
		void onListUpdate(long id);
		void onListDelete(long idDeletedList, long idNewList);
		long getLastSelectedListId();
		void onShowCaseShown();
		void onOpenDialog(long id);
		void onCloseDialog();
	}

	public interface OnListsLoadFinishedListener {
		void onLoadFinished(ArrayList<List> lists);
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

		mListsDS = new ListsDS(getActivity());
		mAdapterRV = new ListsAdapter();

		mIsLandscapeTablet = getResources().getBoolean(R.bool.isTablet) && getResources().getBoolean(R.bool.isLandscape);

		if (savedInstanceState != null) {
			mSaveListRotate = (ArrayList<List>) savedInstanceState.getSerializable(STATE_LISTS);
		}

		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_lists, container, false);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.list_of_lists_menu);
		toolbar.setOnMenuItemClickListener(onMenuItemClickListener());
		toolbar.setTitle(R.string.you_lists);

		mAddButton = (FloatingActionButton) v.findViewById(R.id.add_fab);

		if (mIsLandscapeTablet) {
			mAddButton.setVisibility(View.GONE);

			mAddButton = (ImageButton) toolbar.getMenu().findItem(R.id.add_list).getActionView();
			mAddButton.setImageResource(R.drawable.ic_add_white_24dp);
			mAddButton.setBackgroundColor(Color.TRANSPARENT);
		}

		mAddButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ListDialogFragment addListDialog = new ListDialogFragment();
				addListDialog.setTargetFragment(ListsFragment.this, ADD_LIST);
				addListDialog.show(getFragmentManager(), ADD_LIST_DATE);
				mListsChangeListener.onOpenDialog(-1);
			}
		});



		mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
		mEmptyImage = (ImageView) v.findViewById(R.id.empty_lists);

		mListsRV = (RecyclerView) v.findViewById(R.id.lists);
		mListsRV.setAdapter(mAdapterRV);
		LinearLayoutManager llm = new LinearLayoutManager(getActivity());
		llm.setAutoMeasureEnabled(false);
		mListsRV.setLayoutManager(llm);

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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_LIST || requestCode == EDIT_LIST || requestCode == IS_DELETE_LIST) {
			mListsChangeListener.onCloseDialog();
		}
		if (resultCode != Activity.RESULT_OK) return;

		switch (requestCode) {
			case ADD_LIST:
				updateData();
				mIsUpdateData = true;
				mAddIdList = data.getLongExtra(ListDialogFragment.ID_LIST, -1);

				if (mIsLandscapeTablet) {
					mListsChangeListener.onListSelect(mAddIdList);
				}
				break;
			case EDIT_LIST:
				updateData();
				mListsChangeListener.onListUpdate(data.getLongExtra(ListDialogFragment.ID_LIST, -1));
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

				mListsChangeListener.onListDelete(list.getId(), mLists.size() > 0 ? mLists.get(0).getId() : -1);
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

				if (mSaveListRotate == null && data.moveToFirst()) {
					setLists(((ListCursor) data).getEntities());
				} else if (mSaveListRotate != null && mSaveListRotate.size() > 0) {
					setLists(mSaveListRotate);
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

	public void scrollToList(long id) {
		if (id != -1) {
			mListsRV.scrollToPosition(getPosition(id));
		}
	}

	public void updateData() {
		mSaveListRotate = null;
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();
	}

	public ArrayList<List> getLists() {
		return mLists;
	}

	public void setOnListsLoadListener(OnListsLoadFinishedListener listener) {
		mOnListsLoadFinishedListener = listener;
	}

	private Toolbar.OnMenuItemClickListener onMenuItemClickListener() {
		return new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
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
						return false;
				}
			}
		};
	}

	private void setLists(ArrayList<List> lists) {
		mLists = lists;
		mAdapterRV.setDate();
		hideEmptyStates();

		if (mIsLandscapeTablet) {
			scrollToList(mListsChangeListener.getLastSelectedListId());
		} else {
			scrollToList(mAddIdList);
		}

		if (mOnListsLoadFinishedListener != null) {
			mHandler.sendEmptyMessage(HANDLER_LOAD_FINISHED);
		}
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

	private int getPosition(long id) {
		int index = 1;

		for (int i = 0; i < mLists.size(); i++) {
			if (mLists.get(i).getId() == id) {
				index = i;
				break;
			}
		}

		return index;
	}

	//<editor-fold desc="work with showcases">
	private void showcaseView() {
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), String.valueOf(Showcase.SHOT_ADD_LIST));
		sequence.setConfig(new ShowcaseConfig());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), mAddButton,
				getString(R.string.showcase_create_list_desc)).build());
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

		sequence.start();
	}

	private void showCaseList(ListsAdapter.ViewHolder holder) {
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_LIST);
		sequence.setConfig(new ShowcaseConfig());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), holder.mButtonsContainer,
				getString(R.string.showcase_manage_list_desc)).build());

		sequence.addSequenceItem(Showcase.createShowcase(getActivity(), holder.itemView,
				getString(R.string.showcase_open_list_desc))
				.withRectangleShape()
				.build());

		sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
			@Override
			public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
				if (i == 1) {
					mListsChangeListener.onShowCaseShown();
				}
			}
		});

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

		public void setDate() {
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

			if (!(new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_LIST).hasFired()) &&
					new MaterialShowcaseSequence(getActivity(), Showcase.SHOT_ADD_LIST).hasFired()) {
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
						mListsChangeListener.onOpenDialog(list.getId());
					}
				});

				mDelete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mPositionForDelete = getAdapterPosition();
						List list = mLists.get(mPositionForDelete);

						QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_delete_list) + " \"" + list.getName() + "\"?");
						dialogFrag.setTargetFragment(ListsFragment.this, IS_DELETE_LIST);
						dialogFrag.show(getFragmentManager(), IS_DELETE_LIST_DATE);
						mListsChangeListener.onOpenDialog(list.getId());
					}
				});

				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mIsUpdateData = true;
						int itemPosition = getAdapterPosition();

						if (itemPosition != RecyclerView.NO_POSITION) {
							List list = mLists.get(itemPosition);
							mListsChangeListener.onListSelect(list.getId());
						}
					}
				});
			}
		}
	}
}
