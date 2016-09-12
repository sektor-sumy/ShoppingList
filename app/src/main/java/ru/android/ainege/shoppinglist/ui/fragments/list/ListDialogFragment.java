package ru.android.ainege.shoppinglist.ui.fragments.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.TableInterface.CurrenciesInterface;
import ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS;
import ru.android.ainege.shoppinglist.db.dataSources.ListsDS;
import ru.android.ainege.shoppinglist.ui.OnFinishedImageListener;
import ru.android.ainege.shoppinglist.ui.view.PictureView;
import ru.android.ainege.shoppinglist.util.Image;

import static ru.android.ainege.shoppinglist.db.dataSources.CurrenciesDS.CurrencyCursor;

public abstract class ListDialogFragment extends DialogFragment implements PictureView.PictureInterface {
	public static final String ID_LIST = "idList";
	private static final String STATE_FILE = "state_file";

	protected EditText mNameEditText;
	protected Spinner mCurrencySpinner;
	protected PictureView mPictureView;
	protected String mImagePath;
	private TextInputLayout mNameInputLayout;

	protected abstract long save(ListsDS listDS, String name, long idCurrency);
	protected abstract void setDataToView();

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(createView(savedInstanceState))
				.setCancelable(true)
				.setPositiveButton(R.string.save, null)
				.setNegativeButton(R.string.cancel, null);

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		final AlertDialog dialog = (AlertDialog) getDialog();

		if (dialog != null) {
			Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (saveData()) {
						dialog.dismiss();
					}
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mNameEditText.setSelection(mNameEditText.getText().length());
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		sendResult(Activity.RESULT_CANCELED, null);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mPictureView.setImagePath(mImagePath);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(STATE_FILE, mPictureView.getFile());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mPictureView.onCreateContextMenu(menu, R.id.random_image, getString(R.string.list_image));

		// TODO: 03.09.2016 android bug
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
		return mPictureView.onContextItemSelected(item, null) || super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case PictureView.TAKE_PHOTO:
					mPictureView.takePhotoResult(mImagePath);
					break;
				case PictureView.FROM_GALLERY:
					mPictureView.fromGalleryResult(mImagePath, data.getData());
					break;
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		mPictureView.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public boolean isDeleteImage(String newPath) {
		return mImagePath != null &&
				!mImagePath.contains(Image.ASSETS_IMAGE_PATH) &&
				!mImagePath.equals(newPath);
	}

	@Override
	public void resetImage() {
		setRandomImage();
	}

	protected void loadImage(String imagePath) {
		mPictureView.loadImage(imagePath, mImagePath);
		mImagePath = imagePath;
	}

	protected void setRandomImage() {
		String path;

		do {
			path = Image.LIST_IMAGE_PATH + "random_list_" + new Random().nextInt(9) + ".png";
		} while (path.equals(mImagePath));

		loadImage(path);
	}

	protected int getPosition(Spinner spinner, long idCurrency) {
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

	private View createView(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_list, null);

		mPictureView = new PictureView(this, savedInstanceState, this,
				new OnFinishedImageListener() {
					@Override
					public void onFinished(int resultCode, String path) {
						loadImage(path);
					}
				});
		mPictureView.setImage(v.findViewById(R.id.image));

		mNameInputLayout = (TextInputLayout) v.findViewById(R.id.name_input_layout);
		mNameEditText = (EditText) v.findViewById(R.id.name);
		mCurrencySpinner = (Spinner) v.findViewById(R.id.currency);
		mCurrencySpinner.setAdapter(getSpinnerAdapter());
		mCurrencySpinner.setSelection(getPosition(mCurrencySpinner, getDefaultIdCurrency()));

		if (savedInstanceState == null) {
			setDataToView();
		} else {
			loadImage(mPictureView.getImagePath());
			mPictureView.setFile((File) savedInstanceState.getSerializable(STATE_FILE));
		}

		return v;
	}

	private SimpleCursorAdapter getSpinnerAdapter() {
		SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.spinner_currency,
				new CurrenciesDS(getActivity()).getAll(),
				new String[]{CurrenciesInterface.COLUMN_SYMBOL, CurrenciesInterface.COLUMN_NAME},
				new int[]{R.id.currency_symbol, R.id.currency_name}, 0);
		spinnerAdapter.setDropDownViewResource(R.layout.spinner_currency_drop);
		return spinnerAdapter;
	}

	private long getDefaultIdCurrency() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		return prefs.getLong(getResources().getString(R.string.catalogs_key_currency), -1);
	}

	private boolean saveData() {
		boolean isSave = false;

		if (isValidData()) {
			long idCurrency = ((CurrencyCursor) mCurrencySpinner.getSelectedItem()).getEntity().getId();
			ListsDS listDS = new ListsDS(getActivity());
			long id = save(listDS, mNameEditText.getText().toString().trim(), idCurrency);

			sendResult(Activity.RESULT_OK, new Intent().putExtra(ID_LIST, id));
			isSave = true;
		}

		return isSave;
	}

	private boolean isValidData() {
		boolean isValid = true;

		if (mNameEditText.getText().toString().trim().length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_name));
			isValid = false;
		} else {
			mNameInputLayout.setError(null);
			mNameInputLayout.setErrorEnabled(false);
		}

		if (mPictureView.isLoading()) {
			Toast.makeText(getActivity().getApplicationContext(), "Подождите загрузке картинки", Toast.LENGTH_SHORT).show();
			isValid = false;
		}

		return isValid;
	}

	private void sendResult(int resultCode, Intent intent) {
		if (getTargetFragment() != null) {
			getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
		}
	}
}