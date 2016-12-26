
package com.dtalk.dd.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.dtalk.dd.R;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.ui.helper.Emoparser;
import com.dtalk.dd.ui.widget.GifLoadTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * @author : fengzi on 15-1-25.
 * @email : fengzi@mogujie.com.
 *
 * preview a GIF image when click on the gif message
 */
public class PreviewGifActivity extends Activity implements View.OnClickListener {
    GifImageView gifView = null;
    ImageView backView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_activity_preview_gif);
        gifView = (GifImageView) findViewById(R.id.gif);
        backView = (ImageView)findViewById(R.id.back_btn);
        backView.setOnClickListener(this);
        String content = getIntent().getStringExtra(IntentConstant.PREVIEW_TEXT_CONTENT);
        if(Emoparser.getInstance(this).isMessageGif(content))
        {
            try {
                GifDrawable gifFromResource = new GifDrawable( getResources(), Emoparser.getInstance(getApplicationContext()).getResIdByCharSequence(content));
                gifView.setImageDrawable(gifFromResource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            new GifLoadTask() {
                @Override
                protected void onPostExecute(byte[] bytes) {
                    try {
                        GifDrawable gifFromBytes = new GifDrawable(bytes);
                        gifView.setImageDrawable(gifFromBytes);
                    } catch (Exception e) {
                    }
                }
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }
            }.execute(content);
        }


    }

    @Override
    public void onClick(View view) {
        PreviewGifActivity.this.finish();
    }
}
