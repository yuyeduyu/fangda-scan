package com.demo.scan.bean;

import android.support.annotation.NonNull;
import android.util.Log;

import com.demo.scan.utils.TimeUtils;

import java.text.ParseException;

/**
 * 作者：lish on 2018-10-31 9:50
 * 描述：
 */
public class OrderBean implements Comparable<OrderBean>{
    // [{"name":"140/弹力缎16","orderNumber":"6.000","riceNumber":0.000,"orderTime":"Aug 20, 2015 12:00:00 AM"
// ,"shippingDate":"Oct 31, 2018 12:00:00 AM"}
//[{\"name\":\"140/素绉缎16\",\"orderNumber\":\"4.000\",\"color\":\"43#\",\"riceNumber\":0.000,\"orderTime\":\"Jul 30, 2015 12:00:00 AM\"" +
//            ",\"shippingDate\":\"May 9, 2019 12:00:00 AM\",\"factroy\":\"\"},
//    面料名称      进厂日期      匹数       进厂米数      出货日期
    private String name;
    private String orderTime;
    private double orderNumber;
    private double riceNumber;
    private String shippingDate;
    private String factroy;
    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFactroy() {
        return factroy;
    }

    public void setFactroy(String factroy) {
        this.factroy = factroy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public double getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(double orderNumber) {
        this.orderNumber = orderNumber;
    }

    public double getRiceNumber() {
        return riceNumber;
    }

    public void setRiceNumber(double riceNumber) {
        this.riceNumber = riceNumber;
    }

    public String getShippingDate() {
        return shippingDate;
    }

    public void setShippingDate(String shippingDate) {
        this.shippingDate = shippingDate;
    }

    @Override
    public int compareTo(@NonNull OrderBean o) {
        try {
          /*  Log.e("paixue",(int) -(TimeUtils.dateToStamp(TimeUtils.parseTime1(orderTime))
                    -TimeUtils.dateToStamp(TimeUtils.parseTime1(o.getOrderTime())))+"");*/
            return (int) -(TimeUtils.dateToStamp(TimeUtils.parseTime1(orderTime))
                    -TimeUtils.dateToStamp(TimeUtils.parseTime1(o.getOrderTime())));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
