package com.dtalk.dd.ui.fragment;

import android.view.View;
import android.widget.ProgressBar;

import com.dtalk.dd.R;
import com.dtalk.dd.ui.base.TTBaseFragment;

public abstract class MainFragment extends TTBaseFragment {
	private ProgressBar progressbar;

	public void init(View curView) {
		progressbar = (ProgressBar) curView.findViewById(R.id.progress_bar);
	}

	public void showProgressBar() {
		progressbar.setVisibility(View.VISIBLE);
	}

	public void hideProgressBar() {
		progressbar.setVisibility(View.GONE);
	}

}
