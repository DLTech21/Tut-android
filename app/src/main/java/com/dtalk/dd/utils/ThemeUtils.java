package com.dtalk.dd.utils;

import com.dtalk.dd.R;
import com.dtalk.dd.app.IMApplication;


public class ThemeUtils {


    public static int[][] themeColorArr = {
            {R.color.md_kaigongbao_bg, R.color.md_kaigongbao_bg},
            { R.color.md_red_500, R.color.md_red_700 },
            { R.color.md_pink_500, R.color.md_pink_700 },
            { R.color.md_purple_500, R.color.md_purple_700 },
            { R.color.md_deep_purple_500, R.color.md_deep_purple_700 },
            { R.color.md_indigo_500, R.color.md_indigo_700 },
            { R.color.md_blue_500, R.color.md_blue_700 },
            { R.color.md_light_blue_500, R.color.md_light_blue_700 },
            { R.color.md_cyan_500, R.color.md_cyan_700 },
            { R.color.md_teal_500, R.color.md_teal_500 },
            { R.color.md_green_500, R.color.md_green_500 },
            { R.color.md_light_green_500, R.color.md_light_green_500 },
            { R.color.md_lime_500, R.color.md_lime_700 },
            { R.color.md_yellow_500, R.color.md_yellow_700 },
            { R.color.md_amber_500, R.color.md_amber_700 },
            { R.color.md_orange_500, R.color.md_orange_700 },
            { R.color.md_deep_orange_500, R.color.md_deep_orange_700 },
            { R.color.md_brown_500, R.color.md_brown_700 },
            { R.color.md_grey_500, R.color.md_grey_700 },
            { R.color.md_blue_grey_500, R.color.md_blue_grey_700 }
    };

        public static int getThemeColor() {
                return IMApplication.getInstance().getResources().getColor(themeColorArr[2][0]);
        }
}
