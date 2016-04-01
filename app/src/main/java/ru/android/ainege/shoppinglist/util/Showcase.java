package ru.android.ainege.shoppinglist.util;


import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import ru.android.ainege.shoppinglist.R;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class Showcase {
	public static final String PREFS_SHOWCASE = "material_showcaseview_prefs";

	public static final String SHOT_ADD_LIST = "add_list";
	public static final String SHOT_LIST = "list";
	public static final String SHOT_ADD_ITEM = "add_item";
	public static final String SHOT_ITEM_IN_LIST = "item_in_list";
	public static final String SHOT_ITEM = "item";
	public static final String SHOT_CATEGORY = "category";
	public static final String SHOT_CATEGORY_COLLAPSE = "category_collapse";
	public static final String SHOT_CURRENCY = "currency";

	public static MaterialShowcaseView.Builder createShowcase (Activity activity, View target, String content) {
		return new MaterialShowcaseView.Builder(activity)
				.setTarget(target)
				.setMaskColour(ContextCompat.getColor(activity, R.color.showcase_background_color))
				.setContentText(content)
				.setContentTextColor(ContextCompat.getColor(activity, R.color.showcase_title_color))
				.setDismissOnTouch(true)
				.setShapePadding(activity.getResources().getDimensionPixelOffset(R.dimen.shape_padding));
	}
}