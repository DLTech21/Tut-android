package com.dtalk.dd.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

/**
 * String Utils
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2011-7-22
 */
public class StringUtils {

    private StringUtils() {
        throw new AssertionError();
    }

    /**
     * is null or its length is 0 or it is made by space
     * 
     * <pre>
     * isBlank(null) = true;
     * isBlank(&quot;&quot;) = true;
     * isBlank(&quot;  &quot;) = true;
     * isBlank(&quot;a&quot;) = false;
     * isBlank(&quot;a &quot;) = false;
     * isBlank(&quot; a&quot;) = false;
     * isBlank(&quot;a b&quot;) = false;
     * </pre>
     * 
     * @param str
     * @return if string is null or its size is 0 or it is made by space, return true, else return false.
     */
    public static boolean isBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }

    /**
     * is null or its length is 0
     * 
     * <pre>
     * isEmpty(null) = true;
     * isEmpty(&quot;&quot;) = true;
     * isEmpty(&quot;  &quot;) = false;
     * </pre>
     * 
     * @param str
     * @return if string is null or its size is 0, return true, else return false.
     */
    public static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }

    /**
     * compare two string
     * 
     * @param actual
     * @param expected
     * @return
     * @see ObjectUtils#isEquals(Object, Object)
     */
    public static boolean isEquals(String actual, String expected) {
        return ObjectUtils.isEquals(actual, expected);
    }

    /**
     * get length of CharSequence
     * 
     * <pre>
     * length(null) = 0;
     * length(\"\") = 0;
     * length(\"abc\") = 3;
     * </pre>
     * 
     * @param str
     * @return if str is null or empty, return 0, else return {@link CharSequence#length()}.
     */
    public static int length(CharSequence str) {
        return str == null ? 0 : str.length();
    }

    /**
     * null Object to empty string
     * 
     * <pre>
     * nullStrToEmpty(null) = &quot;&quot;;
     * nullStrToEmpty(&quot;&quot;) = &quot;&quot;;
     * nullStrToEmpty(&quot;aa&quot;) = &quot;aa&quot;;
     * </pre>
     * 
     * @param str
     * @return
     */
    public static String nullStrToEmpty(Object str) {
        return (str == null ? "" : (str instanceof String ? (String)str : str.toString()));
    }

    /**
     * capitalize first letter
     * 
     * <pre>
     * capitalizeFirstLetter(null)     =   null;
     * capitalizeFirstLetter("")       =   "";
     * capitalizeFirstLetter("2ab")    =   "2ab"
     * capitalizeFirstLetter("a")      =   "A"
     * capitalizeFirstLetter("ab")     =   "Ab"
     * capitalizeFirstLetter("Abc")    =   "Abc"
     * </pre>
     * 
     * @param str
     * @return
     */
    public static String capitalizeFirstLetter(String str) {
        if (isEmpty(str)) {
            return str;
        }

        char c = str.charAt(0);
        return (!Character.isLetter(c) || Character.isUpperCase(c)) ? str : new StringBuilder(str.length())
                .append(Character.toUpperCase(c)).append(str.substring(1)).toString();
    }

    /**
     * encoded in utf-8
     * 
     * <pre>
     * utf8Encode(null)        =   null
     * utf8Encode("")          =   "";
     * utf8Encode("aa")        =   "aa";
     * utf8Encode("啊啊啊啊")   = "%E5%95%8A%E5%95%8A%E5%95%8A%E5%95%8A";
     * </pre>
     * 
     * @param str
     * @return
     * @throws UnsupportedEncodingException if an error occurs
     */
    public static String utf8Encode(String str) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
            }
        }
        return str;
    }

    /**
     * encoded in utf-8, if exception, return defultReturn
     * 
     * @param str
     * @param defultReturn
     * @return
     */
    public static String utf8Encode(String str, String defultReturn) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return defultReturn;
            }
        }
        return str;
    }

    /**
     * get innerHtml from href
     * 
     * <pre>
     * getHrefInnerHtml(null)                                  = ""
     * getHrefInnerHtml("")                                    = ""
     * getHrefInnerHtml("mp3")                                 = "mp3";
     * getHrefInnerHtml("&lt;a innerHtml&lt;/a&gt;")                    = "&lt;a innerHtml&lt;/a&gt;";
     * getHrefInnerHtml("&lt;a&gt;innerHtml&lt;/a&gt;")                    = "innerHtml";
     * getHrefInnerHtml("&lt;a&lt;a&gt;innerHtml&lt;/a&gt;")                    = "innerHtml";
     * getHrefInnerHtml("&lt;a href="baidu.com"&gt;innerHtml&lt;/a&gt;")               = "innerHtml";
     * getHrefInnerHtml("&lt;a href="baidu.com" title="baidu"&gt;innerHtml&lt;/a&gt;") = "innerHtml";
     * getHrefInnerHtml("   &lt;a&gt;innerHtml&lt;/a&gt;  ")                           = "innerHtml";
     * getHrefInnerHtml("&lt;a&gt;innerHtml&lt;/a&gt;&lt;/a&gt;")                      = "innerHtml";
     * getHrefInnerHtml("jack&lt;a&gt;innerHtml&lt;/a&gt;&lt;/a&gt;")                  = "innerHtml";
     * getHrefInnerHtml("&lt;a&gt;innerHtml1&lt;/a&gt;&lt;a&gt;innerHtml2&lt;/a&gt;")        = "innerHtml2";
     * </pre>
     * 
     * @param href
     * @return <ul>
     *         <li>if href is null, return ""</li>
     *         <li>if not match regx, return source</li>
     *         <li>return the last string that match regx</li>
     *         </ul>
     */
    public static String getHrefInnerHtml(String href) {
        if (isEmpty(href)) {
            return "";
        }

        String hrefReg = ".*<[\\s]*a[\\s]*.*>(.+?)<[\\s]*/a[\\s]*>.*";
        Pattern hrefPattern = Pattern.compile(hrefReg, Pattern.CASE_INSENSITIVE);
        Matcher hrefMatcher = hrefPattern.matcher(href);
        if (hrefMatcher.matches()) {
            return hrefMatcher.group(1);
        }
        return href;
    }

