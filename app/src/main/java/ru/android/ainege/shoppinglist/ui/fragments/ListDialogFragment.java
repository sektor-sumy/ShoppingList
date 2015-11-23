package ru.android.ainege.shoppinglist.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import ru.android.ainege.shoppinglist.R;

public class ListDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.dialog_list, null);

		ImageView imageList = (ImageView) v.findViewById(R.id.image);
		imageList.setImageResource(R.drawable.list);

		Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.list_menu);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()){
					case R.id.take_photo:
						Toast.makeText(getActivity(), "new photo", Toast.LENGTH_SHORT).show();
						return true;
					case R.id.select_from_gallery:
						Toast.makeText(getActivity(), "image from gallery", Toast.LENGTH_SHORT).show();
						return true;
					case R.id.reset_image:
						Toast.makeText(getActivity(), "random image", Toast.LENGTH_SHORT).show();
						return true;
					default:
						return false;
				}
			}
		});

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
		return builder.create();
	}
}