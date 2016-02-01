package ru.android.ainege.shoppinglist.ui.fragments.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.sliders.LobsterOpacitySlider;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.dataSources.CategoriesDS;
import ru.android.ainege.shoppinglist.db.entities.Category;

public class CategoryDialogFragment extends GeneralDialogFragment<Category> {
	private LinearLayout colorPicker;
	private LobsterPicker lobsterPicker;
	private LobsterShadeSlider shadeSlider;
	private LobsterOpacitySlider opacitySlider;

	public static CategoryDialogFragment newInstance(Category category) {
		Bundle args = new Bundle();
		args.putSerializable(ITEM, category);

		CategoryDialogFragment fragment = new CategoryDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	protected View setupView() {
		View v = super.setupView();

		colorPicker = (LinearLayout) v.findViewById(R.id.color_picker);
		lobsterPicker = (LobsterPicker) v.findViewById(R.id.lobster_picker);
		shadeSlider = (LobsterShadeSlider) v.findViewById(R.id.shade_slider);
		opacitySlider = (LobsterOpacitySlider) v.findViewById(R.id.opacity_slider);

		lobsterPicker.addDecorator(shadeSlider);
		lobsterPicker.addDecorator(opacitySlider);

		colorPicker.setVisibility(View.VISIBLE);

		return v;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.category_info);
	}

	@Override
	protected void setDataToView() {
		super.setDataToView();

		lobsterPicker.setColor(mEditItem.getColor());
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
			int color = lobsterPicker.getColor();
			CategoriesDS categoryDS = new CategoriesDS(getActivity());
			long id;

			if (getArguments() == null) {
				id = categoryDS.add(new Category(name, color));
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
