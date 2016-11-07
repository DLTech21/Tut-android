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
    private RequestManager glideRequest;
    private static ImageLoadManager imageLoadManager;

    public ImageLoadManager(Context context) {
        if (glideRequest == null) {
            glideRequest = Glide.with(context);
        }
    }

    public static ImageLoadManager getInstance(Context context) {
        synchronized (ImageLoadManager.class) {
            if (imageLoadManager == null) {
                imageLoadManager = new ImageLoadManager(context);
            }
            return imageLoadManager;
        }
    }

    public void setImageGlide(String PicURL, ImageView mImageView) {
        glideRequest.load(PicURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .placeholder(R.drawable.tt_message_image_default)
                .error(R.drawable.tt_default_image_error)
                .into(mImageView);
    }

    public void setCircleGlide(String PicURL, ImageView mImageView) {
        glideRequest.load(PicURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .placeholder(R.drawable.tt_default_album_grid_image)
                .error(R.drawable.tt_default_image_error)
                .into(mImageView);
    }

    public void setCirclePubGlide(String PicURL, ImageView mImageView) {
        glideRequest.load(PicURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.tt_default_album_grid_image)
                .error(R.drawable.tt_default_image_error)
                .dontAnimate()
                .into(mImageView);
    }

    public void setDrawableGlide(int drawable, ImageView mImageView) {
        glideRequest.load(drawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(drawable)
                .dontAnimate()
                .into(mImageView);
    }
}
