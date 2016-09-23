package ru.android.ainege.shoppinglist.util;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdView;

import ru.android.ainege.shoppinglist.R;

public class AndroidBug5497Workaround {
	// For more information, see https://code.google.com/p/android/issues/detail?id=5497

	public static AndroidBug5497Workaround assistActivity (Activity activity) {
		return new AndroidBug5497Workaround(activity);
	}

	private OnOpenKeyboardListener mOnOpenKeyboard;

	public interface OnOpenKeyboardListener {
		void isOpen(int screenAppHeight, AdView adView);
		void isClose(AdView adView);
	}

	public void setOnOpenKeyboard(OnOpenKeyboardListener onOpenKeyboard) {
		mOnOpenKeyboard = onOpenKeyboard;
	}

	private View mChildOfContent;
	private int usableHeightPrevious;
	private FrameLayout.LayoutParams frameLayoutParams;
	private final int defaultHeight = ViewGroup.LayoutParams.MATCH_PARENT;

	private AndroidBug5497Workaround(Activity activity) {
		FrameLayout content = (FrameLayout)  activity.findViewById(android.R.id.content);
		mChildOfContent = content.getChildAt(0);

		if (mChildOfContent != null) {
			mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				public void onGlobalLayout() {
					possiblyResizeChildOfContent();
				}
			});
			frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
		}
	}

	private void possiblyResizeChildOfContent() {
		int usableHeightNow = computeUsableHeight();
		if (usableHeightNow != usableHeightPrevious) {
			int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
			int heightDifference = usableHeightSansKeyboard - usableHeightNow;
			AdView adView = (AdView) mChildOfContent.findViewById(R.id.adView);

			if (heightDifference > (usableHeightSansKeyboard/4)) {
				frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;

				// keyboard probably just became visible
				if (mOnOpenKeyboard != null) {
					mOnOpenKeyboard.isOpen(frameLayoutParams.height, adView);
				}
			} else {
				frameLayoutParams.height = defaultHeight;

				// keyboard probably just became hidden
				if (mOnOpenKeyboard != null) {
					mOnOpenKeyboard.isClose(adView);
				}
			}

			mChildOfContent.requestLayout();
			usableHeightPrevious = usableHeightNow;
		}
	}

	private int computeUsableHeight() {
		Rect r = new Rect();
		mChildOfContent.getWindowVisibleDisplayFrame(r);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return r.bottom;
		} else {
			return r.bottom - r.top;
		}
	}
}
