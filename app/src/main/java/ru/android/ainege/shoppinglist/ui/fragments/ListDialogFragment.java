package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
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
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDataSource;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDataSource;
import ru.android.ainege.shoppinglist.db.entities.List;
import ru.android.ainege.shoppinglist.db.tables.CurrencyTable;

public class ListDialogFragment extends DialogFragment {
	private static final String ID_LIST = "idList";
	private static final String LIST = "list";

	private static final int TAKE_PHOTO_CODE = 0;
	private static final int LOAD_IMAGE_CODE = 1;

	private ImageView mImageList;
	private TextInputLayout mNameInputLayout;
	private EditText mName;
	private Spinner mCurrency;

	private List mEditList;
	private File mFile;
	private String mImagePath;

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

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.list_menu);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.take_photo:
						Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

						mFile = Image.create().createImageFile();
						if (mFile != null) {
							mImagePath = Image.PATH_PROTOCOL + mFile.getAbsolutePath();

							cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mFile));
							startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
						} else {
							Toast.makeText(getActivity(), "Не удалось создать файл", Toast.LENGTH_SHORT).show();
						}
						return true;
					case R.id.select_from_gallery:
						Intent galleryIntent = new Intent(Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(galleryIntent, LOAD_IMAGE_CODE);
						return true;
					case R.id.reset_image:
						setRandomImage();
						return true;
					default:
						return false;
				}
			}
		});

		mImageList = (ImageView) v.findViewById(R.id.image);

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mName = (EditText) v.findViewById(R.id.name);
		mCurrency = (Spinner) v.findViewById(R.id.currency);
		mCurrency.setAdapter(getSpinnerAdapter());
		mCurrency.setSelection(getPosition(mCurrency, getIdCurrency()));

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

		if (getArguments() != null) {
			mEditList = (List) getArguments().getSerializable(LIST);
			setDataToView();
		} else {
			setRandomImage();
		}

		return builder.create();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) return;

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		switch (requestCode) {
			case TAKE_PHOTO_CODE:
				if (Image.create().postProcessingToFile(mFile, metrics.widthPixels - 30)) {
					loadImage();
				} else {
					Toast.makeText(getActivity(), "Не удалось получить файл", Toast.LENGTH_SHORT).show();
				}
				break;
			case LOAD_IMAGE_CODE:
				Uri selectedImage = data.getData();

				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

					Image image = Image.create();
					File file = image.createImageFile();

					if (file != null) {
						bitmap = image.postProcessing(bitmap, metrics.widthPixels - 30);
						if (image.saveImageToFile(file, bitmap)) {
							mImagePath = Image.PATH_PROTOCOL + file.getAbsolutePath();
							loadImage();
						} else {
							Toast.makeText(getActivity(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getActivity(), "Не удалось создать файл", Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
		}
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

	private SimpleCursorAdapter getSpinnerAdapter() {
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.spinner_currency,
				new CurrenciesDataSource(getActivity()).getAll(),
				new String[]{CurrencyTable.COLUMN_SYMBOL, CurrencyTable.COLUMN_NAME},
				new int[]{R.id.currency_symbol, R.id.currency_name}, 0);
		spinnerAdapter.setDropDownViewResource(R.layout.spinner_currency_drop);
		return spinnerAdapter;
	}

	private long getIdCurrency() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		return prefs.getLong(getResources().getString(R.string.settings_key_dafault_currency), -1);
	}

	private void setDataToView() {
		mImagePath = mEditList.getImagePath();
		loadImage();

		mName.setText(mEditList.getName());
		mName.setSelection(mName.getText().length());

		mCurrency.setSelection(getPosition(mCurrency, mEditList.getIdCurrency()));
	}

	private int getPosition(Spinner spinner, long idCurrency) {
		int index = 0;
		for (int i = 0; i < spinner.getCount(); i++) {
			long id = ((CurrenciesDataSource.CurrencyCursor) spinner.getItemAtPosition(i)).getEntity().getId();
			if (id == idCurrency) {
				index = i;
				break;
			}
		}
		return index;
	}

	private void loadImage() {
		Image.create().insertImageToView(getActivity(), mImagePath, mImageList);
	}

	private void setRandomImage() {
		String path;
		do {
			path = Image.ASSETS_IMAGE_PATH + "item/random_list_" + (new Random().nextInt(4) + 1) + ".jpg";
		} while(path.equals(mImagePath));
		mImagePath = path;
		loadImage();
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

		if (!mNameInputLayout.isErrorEnabled()) {
			long idCurrency = ((CurrenciesDataSource.CurrencyCursor) mCurrency.getSelectedItem()).getEntity().getId();

			ListsDataSource listDS = new ListsDataSource(getActivity());

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
}