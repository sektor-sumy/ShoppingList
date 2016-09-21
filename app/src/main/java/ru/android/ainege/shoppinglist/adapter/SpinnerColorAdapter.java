package ru.android.ainege.shoppinglist.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import ru.android.ainege.shoppinglist.R;
import ru.android.ainege.shoppinglist.db.TableInterface;

public class SpinnerColorAdapter extends SimpleCursorAdapter {
	private Context mContext;
	private boolean mIsUseCategory = true;

	public SpinnerColorAdapter(Context context, int layout, Cursor c, boolean isGetUseCategoryFromSetting) {
		super(context, layout, c, new String[]{}, new int[]{}, 0);

		mContext = context;

		if (isGetUseCategoryFromSetting) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			mIsUseCategory = prefs.getBoolean(context.getString(R.string.settings_key_use_category), true);
		}
	}

	public void updateUseCategoryFromSetting() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mIsUseCategory = prefs.getBoolean(mContext.getString(R.string.settings_key_use_category), true);
		notifyDataSetChanged();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return setHolderToView(super.newView(context, cursor, parent));
	}

	@Override
	public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
		return setHolderToView(super.newDropDownView(context, cursor, parent));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);

		ViewHolder holder = (ViewHolder) view.getTag();
		String name = "";

		if (cursor.getColumnIndex(TableInterface.ItemsInterface.COLUMN_NAME) != -1) {
			name = cursor.getString(cursor.getColumnIndex(TableInterface.ItemsInterface.COLUMN_NAME));
		} else if (cursor.getColumnIndex(TableInterface.CategoriesInterface.COLUMN_NAME) != -1) {
			name = cursor.getString(cursor.getColumnIndex(TableInterface.CategoriesInterface.COLUMN_NAME));
		}

		holder.mName.setText(name);

		if (mIsUseCategory) {
			holder.mColor.setBackgroundColor(cursor.getInt(cursor.getColumnIndex(TableInterface.CategoriesInterface.COLUMN_COLOR)));
		}
	}

	private View setHolderToView(View v) {
		ViewHolder holder = new ViewHolder(v);
		v.setTag(holder);

		return v;
	}

	private class ViewHolder {
		public TextView mName;
		public TextView mColor;

		ViewHolder(View v) {
			mName = (TextView) v.findViewById(R.id.name);
			mColor = (TextView) v.findViewById(R.id.color);
		}
	}
}
