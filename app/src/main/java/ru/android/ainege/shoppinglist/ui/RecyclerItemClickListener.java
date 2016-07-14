package ru.android.ainege.shoppinglist.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import ru.android.ainege.shoppinglist.R;

public class RecyclerItemClickListener extends RecyclerView.SimpleOnItemTouchListener {
	private final OnItemClickListener mListener;
	private final GestureDetector mGestureDetector;

	public interface OnItemClickListener {
		void onClick(int position);

		void onLongClick(int position);

		void onSwipeRight(int position);
	}

	public RecyclerItemClickListener(final Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
		mListener = listener;

		//define gesture
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

				if (childView != null && mListener != null) {
					mListener.onLongClick(recyclerView.getChildAdapterPosition(childView));
				}
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (e1 == null || e2 == null) {
					return false;
				}

				View childView = recyclerView.findChildViewUnder(e1.getX(), e1.getY());

				if (childView != null && mListener != null) {
					int swipe_distanceX = 120;
					int swipe_distanceY = (int) context.getResources().getDimension(R.dimen.row_list_item_height);
					int swipe_velocity = 200; //measure in pixels per second

					if (((e2.getX() - e1.getX()) > swipe_distanceX) && (Math.abs(e2.getY() - e1.getY()) <= swipe_distanceY)
							&& (Math.abs(velocityX) > swipe_velocity)) {
						mListener.onSwipeRight(recyclerView.getChildAdapterPosition(childView));
					}
				}

				return false;
			}
		});
	}

	@Override
	public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
		if (e == null) {
			return false;
		}

		View childView = rv.findChildViewUnder(e.getX(), e.getY());

		if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
			mListener.onClick(rv.getChildAdapterPosition(childView));
			return true;
		}

		return false;
	}
}
