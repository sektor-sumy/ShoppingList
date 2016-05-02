package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.ITable.ICurrencies;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.util.Image;

import static ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS.CurrencyCursor;

public class ListDialogFragment extends DialogFragment {
	private static final String ID_LIST = "idList";
	private static final String LIST = "list";
	private static final String RETAINED_FRAGMENT = "retained_fragment_list";
	private static final int TAKE_PHOTO = 0;
	private static final int LOAD_IMAGE = 1;
	private static final String STATE_FILE = "state_file";

	private ImageView mImageList;
	private TextInputLayout mNameInputLayout;
	private EditText mName;
	private Spinner mCurrency;

	private List mEditList;
	private File mFile;
	private String mImagePath;

	private RetainedFragment dataFragment;

	public static ListDialogFragment newInstance(List list) {
		Bundle args = new Bundle();
		args.putSerializable(LIST, list);

		ListDialogFragment fragment = new ListDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_list, null);

		mImageList = (ImageView) v.findViewById(R.id.image);
		registerForContextMenu(mImageList);
		mImageList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.showContextMenu();
			}
		});

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (EditText) v.findViewById(R.id.name);
		mCurrency = (Spinner) v.findViewById(R.id.currency);
		mCurrency.setAdapter(getSpinnerAdapter());
		mCurrency.setSelection(getPosition(mCurrency, getDefaultIdCurrency()));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(v)
				.setCancelable(true)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});




		FragmentManager fm = getFragmentManager();
		dataFragment = (RetainedFragment) fm.findFragmentByTag(RETAINED_FRAGMENT);

		if (dataFragment == null || savedInstanceState == null) {
			dataFragment = new RetainedFragment();
			fm.beginTransaction().add(dataFragment, RETAINED_FRAGMENT).commit();

			if (getArguments() != null) {
				mEditList = (List) getArguments().getSerializable(LIST);
				setDataToView();
			} else {
				setRandomImage();
			}
		} else {
			loadImage(dataFragment.getImagePath());
		}

		dataFragment.setOnLoadedFinish(new RetainedFragment.ImageLoad() {
			@Override
			public void finish(String path) {
				loadImage(path);
			}
		});

		if (savedInstanceState != null) {
			mFile = (File) savedInstanceState.getSerializable(STATE_FILE);
		}

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();

		if (getDialog() == null) {
			return;
		}

		final AlertDialog dialog = (AlertDialog) getDialog();

		Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Boolean wantToCloseDialog = saveData();
				if (wantToCloseDialog) {
					dialog.dismiss();
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		mName.setSelection(mName.getText().length());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		dataFragment.setImagePath(mImagePath);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(STATE_FILE, mFile);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		getActivity().getMenuInflater().inflate(R.menu.image_menu, menu);
		menu.findItem(R.id.random_image).setVisible(true);
		menu.setHeaderTitle(getString(R.string.list_image));

		MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				onContextItemSelected(item);
				return true;
			}
		};

		for (int i = 0, n = menu.size(); i < n; i++)
			menu.getItem(i).setOnMenuItemClickListener(listener);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.take_photo:
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				mFile = Image.create().createImageFile();

				if (mFile != null) {
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
					startActivityForResult(cameraIntent, TAKE_PHOTO);
				} else {
					Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
				}

				break;
			case R.id.select_from_gallery:
				Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(galleryIntent, LOAD_IMAGE);
				break;
			case R.id.random_image:
				setRandomImage();
				break;
			default:
				return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		switch (requestCode) {
			case TAKE_PHOTO:
				deletePhotoFromGallery();
				dataFragment.execute(mFile, metrics.widthPixels - 30);
				break;
			case LOAD_IMAGE:
				try {
					File file = Image.create().createImageFile();

					if (file != null) {
						Uri selectedImage = data.getData();
						Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
						dataFragment.execute(file, bitmap, metrics.widthPixels - 30);
					} else {
						Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_file_not_create), Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				break;
		}
	}

	private void setDataToView() {
		loadImage(mEditList.getImagePath());

		mName.setText(mEditList.getName());
		mName.setSelection(mName.getText().length());

		mCurrency.setSelection(getPosition(mCurrency, mEditList.getIdCurrency()));
	}

	private SimpleCursorAdapter getSpinnerAdapter() {
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.spinner_currency,
				new CurrenciesDS(getActivity()).getAll(),
				new String[]{ICurrencies.COLUMN_SYMBOL, ICurrencies.COLUMN_NAME},
				new int[]{R.id.currency_symbol, R.id.currency_name}, 0);
		spinnerAdapter.setDropDownViewResource(R.layout.spinner_currency_drop);
		return spinnerAdapter;
	}

	private long getDefaultIdCurrency() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		return prefs.getLong(getResources().getString(R.string.settings_key_currency), -1);
	}

	private int getPosition(Spinner spinner, long idCurrency) {
		int index = 0;
		for (int i = 0; i < spinner.getCount(); i++) {
			long id = ((CurrencyCursor) spinner.getItemAtPosition(i)).getEntity().getId();
			if (id == idCurrency) {
				index = i;
				break;
			}
		}
		return index;
	}

	private void loadImage(String imagePath) {
		mImagePath = imagePath;
		Image.create().insertImageToView(getActivity(), mImagePath, mImageList);
	}

	private void setRandomImage() {
		String path;

		do {
			path = Image.LIST_IMAGE_PATH + "random_list_" + new Random().nextInt(9) + ".png";
		} while (path.equals(mImagePath));

		loadImage(path);
	}

	private boolean saveData() {
		boolean isSave = false;
		String name = mName.getText().toString().trim();

		if (name.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_name));
		} else {
			mNameInputLayout.setError(null);
			mNameInputLayout.setErrorEnabled(false);
		}

		if (dataFragment.isLoading()) {
			Toast.makeText(getActivity().getApplicationContext(), "Подождите загрузке картинки", Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!mNameInputLayout.isErrorEnabled()) {
			long idCurrency = ((CurrencyCursor) mCurrency.getSelectedItem()).getEntity().getId();

			ListsDS listDS = new ListsDS(getActivity());

			long id;
			if (getArguments() == null) {
				id = listDS.add(new List(name, idCurrency, mImagePath));
			} else {
				id = mEditList.getId();
				listDS.update(new List(id, name, idCurrency, mImagePath));
			}

			sendResult(id);

			isSave = true;
		}

		return isSave;
	}

	private void sendResult(long id) {
		if (getTargetFragment() == null)
			return;

		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent().putExtra(ID_LIST, id));
	}

	private void deletePhotoFromGallery() {
		String[] projection = {BaseColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN};

		Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

		if ((cursor != null) && (cursor.moveToFirst())) {
			String id = cursor.getString(cursor.getColumnIndex(BaseColumns._ID));
			long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));

			if (Math.abs(date - mFile.lastModified()) < 30000) {
				ContentResolver cr = getActivity().getContentResolver();
				cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + id, null);
			}
			cursor.close();
		}
	}
}