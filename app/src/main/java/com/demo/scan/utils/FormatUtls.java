package com.demo.scan.utils;

import java.text.DecimalFormat;

/**
 * 作者：lish on 2018-11-05 10:59
 * 描述：
 */
public class FormatUtls {
    public static String for2s(String data){
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(data);
    }
}
