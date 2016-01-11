package ru.android.ainege.shoppinglist.ui;

import android.app.Activity;
import android.content.Context;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.ShowcaseView;

import ru.android.ainege.shoppinglist.R;

public class Showcase {
	public static final String PREFS_SHOWCASE_INTERNAL = "showcase_internal";

	private static int shot = 0;
	public static final int SHOT_LIST = shot++;
	public static final int SHOT_ADD_ITEM = shot++;
	public static final int SHOT_ITEM_IN_LIST = shot++;
	public static final int SHOT_ITEM = shot++;
	public static final int SHOT_CURRENCY = shot++;
	private static ShowcaseView mShowcaseView;
	private static Activity mActivity;


	public static Showcase newInstance(ShowcaseView showcaseView, Activity activity) {
		Showcase showcase = new Showcase();

		mShowcaseView = showcaseView;
		mActivity = activity;

		return showcase;
	}

	public void setButton(String text, boolean isRight) {
		int margin = (int) mActivity.getResources().getDimension(R.dimen.floating_button_margin);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		if (isRight) {
			lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}
		lp.setMargins(margin, margin, margin, margin);

		mShowcaseView.setButtonPosition(lp);
		mShowcaseView.setButtonText(text);
	}

	public static boolean shouldBeShown(Context context, int type) {
		return !context.getSharedPreferences(PREFS_SHOWCASE_INTERNAL, Context.MODE_PRIVATE)
				.getBoolean("hasShot" + type, false);
	}
}
