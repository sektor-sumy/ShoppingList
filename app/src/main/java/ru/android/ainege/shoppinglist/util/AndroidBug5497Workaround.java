package ru.android.ainege.shoppinglist.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class AndroidBug5497Workaround {
	// For more information, see https://code.google.com/p/android/issues/detail?id=5497

	public static AndroidBug5497Workaround assistActivity (Activity activity) {
		return new AndroidBug5497Workaround(activity);
	}

	OnOpenKeyboard mOnOpenKeyboard;

	public interface OnOpenKeyboard {
		void isOpen(int screenAppHeight);
		void isClose();
	}

	public void setOnOpenKeyboard(OnOpenKeyboard onOpenKeyboard) {
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

			if (heightDifference > (usableHeightSansKeyboard/4)) {
				frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;

				// keyboard probably just became visible
				if (mOnOpenKeyboard != null) {
					mOnOpenKeyboard.isOpen(frameLayoutParams.height);
				}
			} else {
				frameLayoutParams.height = defaultHeight;

				// keyboard probably just became hidden
				if (mOnOpenKeyboard != null) {
					mOnOpenKeyboard.isClose();
				}
			}

			mChildOfContent.requestLayout();
			usableHeightPrevious = usableHeightNow;
		}
	}

	private int computeUsableHeight() {
		Rect r = new Rect();
		mChildOfContent.getWindowVisibleDisplayFrame(r);

		return r.bottom - r.top;
	}
}
