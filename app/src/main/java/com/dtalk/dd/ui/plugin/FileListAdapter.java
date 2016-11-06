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

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.dtalk.dd.R;
import com.dtalk.dd.utils.CnToCharUntil;
import com.dtalk.dd.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-30
 * @version 4.0
 */
public class FileListAdapter extends BaseAdapter {

	private static final String TAG = "ECDemo.FileListAdapter";
	private Context mContext;
	/**上一级目录*/
	private File mParentFile;
	/**当前浏览目录*/
	private File mCurrentFile;
	/**文件浏览根目录*/
	private String mRootDirectory;
	/**文件列表数据*/
	private File[] mFiles;
	
	public FileListAdapter(Context ctx) {
		mContext  = ctx;
	}
	
	public void setPath(String path) {
		mRootDirectory = path;
	}
	
	/**
	 * 设置浏览目录
	 * @param parent
	 * @param sub
	 */
	public void setFiles(File parent ,File sub) {
		mParentFile = parent;
		if(sub.getAbsolutePath().equalsIgnoreCase(mRootDirectory)){
			// 如果是根目录，则不显示返回上一级按钮
			mParentFile = null;
		}
		mCurrentFile = sub;
		if(mCurrentFile.canRead()) {
			mFiles = mCurrentFile.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return !pathname.isHidden();
				}
			});
			
		}
		if(mFiles.length > 0) {
			ArrayList<FileItem> spellDirectoty = new ArrayList<FileItem>();
			ArrayList<FileItem> spellFile = new ArrayList<FileItem>();
			for(int i = 0 ; i < mFiles.length ; i++) {
				File file = mFiles[i];
				FileItem fileItem = new FileItem();
				fileItem.file = file;
				fileItem.spell = CnToCharUntil.getSpell(file.getName().toUpperCase(), false);
				
				if(file.isDirectory()) {
					spellDirectoty.add(fileItem);
				} else {
					spellFile.add(fileItem);
				}
			}
			Collections.sort(spellDirectoty, new Comparator<FileItem>() {

				@Override
				public int compare(FileItem lhs, FileItem rhs) {

					return lhs.spell.compareTo(rhs.spell);
				}
			});
			Collections.sort(spellFile, new Comparator<FileItem>() {

				@Override
				public int compare(FileItem lhs, FileItem rhs) {

					return lhs.spell.compareTo(rhs.spell);
				}
			});
			int index = 0;
			for(FileItem f : spellDirectoty) {
				mFiles[index] = f.file;
				index ++;
			}
			for(FileItem f : spellFile) {
				mFiles[index] = f.file;
				index ++;
			}
		}
	}
	
	@Override
	public int getCount() {
		if(mFiles == null) {
			return 0;
		}
		int length = mFiles.length;
		File f = mParentFile;
		if(f != null) {
			length += 1;
		}
		return length;
	}

	@Override
	public Object getItem(int position) {
		if(mParentFile != null && position == 0) {
			// 如果第一个 返回上一级目录Item
			return mParentFile;
		}
		if(mParentFile != null) {
			--position;
		}
		return mFiles[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder mViewHolder = null;
		if(convertView == null || convertView.getTag() == null) {
			view = View.inflate(mContext, R.layout.file_explorer_item, null);

			mViewHolder = new ViewHolder();
			mViewHolder.mFileIcon = (ImageView) view.findViewById(R.id.file_icon_iv);
			mViewHolder.mFileName = (TextView) view.findViewById(R.id.file_name_tv);
			mViewHolder.mFileSummary = (TextView) view.findViewById(R.id.file_summary_tv);
			
			view.setTag(mViewHolder);
		} else {
			view = convertView;
			mViewHolder = (ViewHolder) view.getTag();
		}
		
		File file = (File) getItem(position);
		if(file == mParentFile) {
			mViewHolder.mFileIcon.setImageResource(R.drawable.im_attach_back);
		} else {
			if(file.isDirectory()) {
				mViewHolder.mFileIcon.setImageResource(R.drawable.file_attach_folder);
			} else {
				mViewHolder.mFileIcon.setImageResource(FileUtils.getFileIcon(file.getName()));
			}
		}
		mViewHolder.mFileName.setText(file.getName());
		StringBuilder sb = new StringBuilder().append(DateFormat.format(
				"yyyy-MM-dd hh:mm:ss", file.lastModified()).toString());
		if(!file.isDirectory()) {
			sb.append("  " + FileUtils.formatFileLength(file.length()));
		}
		mViewHolder.mFileSummary.setText(sb.toString());
		return view;
	}
	
	public File getCurrentFile() {
		return mCurrentFile;
	}
	
	
	/**
	 * @return the mParentFile
	 */
	public File getParentFile() {
		return mParentFile;
	}




	class ViewHolder {
		/**文件图标*/
		ImageView mFileIcon;
		/**文件名称*/
		TextView mFileName;
		/**文件概要*/
		TextView mFileSummary;
		
	}

	
	
	public class FileItem {
		File file;
		String spell;
	}
}