/**
     * process special char in html
     * 
     * <pre>
     * htmlEscapeCharsToString(null) = null;
     * htmlEscapeCharsToString("") = "";
     * htmlEscapeCharsToString("mp3") = "mp3";
     * htmlEscapeCharsToString("mp3&lt;") = "mp3<";
     * htmlEscapeCharsToString("mp3&gt;") = "mp3\>";
     * htmlEscapeCharsToString("mp3&amp;mp4") = "mp3&mp4";
     * htmlEscapeCharsToString("mp3&quot;mp4") = "mp3\"mp4";
     * htmlEscapeCharsToString("mp3&lt;&gt;&amp;&quot;mp4") = "mp3\<\>&\"mp4";
     * </pre>
     * 
     * @param source
     * @return
     */
    public static String htmlEscapeCharsToString(String source) {
        return StringUtils.isEmpty(source) ? source : source.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
    }

    /**
     * transform half width char to full width char
     * 
     * <pre>
     * fullWidthToHalfWidth(null) = null;
     * fullWidthToHalfWidth("") = "";
     * fullWidthToHalfWidth(new String(new char[] {12288})) = " ";
     * fullWidthToHalfWidth("！＂＃＄％＆) = "!\"#$%&";
     * </pre>
     * 
     * @param s
     * @return
     */
    public static String fullWidthToHalfWidth(String s) {
        if (isEmpty(s)) {
            return s;
        }

        char[] source = s.toCharArray();
        for (int i = 0; i < source.length; i++) {
            if (source[i] == 12288) {
                source[i] = ' ';
                // } else if (source[i] == 12290) {
                // source[i] = '.';
            } else if (source[i] >= 65281 && source[i] <= 65374) {
                source[i] = (char)(source[i] - 65248);
            } else {
                source[i] = source[i];
            }
        }
        return new String(source);
    }

    /**
     * transform full width char to half width char
     * 
     * <pre>
     * halfWidthToFullWidth(null) = null;
     * halfWidthToFullWidth("") = "";
     * halfWidthToFullWidth(" ") = new String(new char[] {12288});
     * halfWidthToFullWidth("!\"#$%&) = "！＂＃＄％＆";
     * </pre>
     * 
     * @param s
     * @return
     */
    public static String halfWidthToFullWidth(String s) {
        if (isEmpty(s)) {
            return s;
        }

        char[] source = s.toCharArray();
        for (int i = 0; i < source.length; i++) {
            if (source[i] == ' ') {
                source[i] = (char)12288;
                // } else if (source[i] == '.') {
                // source[i] = (char)12290;
            } else if (source[i] >= 33 && source[i] <= 126) {
                source[i] = (char)(source[i] + 65248);
            } else {
                source[i] = source[i];
            }
        }
        return new String(source);
    }
    
    private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    private final static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat dateFormater2 = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * 将php时间戳转位日期类型
     * @param sdate
     * @return
     */
    public static String phpLongtoDate(String sdate){
        try{
            Date date = new Date(Long.valueOf(sdate)*1000);
            return dateFormater.format(date);
        }catch (Exception e) {
            return null;
        }
    }
    
    public static String phpLongtoDate(String sdate, SimpleDateFormat format){
        try{
            Date date = new Date(Long.valueOf(sdate)*1000);
            return format.format(date);
        }catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 将字符串转位日期类型
     * @param sdate
     * @return
     */
    public static Date toDate(String sdate) {
        try {
            return dateFormater.parse(sdate);
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * 以友好的方式显示时间
     * @param sdate
     * @return
     */
    public static String friendly_time(String sdate) {
        Date time = toDate(sdate);
        if(time == null) {
            return "Unknown";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();
        
        //判断是否是同一天
        String curDate = dateFormater2.format(cal.getTime());
        String paramDate = dateFormater2.format(time);
        if(curDate.equals(paramDate)){
            int hour = (int)((cal.getTimeInMillis() - time.getTime())/3600000);
            if(hour == 0)
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000,1)+"分钟前";
            else 
                ftime = hour+"小时前";
            return ftime;
        }
        
        long lt = time.getTime()/86400000;
        long ct = cal.getTimeInMillis()/86400000;
        int days = (int)(ct - lt);      
        if(days == 0){
            int hour = (int)((cal.getTimeInMillis() - time.getTime())/3600000);
            if(hour == 0)
                ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000,1)+"分钟前";
            else 
                ftime = hour+"小时前";
        }
        else if(days == 1){
            ftime = "昨天";
        }
        else if(days == 2){
            ftime = "前天";
        }
        else if(days > 2 && days <= 10){ 
            ftime = days+"天前";          
        }
        else if(days >= 1){         
            ftime = dateFormater.format(time);
        }
        return ftime;
    }
    
    /**
     * 判断给定字符串时间是否为今日
     * @param sdate
     * @return boolean
     */
    public static boolean isToday(String sdate){
        boolean b = false;
        Date time = toDate(sdate);
        Date today = new Date();
        if(time != null){
            String nowDate = dateFormater2.format(today);
            String timeDate = dateFormater2.format(time);
            if(nowDate.equals(timeDate)){
                b = true;
            }
        }
        return b;
    }
    
    public static boolean isURL(String url) {
        if (empty(url)) {
            return false;
        }
        Pattern urlPattern = Pattern.compile("[http|https]+[://]+[0-9A-Za-z:/[-]_#[?][=][.][&]]*");
        return urlPattern.matcher(url).matches();
    }
    
    /**
     * 判断是不是一个合法的电子邮件地址
     * @param email
     * @return
     */
    public static boolean isEmail(String email){
        if(email == null || email.trim().length()==0) 
            return false;
        return emailer.matcher(email).matches();
    }
    
    /**
     * 去掉字符串的首尾空格
     * @param str
     * @return
     */
    public static String spaceEdit(String str){
            int start=0,end=str.length()-1;
            while(start<=end && str.charAt(start)==' ')
                    start++;
            while(start<=end && str.charAt(end)==' ')
                    end--;
            return str.substring(start,end+1);
    }
    
    /**
     * 判断是不是一个合法的国内手机号码
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles){
        String regex = "^(((86|\\+86|0|)1[345678][0-9]{9})|(\\d{3,4}-(\\d{7,8})))$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
    
    /**
     * 判断是不是中文地址
     * @param address
     * @return
     */
    public static boolean isAddress(String address) {
        String regex = "^[\u4e00-\u9fa5]{2}[\\w]*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(address);
        return m.matches();
    }
    
    /**
     * 判断是不是中文
     * @param text
     * @return
     */
    public static boolean isChinese(String text) {
        String regex = "^[\u4e00-\u9fa5]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        if (m.matches()) {
            return true;
        }
        return false;
    }
    
    /**
     * 判断是不是中文姓名
     * @param name
     * @return
     */
    public static boolean isChineseName(String name) {
        String regex = "^[\u4e00-\u9fa5]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(name);
        if (m.matches()) {
            String surnames = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻柏水窦章云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳酆鲍史唐费廉岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅皮卞齐康伍余元卜顾孟平黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计伏成戴谈宋茅庞熊纪舒屈项祝董梁杜阮蓝闵席季麻强贾路娄危江童颜郭梅盛林刁锺徐邱骆高夏蔡田樊胡凌霍虞万支柯昝管卢莫经房裘缪干解应宗丁宣贲邓郁单杭洪包诸左石崔吉钮龚程嵇邢滑裴陆荣翁荀羊於惠甄麴家封芮羿储靳汲邴糜松井段富巫乌焦巴弓牧隗山谷车侯宓蓬全郗班仰秋仲伊宫宁仇栾暴甘钭历戎祖武符刘景詹束龙叶幸司韶郜黎蓟溥印宿白怀蒲邰从鄂索咸籍赖卓蔺屠蒙池乔阳郁胥能苍双闻莘党翟谭贡劳逄姬申扶堵冉宰郦雍却璩桑桂濮牛寿通边扈燕冀僪浦尚农温别庄晏柴瞿阎充慕连茹习宦艾鱼容向古易慎戈廖庾终暨居衡步都耿满弘匡国文寇广禄阙东欧殳沃利蔚越夔隆师巩厍聂晁勾敖融冷訾辛阚那简饶空曾毋沙乜养鞠须丰巢关蒯相查后荆红游竺权逮盍益桓公万俟司马上官欧阳夏侯诸葛闻人东方赫连皇甫尉迟公羊澹台公冶宗政濮阳淳于单于太叔申屠公孙仲孙轩辕令狐钟离宇文长孙慕容司徒司空召有舜叶赫那拉丛岳寸贰皇侨彤竭端赫实甫集象翠狂辟典良函芒苦其京中夕之章佳那拉冠宾香果依尔根觉罗依尔觉罗萨嘛喇赫舍里额尔德特萨克达钮祜禄他塔喇喜塔腊讷殷富察叶赫那兰库雅喇瓜尔佳舒穆禄爱新觉罗索绰络纳喇乌雅范姜碧鲁张廖张简图门太史公叔乌孙完颜马佳佟佳富察费莫蹇称诺来多繁戊朴回毓税荤靖绪愈硕牢买但巧枚撒泰秘亥绍以壬森斋释奕姒朋求羽用占真穰翦闾漆贵代贯旁崇栋告休褒谏锐皋闳在歧禾示是委钊频嬴呼大威昂律冒保系抄定化莱校么抗祢綦悟宏功庚务敏捷拱兆丑丙畅苟随类卯俟友答乙允甲留尾佼玄乘裔延植环矫赛昔侍度旷遇偶前由咎塞敛受泷袭衅叔圣御夫仆镇藩邸府掌首员焉戏可智尔凭悉进笃厚仁业肇资合仍九衷哀刑俎仵圭夷徭蛮汗孛乾帖罕洛淦洋邶郸郯邗邛剑虢隋蒿茆菅苌树桐锁钟机盘铎斛玉线针箕庹绳磨蒉瓮弭刀疏牵浑恽势世仝同蚁止戢睢冼种涂肖己泣潜卷脱谬蹉赧浮顿说次错念夙斯完丹表聊源姓吾寻展出不户闭才无书学愚本性雪霜烟寒少字桥板斐独千诗嘉扬善揭祈析赤紫青柔刚奇拜佛陀弥阿素长僧隐仙隽宇祭酒淡塔琦闪始星南天接波碧速禚腾潮镜似澄潭謇纵渠奈风春濯沐茂英兰檀藤枝检生折登驹骑貊虎肥鹿雀野禽飞节宜鲜粟栗豆帛官布衣藏宝钞银门盈庆喜及普建营巨望希道载声漫犁力贸勤革改兴亓睦修信闽北守坚勇汉练尉士旅五令将旗军行奉敬恭仪母堂丘义礼慈孝理伦卿问永辉位让尧依犹介承市所苑杞剧第零谌招续达忻六鄞战迟候宛励粘萨邝覃辜初楼城区局台原考妫纳泉老清德卑过麦曲竹百福言第五佟爱年笪谯哈墨南宫赏伯佴佘牟商西门东门左丘梁丘琴后况亢缑帅微生羊舌海归呼延南门东郭百里钦鄢汝法闫楚晋谷梁宰父夹谷拓跋壤驷乐正漆雕公西巫马端木颛孙子车督仉司寇亓官鲜于锺离盖逯库郏逢阴薄厉稽闾丘公良段干开光操瑞眭泥运摩伟铁迮付";
            if (surnames.indexOf(name.substring(0, 1)) != -1) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断是不是一个合法的momoka's pn
     * @param pn
     * @return
     */
    public static boolean isPN(String pn){
        pn = pn.replace(" ", "");
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(pn);
        return m.matches();
    }
    
    /**
     * 全角转半角
     * @param input
     * @return
     */
    public static String ToDBC(String input) {   
        char[] c = input.toCharArray();   
        for (int i = 0; i < c.length; i++) {   
            if (c[i] == 12288) {   
                c[i] = (char) 32;   
                continue;   
            }   
            if (c[i] > 65280 && c[i] < 65375)   
                c[i] = (char) (c[i] - 65248);   
        }   
        return new String(c);   
    } 
    
    /**
     * 半角转全角
     * @param input
     * @return
     *//*
    public static String ToSBC(String input) {
          char[] c = input.toCharArray();
          for (int i = 0; i < c.length; i++) {
           if (c[i] == 32) {
            c[i] = (char) 12288;
            continue;
           }
           if (c[i] < 127)
            c[i] = (char) (c[i] + 65248);
          }
          return new String(c);
    }*/
    
    /**
     * 字符串转整数
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try{
            return Integer.parseInt(str);
        }catch(Exception e){}
        return defValue;
    }
    
    /**
     * 对象转整数
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if(obj==null) return 0;
        return toInt(obj.toString(),0);
    }
    
    /**
     * 对象转整数
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try{
            return Long.parseLong(obj);
        }catch(Exception e){}
        return 0;
    }
    
    /**
     * 字符串转布尔值
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try{
            return Boolean.parseBoolean(b);
        }catch(Exception e){}
        return false;
    }
    
    
    /**
     * 控制TextView的显示行数
     * @param view
     * @param maxLine
     */
    public static void truncate(final TextView view, final int maxLine) {
       ViewTreeObserver vto = view.getViewTreeObserver();
       vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
           public void onGlobalLayout() {
               if (view.getLineCount() > maxLine) {
                   int lineEndIndex = view.getLayout().getLineEnd(maxLine - 1);
                   String text = view.getText().subSequence(0,
                           lineEndIndex - 3)
                           + "...";
                   view.setText(text);
               }
           }
       });
   }
    
    public static String unescape (String s)
    {
        while (true)
        {
            int n=s.indexOf("&#");
            if (n<0) break;
            int m=s.indexOf(";",n+2);
            if (m<0) break;
            try
            {
                s=s.substring(0,n)+(char)(Integer.parseInt(s.substring(n+2,m)))+
                s.substring(m+1);
            }
            catch (Exception e)
            {
                return s;
            }
        }
        s=s.replace("&quot;","\"");
        s=s.replace("&lt;","<");
        s=s.replace("&gt;","<");
        s=s.replace("&amp;","&");
        s=s.replace("&nbsp;"," ");
        return s;
    }
    
    public static String unescapeSpace(String s) {
        return s.replaceAll("&nbsp;", "");
    }
    
    public static String getAlpha(String str) {
        if (str == null) {
            return "~";
        }
        if (str.trim().length() == 0) {
            return "~";
        }
        char c = str.trim().substring(0, 1).charAt(0);
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");
        if (pattern.matcher(c + "").matches()) {
            return (c + "").toUpperCase();
        } else {
            return "~";
        }
    }
    
    
    /**
     * 处理空字符串
     * 
     * @param str
     * @return String
     */
    public static String doEmpty(String str) {
        return doEmpty(str, "");
    }

    /**
     * 处理空字符串
     * 
     * @param str
     * @param defaultValue
     * @return String
     */
    public static String doEmpty(String str, String defaultValue) {
        if (str == null || str.equalsIgnoreCase("null")
                || str.trim().equals("") || str.trim().equals("－请选择－")) {
            str = defaultValue;
        } else if (str.startsWith("null")) {
            str = str.substring(4, str.length());
        }
        return str.trim();
    }

    /**
     * 请选择
     */
    final static String PLEASE_SELECT = "请选择...";

    public static boolean notEmpty(Object o) {
        return o != null && !"".equals(o.toString().trim())
                && !"null".equalsIgnoreCase(o.toString().trim())
                && !"undefined".equalsIgnoreCase(o.toString().trim())
                && !PLEASE_SELECT.equals(o.toString().trim());
    }

    public static boolean empty(Object o) {
        return o == null || "".equals(o.toString().trim())
                || "null".equalsIgnoreCase(o.toString().trim())
                || "undefined".equalsIgnoreCase(o.toString().trim())
                || PLEASE_SELECT.equals(o.toString().trim());
    }

    public static boolean num(Object o) {
        int n = 0;
        try {
            n = Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (n > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean decimal(Object o) {
        double n = 0;
        try {
            n = Double.parseDouble(o.toString().trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (n > 0.0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 给JID返回用户名
     * 
     * @param Jid
     * @return
     */
    public static String getUserNameByJid(String Jid) {
        if (empty(Jid)) {
            return null;
        }
        if (!Jid.contains("@")) {
            return Jid;
        }
        return Jid.split("@")[0];
    }

    /**
     * 给用户名返回JID
     * 
     * @param jidFor
     *            域名//如ahic.com.cn
     * @param userName
     * @return
     */
    public static String getJidByName(String userName, String jidFor) {
        if (empty(jidFor) || empty(jidFor)) {
            return null;
        }
        return userName + "@" + jidFor;
    }

    /**
     * 给用户名返回JID,使用默认域名ahic.com.cn
     * 
     * @param userName
     * @return
     */
    public static String getJidByName(String userName) {
        String jidFor = "ahic.com.cn";
        return getJidByName(userName, jidFor);
    }

    /**
     * 根据给定的时间字符串，返回月 日 时 分 秒
     * 
     * @param allDate
     *            like "yyyy-MM-dd hh:mm:ss SSS"
     * @return
     */
    public static String getMonthTomTime(String allDate) {
        return allDate.substring(5, 19);
    }

    /**
     * 根据给定的时间字符串，返回月 日 时 分 月到分钟
     * 
     * @param allDate
     *            like "yyyy-MM-dd hh:mm:ss SSS"
     * @return
     */
    public static String getMonthTime(String allDate) {
        return allDate.substring(5, 16);
    }
}