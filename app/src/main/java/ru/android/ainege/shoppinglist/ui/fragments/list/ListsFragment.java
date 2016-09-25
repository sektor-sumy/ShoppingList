package ru.android.ainege.shoppinglist.ui.fragments.list;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.ui.OnDialogShownListener;
import ru.android.ainege.shoppinglist.ui.fragments.OnCreateViewListener;
import ru.android.ainege.shoppinglist.ui.fragments.QuestionDialogFragment;
import ru.android.ainege.shoppinglist.util.Image;
import ru.android.ainege.shoppinglist.util.Showcase;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static ru.android.ainege.shoppinglist.db.dataSources.ListsDS.ListCursor;

public class ListsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String APP_PREFERENCES = "shopping_list_settings";
	private static final String APP_PREFERENCES_ID = "idList";
	private static final String STATE_IS_UPDATE_DATA = "state_is_update_data";
	private static final String STATE_SCROLL_POSITION = "state_scroll_position";

	private static final int ADD_LIST = 1;
	private static final int EDIT_LIST = 2;
	private static final int IS_DELETE_LIST = 3;
	private static final String ADD_LIST_DATE = "addListDialog";
	private static final String EDIT_LIST_DATE = "editListDialog";
	private static final String IS_DELETE_LIST_DATE = "answerListDialog";
	private static final int DATA_LOADER = 100;
	private static final int HANDLER_LOAD_FINISHED = 1;

	private OnCreateViewListener mOnCreateViewListener;
	private OnListSelectListener mOnListSelectListener;
	private OnListChangedListener mOnListChangedListener;
	private OnDialogShownListener mOnDialogShownListener;
	private OnListsLoadFinishedListener mOnListsLoadFinishedListener;

	private ArrayList<List> mLists = new ArrayList<>();
	private ListsDS mListsDS;
	private boolean mIsLandscapeTablet;
	private long mLastListId = -1;
	protected int mScrollToPosition = -1;

	private RecyclerView mListsRV;
	private ListsAdapter mAdapterRV;
	private ImageButton mAddButton;
	private ProgressBar mProgressBar;
	private ImageView mEmptyImage;

	private boolean mIsUpdateData = false;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()  {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == HANDLER_LOAD_FINISHED && mOnListsLoadFinishedListener != null) {
				mOnListsLoadFinishedListener.onLoadFinished(mLists);
			}
		}
	};

	public interface OnListSelectListener {
		void onListClick(long id);
	}

	public interface OnListChangedListener {
		void onListUpdated(long id);
		void onListDeleted(long idDeletedList, long idNewList);
		long getLastSelectedListId();
		void onShowCaseShown();
	}

	public interface OnListsLoadFinishedListener {
		void onLoadFinished(ArrayList<List> lists);
	}

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

		mListsDS = new ListsDS(getActivity());
		mAdapterRV = new ListsAdapter();

		mIsLandscapeTablet = getResources().getBoolean(R.bool.isTablet) && getResources().getBoolean(R.bool.isLandscape);

		if (savedInstanceState != null) {
			mIsUpdateData = savedInstanceState.getBoolean(STATE_IS_UPDATE_DATA);
			mScrollToPosition = savedInstanceState.getInt(STATE_SCROLL_POSITION);
		}

		// TODO: 28.08.2016 bug in support lib, not save loader after rotate
		getLoaderManager().initLoader(DATA_LOADER, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_lists, container, false);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.list_of_lists_menu);
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
				ListDialogFragment addListDialog = new AddListDialogFragment();
				addListDialog.setTargetFragment(ListsFragment.this, ADD_LIST);
				addListDialog.show(getFragmentManager(), ADD_LIST_DATE);

				if (mOnDialogShownListener != null) {
					mOnDialogShownListener.onOpenDialog(-1);
				}
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
		mOnCreateViewListener.onCreateViewListener(this, toolbar);

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

		mOnCreateViewListener = null;
		mOnListSelectListener = null;
		mOnListChangedListener = null;
		mOnDialogShownListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		int firstVisiblePosition = ((LinearLayoutManager) mListsRV.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
		outState.putInt(STATE_SCROLL_POSITION, firstVisiblePosition);
		outState.putBoolean(STATE_IS_UPDATE_DATA, mIsUpdateData);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mOnDialogShownListener != null &&
				(requestCode == ADD_LIST || requestCode == EDIT_LIST || requestCode == IS_DELETE_LIST)) {
			mOnDialogShownListener.onCloseDialog();
		}
		if (resultCode != Activity.RESULT_OK) return;

		switch (requestCode) {
			case ADD_LIST:
				updateData();
				mLastListId = data.getLongExtra(ListDialogFragment.ID_LIST, -1);

				if (mIsLandscapeTablet && mOnListSelectListener != null) {
					mOnListSelectListener.onListClick(mLastListId);
				}
				break;
			case EDIT_LIST:
				updateData();
				mLastListId = data.getLongExtra(ListDialogFragment.ID_LIST, -1);

				if (mOnListChangedListener != null) {
					mOnListChangedListener.onListUpdated(data.getLongExtra(ListDialogFragment.ID_LIST, -1));
				}
				break;
			case IS_DELETE_LIST:
				int position = getPosition(data.getLongExtra(QuestionDialogFragment.ID, -1));

				if (position != -1) {
					List list = mLists.get(position);
					mListsDS.delete(list.getId());
					mAdapterRV.removeItem(position);
					Image.deleteFile(list.getImagePath());

					if (mLists.size() > 0) {
						hideEmptyStates();
					} else {
						showEmptyStates();
					}

					if (mOnListChangedListener != null) {
						mOnListChangedListener.onListDeleted(list.getId(), mLists.size() > 0 ? mLists.get(0).getId() : -1);
					}

					deleteSaveListFromSettings(list.getId());
				} else{
					Toast.makeText(getActivity(), getString(R.string.error_delete), Toast.LENGTH_SHORT).show();
				}

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

				if (data.moveToFirst()) {
					setLists(((ListCursor) data).getEntities());
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

	public void setListeners(OnListSelectListener onListSelectListener, OnDialogShownListener onDialogShownListener) {
		setOnListSelectListener(onListSelectListener);
		setOnDialogShownListener(onDialogShownListener);
	}

	public void setOnListSelectListener(OnListSelectListener onListSelectListener) {
		mOnListSelectListener = onListSelectListener;
	}

	public void setOnListChangedListener(OnListChangedListener onListChangedListener) {
		mOnListChangedListener = onListChangedListener;
	}

	public void setOnDialogShownListener(OnDialogShownListener onDialogShownListener) {
		mOnDialogShownListener = onDialogShownListener;
	}

	public void scrollToList() {
		int position;

		if (mIsLandscapeTablet && mOnListChangedListener != null) {
			position = getPosition(mOnListChangedListener.getLastSelectedListId());
		} else if (mLastListId != -1){
			position = getPosition(mLastListId);
			mLastListId = -1;
		} else {
			position = mScrollToPosition;
		}

		if (position != -1) {
			mListsRV.scrollToPosition(position);
		}
	}

	public void scrollToList(long id) {
		if (id != -1) {
			mListsRV.scrollToPosition(getPosition(id));
		}
	}

	public void updateData() {
		getLoaderManager().getLoader(DATA_LOADER).forceLoad();
	}

	public ArrayList<List> getLists() {
		return mLists;
	}

	public void setOnListsLoadListener(OnListsLoadFinishedListener listener) {
		mOnListsLoadFinishedListener = listener;
	}

	private void setLists(ArrayList<List> lists) {
		mLists = lists;
		mAdapterRV.setDate();

		hideEmptyStates();
		scrollToList();

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

			if (mOnCreateViewListener != null) {
				mOnCreateViewListener.onDeleteSavedList();
			}
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
				if (i == 1 && mOnListChangedListener != null) {
					mOnListChangedListener.onShowCaseShown();
				}
			}
		});

		sequence.start();
	}
	//</editor-fold>

	private static class ListsCursorLoader extends CursorLoader {
		private final Context mContext;

		ListsCursorLoader(Context context) {
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
			return mLists.size();
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
			int amountBoughtItems = list.getAmountBoughtItems();
			String statisticsShopping;

			if (amountItems == 0) {
				statisticsShopping = getString(R.string.list_empty);
			} else if (amountBoughtItems == 0) {
				statisticsShopping = getString(R.string.nothing_bought) + amountItems;
			} else if (amountItems == amountBoughtItems) {
				statisticsShopping = getString(R.string.all_bought);
			} else {
				statisticsShopping = getString(R.string.bought) + amountBoughtItems + getString(R.string.of) + amountItems;
			}

			return statisticsShopping;
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			ImageView mImage;
			TextView mName;
			TextView mStatisticsShopping;
			LinearLayout mButtonsContainer;
			ImageButton mEdit;
			ImageButton mDelete;

			ViewHolder(View v) {
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

						ListDialogFragment editListDialog = EditListDialogFragment.newInstance(list);
						editListDialog.setTargetFragment(ListsFragment.this, EDIT_LIST);
						editListDialog.show(getFragmentManager(), EDIT_LIST_DATE);

						if (mOnDialogShownListener != null) {
							mOnDialogShownListener.onOpenDialog(list.getId());
						}
					}
				});

				mDelete.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int itemPosition = getAdapterPosition();
						List list = mLists.get(itemPosition);

						QuestionDialogFragment dialogFrag = QuestionDialogFragment.newInstance(getString(R.string.ask_delete_list) + " \"" + list.getName() + "\"?", list.getId());
						dialogFrag.setTargetFragment(ListsFragment.this, IS_DELETE_LIST);
						dialogFrag.show(getFragmentManager(), IS_DELETE_LIST_DATE);

						if (mOnDialogShownListener != null) {
							mOnDialogShownListener.onOpenDialog(list.getId());
						}
					}
				});

				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mIsUpdateData = true;
						int itemPosition = getAdapterPosition();

						if (itemPosition != RecyclerView.NO_POSITION) {
							List list = mLists.get(itemPosition);

							if (mOnListSelectListener != null) {
								mOnListSelectListener.onListClick(list.getId());
							}
						}
					}
				});
			}
		}
	}
}
