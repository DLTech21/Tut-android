package com.dtalk.dd.ui.plugin;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dtalk.dd.R;

/**
 * Created by Donal on 16/11/4.
 */

public class ImageLoadManager {

    public static void setImageGlide(Context context, String PicURL, ImageView mImageView) {
        Glide.with(context).load(PicURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .placeholder(R.drawable.tt_message_image_default)
                .error(R.drawable.tt_default_image_error)
                .into(mImageView);
    }

    public static void setCircleGlide(Context context, String PicURL, ImageView mImageView) {
        Glide.with(context).load(PicURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .placeholder(R.drawable.tt_default_album_grid_image)
                .error(R.drawable.tt_default_image_error)
                .into(mImageView);
    }

    public static void setCirclePubGlide(Context context, String PicURL, ImageView mImageView) {
        Glide.with(context).load(PicURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.tt_default_album_grid_image)
                .error(R.drawable.tt_default_image_error)
                .crossFade()
                .into(mImageView);
    }

    public static void setDrawableGlide(Context context, int drawable, ImageView mImageView) {
        Glide.with(context).load(drawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(drawable)
                .crossFade()
                .into(mImageView);
    }
}
