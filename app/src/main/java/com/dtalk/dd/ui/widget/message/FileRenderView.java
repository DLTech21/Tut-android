package com.dtalk.dd.ui.widget.message;

import android.content.Context;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.dtalk.dd.DB.entity.MessageEntity;
import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.imservice.entity.FileEntity;
import com.dtalk.dd.imservice.entity.FileMessage;
import com.dtalk.dd.ui.helper.Emoparser;

/**
 * Created by Donal on 16/4/22.
 */
public class FileRenderView extends  BaseMsgRenderView {
    /** 文字消息体 */
    private TextView messageContent;

    public static FileRenderView inflater(Context context,ViewGroup viewGroup,boolean isMine){
        int resource = isMine? R.layout.tt_mine_file_message_item:R.layout.tt_other_file_message_item;

        FileRenderView  fileRenderView = (FileRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        fileRenderView.setMine(isMine);
        fileRenderView.setParentView(viewGroup);
        return fileRenderView;
    }

    public FileRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        messageContent = (TextView) findViewById(R.id.message_content);
    }


    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(MessageEntity messageEntity, UserEntity userEntity,Context context) {
        super.render(messageEntity, userEntity,context);
        FileMessage fileMessage = (FileMessage) messageEntity;
        // 按钮的长按也是上层设定的
        // url 路径可以设定 跳转哦哦
        String content = fileMessage.getContent();
        FileEntity fileEntity = new Gson().fromJson(content, FileEntity.class);
        messageContent.setText(Emoparser.getInstance(getContext()).emoCharsequence(fileEntity.getFileName())); // 所以上层还是处理好之后再给我 Emoparser 处理之后的
        extractUrl2Link(messageContent);

    }
    private static final String SCHEMA ="com.mogujie.tt://message_private_url";
    private static final String PARAM_UID ="uid";
    private String urlRegex = "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+(?:(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])|(?:biz|b[abdefghijmnorstvwyz])|(?:cat|com|coop|c[acdfghiklmnoruvxyz])|d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]|(?:gov|g[abdefghilmnpqrstuwy])|h[kmnrtu]|(?:info|int|i[delmnoqrst])|(?:jobs|j[emop])|k[eghimnrwyz]|l[abcikrstuvy]|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])|(?:name|net|n[acefgilopruz])|(?:org|om)|(?:pro|p[aefghklmnrstwy])|qa|r[eouw]|s[abcdeghijklmnortuvyz]|(?:tel|travel|t[cdfghjklmnoprtvwz])|u[agkmsyz]|v[aceginu]|w[fs]|y[etu]|z[amw]))|(?:(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])))(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?(?:\\b|$)";

    private void extractUrl2Link(TextView v) {
        java.util.regex.Pattern wikiWordMatcher = java.util.regex.Pattern.compile(urlRegex);
        String mentionsScheme = String.format("%s/?%s=",SCHEMA, PARAM_UID);
        Linkify.addLinks(v, wikiWordMatcher, mentionsScheme);
    }

    @Override
    public void msgFailure(MessageEntity messageEntity) {
        super.msgFailure(messageEntity);
    }

    /**----------------set/get---------------------------------*/
    public TextView getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(TextView messageContent) {
        this.messageContent = messageContent;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }

    public ViewGroup getParentView() {
        return parentView;
    }

    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }
}
