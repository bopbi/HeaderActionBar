package com.arjunalabs.headeractionbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Space;

/**
 * Created by bobbyadiprabowo on 10/11/14.
 */
public abstract class HeaderActivity extends Activity {

    private static final String TAG = "HeaderActionBar";

    public static final int HEADER_BACKGROUND_SCROLL_NORMAL = 0;
    public static final int HEADER_BACKGROUND_SCROLL_PARALLAX = 1;
    public static final int HEADER_BACKGROUND_SCROLL_STATIC = 2;

    private FrameLayout mFrameLayout;
    private View mContentOverlay;

    // header
    private View mHeader;
    private View mHeaderHeader;
    private View mHeaderBackground;
    private int mHeaderHeight;
    private int mHeaderScroll;

    private int mHeaderBackgroundScrollMode = HEADER_BACKGROUND_SCROLL_NORMAL;

    private Space mFakeHeader;
    private boolean isListViewEmpty;

    // listeners
    private AbsListView.OnScrollListener mOnScrollListener;
    private OnHeaderScrollChangedListener mOnHeaderScrollChangedListener;

    public interface OnHeaderScrollChangedListener {
        public void onHeaderScrollChanged(float progress, int height, int scroll);
    }

    public void setOnHeaderScrollChangedListener(OnHeaderScrollChangedListener listener) {
        mOnHeaderScrollChangedListener = listener;
    }

    public void setHeaderBackgroundScrollMode(int scrollMode) {
        mHeaderBackgroundScrollMode = scrollMode;
    }

    private void scrollHeaderTo(int scrollTo) {
        scrollHeaderTo(scrollTo, false);
    }

    private void scrollHeaderTo(int scrollTo, boolean forceChange) {
        scrollTo = Math.min(Math.max(scrollTo, -mHeaderHeight), 0);
        if (mHeaderScroll == (mHeaderScroll = scrollTo) & !forceChange) return;

        setViewTranslationY(mHeader, scrollTo);
        setViewTranslationY(mHeaderHeader, -scrollTo);

        switch (mHeaderBackgroundScrollMode) {
            case HEADER_BACKGROUND_SCROLL_NORMAL:
                setViewTranslationY(mHeaderBackground, 0);
                break;
            case HEADER_BACKGROUND_SCROLL_PARALLAX:
                setViewTranslationY(mHeaderBackground, -scrollTo / 1.6f);
                break;
            case HEADER_BACKGROUND_SCROLL_STATIC:
                setViewTranslationY(mHeaderBackground, -scrollTo);
                break;
        }

        if (mContentOverlay != null) {
            final ViewGroup.LayoutParams lp = mContentOverlay.getLayoutParams();
            final int delta = mHeaderHeight + scrollTo;
            lp.height = mFrameLayout.getHeight() - delta;
            mContentOverlay.setLayoutParams(lp);
            mContentOverlay.setTranslationY(delta);
        }

        notifyOnHeaderScrollChangeListener(
                (float) -scrollTo / mHeaderHeight,
                mHeaderHeight,
                -scrollTo);
    }

    private void setViewTranslationY(View view, float translationY) {
        if (view != null) view.setTranslationY(translationY);
    }

    private void notifyOnHeaderScrollChangeListener(float progress, int height, int scroll) {
        if (mOnHeaderScrollChangedListener != null) {
            mOnHeaderScrollChangedListener.onHeaderScrollChanged(progress, height, scroll);
        }
    }

    public abstract View onCreateHeaderView(LayoutInflater inflater, ViewGroup container);

    public abstract View onCreateContentView(LayoutInflater inflater, ViewGroup container);

    public abstract View onCreateContentOverlayView(LayoutInflater inflater, ViewGroup container);

    public void setListViewAdapter(ListView listView, ListAdapter adapter) {
        isListViewEmpty = adapter == null;
        listView.setAdapter(null);
        listView.removeHeaderView(mFakeHeader);
        listView.addHeaderView(mFakeHeader);
        listView.setAdapter(adapter);
    }

    /**
     * {@inheritDoc AbsListView#setOnScrollChangedListener}
     */
    public void setListViewOnScrollChangedListener(AbsListView.OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    // //////////////////////////////////////////
    // //////////// -- GETTERS -- ///////////////
    // //////////////////////////////////////////

    public View getHeaderView() {
        return mHeader;
    }

    public View getHeaderHeaderView() {
        return mHeaderHeader;
    }

    public View getHeaderBackgroundView() {
        return mHeaderBackground;
    }

    public int getHeaderBackgroundScrollMode() {
        return mHeaderBackgroundScrollMode;
    }

    public View initLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mFrameLayout = new FrameLayout(this);

        mHeader = onCreateHeaderView(inflater, mFrameLayout);
        mHeaderHeader = mHeader.findViewById(android.R.id.title);
        mHeaderBackground = mHeader.findViewById(android.R.id.background);
        assert mHeader.getLayoutParams() != null;
        mHeaderHeight = mHeader.getLayoutParams().height;

        mFakeHeader = new Space(this);
        mFakeHeader.setLayoutParams(
                new ListView.LayoutParams(0, mHeaderHeight));

        View content = onCreateContentView(inflater, mFrameLayout);
        if (content instanceof ListView) {
            isListViewEmpty = true;

            final ListView listView = (ListView) content;
            listView.addHeaderView(mFakeHeader);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                    if (mOnScrollListener != null) {
                        mOnScrollListener.onScrollStateChanged(absListView, scrollState);
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    if (mOnScrollListener != null) {
                        mOnScrollListener.onScroll(
                                absListView, firstVisibleItem,
                                visibleItemCount, totalItemCount);
                    }

                    if (isListViewEmpty) {
                        scrollHeaderTo(0);
                    } else {
                        final View child = absListView.getChildAt(0);
                        assert child != null;
                        scrollHeaderTo(child == mFakeHeader ? child.getTop() : -mHeaderHeight);
                    }
                }
            });
        } else {

            // Merge fake header view and content view.
            final LinearLayout view = new LinearLayout(this);
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            view.setOrientation(LinearLayout.VERTICAL);
            view.addView(mFakeHeader);
            view.addView(content);

            // Put merged content to ScrollView
            final NotifyingScrollView scrollView = new NotifyingScrollView(this);
            scrollView.addView(view);
            scrollView.setOnScrollChangedListener(new NotifyingScrollView.OnScrollChangedListener() {
                @Override
                public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                    scrollHeaderTo(-t);
                }
            });
            content = scrollView;
        }

        mFrameLayout.addView(content);
        mFrameLayout.addView(mHeader);

        // Content overlay view always shows at the top of content.
        if ((mContentOverlay = onCreateContentOverlayView(inflater, mFrameLayout)) != null) {
            mFrameLayout.addView(mContentOverlay, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        // Post initial scroll
        mFrameLayout.post(new Runnable() {
            @Override
            public void run() {
                scrollHeaderTo(0, true);
            }
        });

        return mFrameLayout;
    }
}
