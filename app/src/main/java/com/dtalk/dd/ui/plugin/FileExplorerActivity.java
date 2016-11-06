/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.dtalk.dd.ui.plugin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.dtalk.dd.R;
import com.dtalk.dd.ui.base.TTBaseActivity;
import com.dtalk.dd.utils.FileUtils;

import java.io.File;

/**
 * 文件浏览器
 */
public class FileExplorerActivity extends TTBaseActivity implements
		View.OnClickListener {

	/**设备内置目录*/
	private static final int DIR_ROOT = 0;
	/**外置存储卡*/
	private static final int DIR_SDCARD = 1;
	/**文件浏览器列表*/
	private ListView mFileListView;
	/**设备选项卡*/
	private TextView mRootTab;
	/**设备选项卡导航线*/
	private View mRootTabSelector;
	/**存储卡选项卡*/
	private TextView mSdcardTab;
	/**存储卡导航线*/
	private View mSdcardTabSelector;
	
	private String mFileExplorerRootTag;
	private String mFileExplorerSdcardTag;
	private FileListAdapter mAdapter;
	private File mRootFile;
	private File mSdcardFile;
	/**浏览文件目录类型*/
	private int mType = DIR_ROOT;
	
	final private AdapterView.OnItemClickListener mItemClickListener
		= new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				File file = (File) mAdapter.getItem(position);
				if(!file.isFile()) {
					if(mType == DIR_ROOT) {
						// 当前为内置存储浏览模式
						mRootFile = file;
					} else {
						mSdcardFile = file;
					}
					
					if(file != mAdapter.getParentFile()) {
						mAdapter.setFiles(mAdapter.getCurrentFile(), file);
					} else {
						mAdapter.setFiles(mAdapter.getParentFile().getParentFile(), mAdapter.getParentFile());
					}
					
					mAdapter.notifyDataSetChanged();
					mFileListView.setSelection(0);
					return ;
				}

			setResult(RESULT_OK, new Intent().putExtra("choosed_file_path", file.getAbsolutePath()));
			finish();
			}
		};
		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater.from(this).inflate(R.layout.file_explorer, topContentView);
		initView();
		initFileExplorer();
	}

	private void initView() {
		setLeftButton(R.drawable.tt_top_back);
		setLeftText(getResources().getString(R.string.top_left_back));
		setTitle("文件");
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
	}

	/**
	 * 初始化页面
	 */
	private void initFileExplorer() {
		mFileListView = (ListView) findViewById(R.id.file_explorer_list_lv);
		mRootTab = (TextView) findViewById(R.id.root_tab);
		mRootTabSelector = findViewById(R.id.root_tab_selector);
		mSdcardTab = (TextView) findViewById(R.id.sdcard_tab);
		mSdcardTabSelector = findViewById(R.id.sdcard_tab_selector);
		
		mFileExplorerRootTag = getString(R.string.plugin_file_explorer_root_tag);
		mFileExplorerSdcardTag = getString(R.string.plugin_file_explorer_sdcard_tag);
		
		File rootDirectory = Environment.getRootDirectory();
		if(!rootDirectory.canRead()) {
			File dataDirectory = Environment.getDataDirectory();
			if(dataDirectory.canRead()) {
				rootDirectory = dataDirectory;
				mFileExplorerRootTag = dataDirectory.getName();
			}
		}
		mRootFile = rootDirectory;
		
		File externalStorageFile = null;
		if(!FileUtils.checkExternalStorageCanWrite()) {
			File downloadCacheDirectory = Environment.getDownloadCacheDirectory();
			if(downloadCacheDirectory.canRead()) {
				externalStorageFile = downloadCacheDirectory;
				mFileExplorerSdcardTag = downloadCacheDirectory.getName();
			}
		} else {
			externalStorageFile = Environment.getExternalStorageDirectory();
			
		}
		mSdcardFile = externalStorageFile;

		setCurrentTabSelector(DIR_SDCARD);
		mAdapter = new FileListAdapter(this);
		mAdapter.setPath(externalStorageFile.getPath());
		mAdapter.setFiles(externalStorageFile.getParentFile(), externalStorageFile);
		mFileListView.setAdapter(mAdapter);
		mFileListView.setOnItemClickListener(mItemClickListener);
		mRootTab.setOnClickListener(new FileTabClickListener(DIR_ROOT , mRootFile));
		mSdcardTab.setOnClickListener(new FileTabClickListener(DIR_SDCARD , mSdcardFile));
	}
	
	/**
	 * 切换视图
	 * @param type
	 */
	private void setCurrentTabSelector(int type) {
		mType = type;
		if (type == DIR_SDCARD) {
			mSdcardTab.setTextColor(getResources().getColor(R.color.red_btn_color_normal));
			mRootTab.setTextColor(getResources().getColor(R.color.action_bar_tittle_color));
			mRootTabSelector.setVisibility(View.GONE);
			mSdcardTabSelector.setVisibility(View.VISIBLE);
			return;
		}
		mRootTab.setTextColor(getResources().getColor(R.color.red_btn_color_normal));
		mSdcardTab.setTextColor(getResources().getColor(R.color.action_bar_tittle_color));
		mRootTabSelector.setVisibility(View.VISIBLE);
		mSdcardTabSelector.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.left_txt:
			case R.id.left_btn:
			finish();
				break;
			default:
				break;
		}
	}
	
	private class FileTabClickListener implements View.OnClickListener {

		private int type;
		/**当前浏览文件夹*/
		private File mParentFile;
		/**扩展卡根目录*/
		private File mRootPath;
		public FileTabClickListener(int type , File f) {
			this.type = type;
			this.mRootPath = f;
		}
		
		@Override
		public void onClick(View v) {
			mParentFile = (this.type == DIR_SDCARD) ? mSdcardFile : mRootFile;
			setCurrentTabSelector(this.type);
			mAdapter.setPath(mRootPath.getPath());
			mAdapter.setFiles(mParentFile.getParentFile(), mParentFile);
			mAdapter.notifyDataSetInvalidated();
			mAdapter.notifyDataSetChanged();
			mFileListView.setSelection(0);
		}
		
	}
}
