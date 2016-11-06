package com.dtalk.dd.voip;

import android.util.SparseIntArray;

import com.dtalk.dd.R;


/**
 * com.yuntongxun.ecdemo.ui.voip in ECDemo_Android
 * Created by Jorstin on 2015/7/6.
 */
public class CallFailReason {

    private static final SparseIntArray sReasonsMap = new SparseIntArray();

    static {
        // 对方拒绝您的呼叫请求（对方主观拒绝通话）
        sReasonsMap.put(175603 , R.string.ec_voip_calling_refuse);
        // 对方不在线
        sReasonsMap.put(175404 , R.string.ec_voip_calling_notfound);
        // 呼叫超时
        sReasonsMap.put(175408 , R.string.ec_voip_calling_timeout);
        // 无人应答
        sReasonsMap.put(175409 , R.string.ec_voip_calling_no_answer);
        // 对方正忙(对方非主动拒接)
        sReasonsMap.put(175486 , R.string.ec_voip_calling_busy);
        // 媒体协商失败(有可能初始化音频失败)
        sReasonsMap.put(175488 , R.string.ec_voip_call_error);
        // 第三方鉴权地址连接失败
        sReasonsMap.put(175700 , R.string.ec_voip_call_fail_connection_failed_auth);
        // 第三方应用ID未找到
        sReasonsMap.put(175702 , R.string.ec_voip_call_fail_not_find_appid);
        // 第三方未上线应用仅限呼叫已配置测试号码
        sReasonsMap.put(175704 , R.string.ec_voip_call_fail_not_online_only_call);
        // 第三方鉴权失败，子账号余额不足
        sReasonsMap.put(175705 , R.string.ec_voip_call_auth_failed);
        // 呼入会议号已解散不存在
        sReasonsMap.put(175707 , R.string.ec_meeting_not_exist);
        // 呼入会议号密码验证失败
        sReasonsMap.put(175708 , R.string.ec_meeting_pass_error);
        // 第三方主账号余额不足
        sReasonsMap.put(175710 , R.string.ec_voip_call_fail_no_pay_account);
    }

    /**
     * 根据呼叫错误码查找原因
     * @param reason 呼叫错误码
     * @return
     */
    public static int getCallFailReason(int reason) {
        if(sReasonsMap == null || sReasonsMap.indexOfKey(reason) < 0) {
            return R.string.ec_voip_call_fail;
        }
        return sReasonsMap.get(reason);
    }
}
