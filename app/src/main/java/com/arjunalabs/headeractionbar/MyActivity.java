package com.arjunalabs.headeractionbar;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;


public class MyActivity extends HeaderActivity {

    private FadingActionBarHelper mFadingActionBarHelper;
    private ListView mListView;
    private FrameLayout mContentOverlay;

    String[] mListViewTitles = new String[]{"Placeholder", "Placeholder", "Placeholder", "Placeholder",
            "Placeholder", "Placeholder", "Placeholder", "Placeholder",
            "Placeholder", "Placeholder", "Placeholder", "Placeholder",
            "Placeholder", "Placeholder", "Placeholder", "Placeholder",
            "Placeholder", "Placeholder", "Placeholder", "Placeholder",
            "Placeholder", "Placeholder", "Placeholder", "Placeholder"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = initLayout();



        mFadingActionBarHelper = new FadingActionBarHelper(getActionBar(),
                getResources().getDrawable(R.color.actionbar_bg));
        setHeaderBackgroundScrollMode(HEADER_BACKGROUND_SCROLL_PARALLAX);
        setOnHeaderScrollChangedListener(new OnHeaderScrollChangedListener() {
            @Override
            public void onHeaderScrollChanged(float progress, int height, int scroll) {
                height -= getActionBar().getHeight();

                progress = (float) scroll / height;
                if (progress > 1f) progress = 1f;

                // *
                // `*
                // ```*
                // ``````*
                // ````````*
                // `````````*
                progress = (1 - (float) Math.cos(progress * Math.PI)) * 0.5f;

                mFadingActionBarHelper.setActionBarAlpha((int) (255 * progress));
            }
        });

        setContentView(view);
        setListViewTitles(mListViewTitles);
    }

    private void setListViewTitles(String[] titles) {
        mListViewTitles = titles;
        if (mListView == null) return;

        mListView.setVisibility(View.VISIBLE);
        setListViewAdapter(mListView, new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1,
                mListViewTitles));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateHeaderView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.actionbar_header, container, false);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        mListView = new ListView(this);
        return mListView;
    }

    @Override
    public View onCreateContentOverlayView(LayoutInflater inflater, ViewGroup container) {
        ProgressBar progressBar = new ProgressBar(this);
        mContentOverlay = new FrameLayout(this);
        mContentOverlay.addView(progressBar, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        mContentOverlay.setVisibility(View.GONE);
        return mContentOverlay;
    }

}
