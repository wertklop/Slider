package org.slider;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class Slider extends ViewGroup {

	private int xDiff;
	private int startX;
	private int mCurrentScreen;
	private float mLastMotionX;
	private boolean mFirstLayout = true;
	
	public Slider(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ViewSwitcher can only be used in EXACTLY mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ViewSwitcher can only be used in EXACTLY mode.");
		}

		//The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			scrollTo(mCurrentScreen * width, 0);
			mFirstLayout = false;
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	/**
	 * Returns the index of the currently displayed screen.
	 * 
	 * @return The index of the currently displayed screen.
	 */
	public int getCurrentScreen() {
		return mCurrentScreen;
	}
	
	/**
	 * Sets the current screen.
	 * 
	 * @param currentScreen The new screen.
	 */
	public void setCurrentScreen(int numberOfScreen) {
		mCurrentScreen = Math.max(0, Math.min(numberOfScreen, getChildCount() - 1));
		scrollTo(mCurrentScreen * getWidth(), 0);
		invalidate();
		
	}
	
	/**
	 * Sets the current screen.
	 * 
	 * @param direction Direction play animation
	 * @param moveLenght Length of screen move
	 */
	public void setCurrentScreen(final int direction, final int moveLenght) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (direction > 0) {
					// Move to right
					scrollBy(1, 0);
				} else if (direction < 0) {
					// Move to left
					scrollBy(-1, 0);
				}				
			}
		};
		Thread thread = new Thread(new Runnable() {
			public void run() {
				for(int i = 0; i < moveLenght; i++){
					try {
						Thread.sleep(0, 10000);
						handler.sendMessage(handler.obtainMessage());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
		
		if (direction > 0) {
			//Next screen
			mCurrentScreen = getCurrentScreen() + 1;
		} else if (direction < 0) {
			//Previous screen
			mCurrentScreen = getCurrentScreen() - 1;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			startX = Float.valueOf(x).intValue();
			break;

		case MotionEvent.ACTION_MOVE:
			xDiff = (int) Math.abs(x - mLastMotionX);//deltaX absolute
			if(xDiff != 0){
				int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;
				final int scrollX = getScrollX();
				if (deltaX < 0) {
					// Move to right
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					// Move to left
					final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
			}
			break;
			
		case MotionEvent.ACTION_UP:
			int X = (int)event.getX() + getCurrentScreen()*getWidth();
			int Y = (int)event.getY();
			if(Math.abs(startX - Float.valueOf(x).intValue()) == 0){
				searchView(getChildAt(getCurrentScreen()), X, Y);
			}
			if(startX - Float.valueOf(x).intValue() > 0){
				if(getCurrentScreen() < getChildCount() - 1){
					setCurrentScreen((startX - Float.valueOf(x).intValue()), (getWidth() - startX + (int)event.getX()));
				}
			}
			if(startX - Float.valueOf(x).intValue() < 0){
				if(getCurrentScreen() > 0){
					setCurrentScreen((startX - Float.valueOf(x).intValue()), (getWidth() + startX - (int)event.getX()));
				}
			}
			break;
			
		default:
			break;
		}
		return true;
	}
		
	private void searchView(View rootView, int x, int y){
    	int childCount = 0;
    	boolean inRange = true;
    	try {
			childCount = ((ViewGroup) rootView).getChildCount();
		} catch (ClassCastException e) {
			System.out.println(e + " Объект не является контейнером! Невозможно получить количество его потомков, т.к. он их не имеет");
		}
    	if(childCount != 0){
			for (int i = 0; i < childCount; i++) {
				View child = ((ViewGroup) rootView).getChildAt(i);
				if (child instanceof ViewGroup) {
					searchView((ViewGroup) child, x, y);
				} else {
					//Если координата точки нажатия находится в диапазоне координат объекта,
					//то вызываем событие нажатия
					if(x > child.getRight())  inRange = false;
					if(x < child.getLeft())   inRange = false;
					if(y < child.getTop())    inRange = false;
					if(y > child.getBottom()) inRange = false;
					if(inRange){
						child.performClick();
					}
				}
			}
    	}
    	else{
    		//Если координата точки нажатия находится в диапазоне координат объекта,
			//то вызываем событие нажатия
			if(x > rootView.getRight())  inRange = false;
			if(x < rootView.getLeft())   inRange = false;
			if(y < rootView.getTop())    inRange = false;
			if(y > rootView.getBottom()) inRange = false;
			if(inRange){
				rootView.performClick();
			}
    	}
    }
}
