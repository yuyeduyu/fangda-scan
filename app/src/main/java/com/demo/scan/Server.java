package com.demo.scan;

/**
 * Created by Administrator on 16-2-28.
 */
public class Server {
    //常用服务器地址
    public static final String admin_server = "121.41.94.42";//方大服务器 端口8080
//    public static final String admin_server = "47.105.89.196";
//    public static final String admin_server = "192.168.168.132";
    public static final String admin_port = "8080";
    //备用地址
    public static final String admin_server_spare = "172.16.3.131";

    public static final String serveradress = "/fdsc-1.0-SNAPSHOT";
//    public static final String serveradress = "/fdsc";

    public static String filePath = "/mnt/sdcard/方大丝绸/";

    public static String appVersionByServer = "scanApp.txt";//服务器中获取app版本内容信息
    public static String appName = "scanApp.apk";//服务器中获取app下载路径
}
