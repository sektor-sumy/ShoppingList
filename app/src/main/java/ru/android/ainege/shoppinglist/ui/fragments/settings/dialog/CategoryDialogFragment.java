package ru.android.ainege.shoppinglist.ui.fragments.settings.dialog;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.sliders.LobsterOpacitySlider;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.entities.Category;
import ru.android.ainege.shoppinglist.util.FirebaseAnalytic;

public class CategoryDialogFragment extends GeneralDialogFragment<Category> {
	private static final String STATE_COLOR = "state_color";

	private LobsterPicker mLobsterPicker;

	public static CategoryDialogFragment newInstance(Category category) {
		Bundle args = new Bundle();
		args.putSerializable(ITEM, category);

		CategoryDialogFragment fragment = new CategoryDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(STATE_COLOR, mLobsterPicker.getColor());
	}

	@Override
	protected View setupView() {
		View v = super.setupView();

		LinearLayout mColorPicker = (LinearLayout) v.findViewById(R.id.color_picker);
		mLobsterPicker = (LobsterPicker) v.findViewById(R.id.lobster_picker);
		LobsterShadeSlider shadeSlider = (LobsterShadeSlider) v.findViewById(R.id.shade_slider);
		LobsterOpacitySlider opacitySlider = (LobsterOpacitySlider) v.findViewById(R.id.opacity_slider);

		mLobsterPicker.addDecorator(shadeSlider);
		mLobsterPicker.addDecorator(opacitySlider);

		mColorPicker.setVisibility(View.VISIBLE);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		return v;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.category_info);
	}

	@Override
	protected void setDataToView(Bundle savedInstanceState) {
		super.setDataToView(savedInstanceState);
		int color;

		if (savedInstanceState == null) {
			color = mEditItem.getColor();
		} else {
			color = savedInstanceState.getInt(STATE_COLOR);
		}

		mLobsterPicker.setColor(color);
	}


	@Override
	protected boolean saveData() {
		boolean isSave = false;
		String name = mName.getText().toString().trim();

		if (name.length() == 0) {
			mNameInputLayout.setError(getString(R.string.error_name));
		} else {
			mNameInputLayout.setError(null);
			mNameInputLayout.setErrorEnabled(false);
		}

		if (!mNameInputLayout.isErrorEnabled()) {
			int color = mLobsterPicker.getColor();
			CategoriesDS categoryDS = new CategoriesDS(getActivity());
			long id;

			if (getArguments() == null) {
				id = categoryDS.add(new Category(name, color));
				addAnalytics(FirebaseAnalytic.ADD_CATEGORY, name);
			} else {
				id = mEditItem.getId();
				categoryDS.update(new Category(id, name, color));
			}

			sendResult(id);
			isSave = true;
		}

		return isSave;
	}
}
