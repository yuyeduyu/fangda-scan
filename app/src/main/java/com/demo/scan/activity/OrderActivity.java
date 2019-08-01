package com.demo.scan.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.demo.scan.R;
import com.demo.scan.Server;
import com.demo.scan.bean.RespBean;
import com.demo.scan.utils.TimeUtils;
import com.demo.scan.utils.WaitDialog;
import com.demo.scan.adapter.AllLogAdapter;
import com.demo.scan.bean.OrderBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler2;

public class OrderActivity extends AppCompatActivity {
    Toolbar mToolbar;
    @BindView(R.id.store_house_ptr_frame)
    PtrClassicFrameLayout storeHousePtrFrame;
    @BindView(R.id.filter_edit)
    EditText filterEdit;
    @BindView(R.id.filter_edit_type)
    EditText filterTypeEdit;
    @BindView(R.id.search)
    ImageView search;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    private String TAG = getClass().getCanonicalName();

    ArrayList<OrderBean> mLogs = new ArrayList<>();
    private int pager = 1;//数据库查询页数
    private int pagerSize = 100;//数据库每次查询数量
    private int type = 1;//1:sta  0:ap
    private AllLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_order);
        initTool();
        ButterKnife.bind(this);
        initRecyview();
        initRefresh();

    }

    private void initRecyview() {
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
//        testData();
        adapter = new AllLogAdapter(this, mLogs);
        recyclerview.setAdapter(adapter);
        recyclerview.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    private void testData() {
        OrderBean orderBean = new OrderBean();
        orderBean.setName("3");
        orderBean.setOrderNumber(7);
        orderBean.setOrderTime("Aug 20, 2018 12:00:00 AM");
        orderBean.setRiceNumber(9);
        orderBean.setShippingDate("Aug 20, 2018 12:00:00 AM");

        OrderBean orderBean1 = new OrderBean();
        orderBean1.setName("1");
        orderBean1.setOrderNumber(7);
        orderBean1.setOrderTime("Aug 20, 2017 15:00:00 AM");
        orderBean1.setRiceNumber(9);
        orderBean1.setShippingDate("Aug 20, 2017 12:00:00 AM");

        OrderBean orderBean2 = new OrderBean();
        orderBean2.setName("2");
        orderBean2.setOrderNumber(7);
        orderBean2.setOrderTime("Aug 20, 2015 13:00:00 AM");
        orderBean2.setRiceNumber(9);
        orderBean2.setShippingDate("Aug 20, 2015 12:00:00 AM");

        mLogs.add(orderBean);
        mLogs.add(orderBean1);
        mLogs.add(orderBean2);
        Collections.sort(mLogs);

    }

    private void initRefresh() {
        storeHousePtrFrame.setLastUpdateTimeRelateObject(this);
        storeHousePtrFrame.setPtrHandler(new PtrHandler2() {
            @Override
            public boolean checkCanDoLoadMore(PtrFrameLayout frame, View content, View footer) {
                return PtrDefaultHandler2.checkContentCanBePulledUp(frame, content, footer);
            }

            @Override
            public void onLoadMoreBegin(PtrFrameLayout frame) {
                //上拉加载
//                if (!TextUtils.isEmpty(filterEdit.getText().toString())) {
//                    //上拉加载所有数据
//
//                    pager++;
//                } else {
//                    Toast.makeText(OrderActivity.this, "请输入色号", Toast.LENGTH_SHORT).show();
//                }
                storeHousePtrFrame.refreshComplete();
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                //下拉刷新
                if (!TextUtils.isEmpty(filterEdit.getText().toString())
                        &&!TextUtils.isEmpty(filterTypeEdit.getText().toString())) {
                    selectData(filterEdit.getText().toString().trim(),filterTypeEdit.getText().toString().trim());
                } else {
                    Toast.makeText(OrderActivity.this, "请输入色号或类型", Toast.LENGTH_SHORT).show();
                    storeHousePtrFrame.refreshComplete();
                }
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

        });
        // the following are default settings
        storeHousePtrFrame.setResistance(1.7f);
        storeHousePtrFrame.setRatioOfHeaderHeightToRefresh(1.2f);
        storeHousePtrFrame.setDurationToClose(200);
        storeHousePtrFrame.setDurationToCloseHeader(500);
        // default is false
        storeHousePtrFrame.setPullToRefresh(false);
        // default is true
        storeHousePtrFrame.setKeepHeaderWhenRefresh(true);
        //进入界面刷新加载数据
//        storeHousePtrFrame.autoRefresh();
    }

    /**
     * 初始化 toolbar
     */
    private void initTool() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("订单查询");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    //查询
    private void selectData(final String data,final String type) {
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        String remote_ip = mSharedPrefs.getString("remote_admin_ip", Server.admin_server);
        String remote_port = mSharedPrefs.getString("remote_admin_port", Server.admin_port);
        String url = "http://" + remote_ip + ":" + remote_port + Server.serveradress + "/order/findOrderByColor";
//        String url = "http://q4sx2p.natappfree.cc"  + Server.serveradress + "/order/findOrderByColor";
        RequestParams params = new RequestParams();
        params.put("color", data);
        params.put("type", type);

        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {
                storeHousePtrFrame.refreshComplete();
                String successStr = new String(responseByte);
                successStr = successStr.substring(1, successStr.length() - 1);
                successStr = successStr.replace("\\", "");
                Log.e("sas", successStr);
// [{"name":"140/弹力缎16","orderNumber":"6.000","riceNumber":0.000,"orderTime":"Aug 20, 2015 12:00:00 AM"
// ,"shippingDate":"Oct 31, 2018 12:00:00 AM"}

// ,{"name":"108/弹力缎16","orderNumber":"2.000","riceNumber":0.000
// ,"orderTime":"Aug 31, 2015 12:00:00 AM","shippingDate":"Oct 31, 2018 12:00:00 AM"}


                if (successStr.equals("[]")) {
                    Toast.makeText(OrderActivity.this, "未查到数据！！！", Toast.LENGTH_LONG).show();
                } else {
                    analysisResp(successStr);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(OrderActivity.this, "连接服务器失败  ", Toast.LENGTH_LONG).show();

                storeHousePtrFrame.refreshComplete();
            }
        });
    }

    private void analysisResp(String successStr) {
        mLogs.clear();
//        successStr = successStr.substring(1, successStr.length() - 1).replace("\\", "");
        //创建一个Gson对象
        Gson gson = new Gson();
        //创建一个JsonParser
        JsonParser parser = new JsonParser();
        //通过JsonParser对象可以把json格式的字符串解析成一个JsonElement对象
        JsonElement el = parser.parse(successStr);
        //把JsonElement对象转换成JsonArray
        JsonArray jsonArray = null;
        if (el.isJsonArray()) {
            jsonArray = el.getAsJsonArray();
        }
        //遍历JsonArray对象
        OrderBean product = null;
        Iterator it = jsonArray.iterator();
        while (it.hasNext()) {
            JsonElement e = (JsonElement) it.next();
            //JsonElement转换为JavaBean对象
            product = gson.fromJson(e, OrderBean.class);
            try {
                if ((System.currentTimeMillis()/1000 - TimeUtils.dateToStamp(TimeUtils.parseTime1(product.getOrderTime())))
                        < 6 * 30 * 24 * 60 *60)
                    mLogs.add(product);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        if (mLogs.size()<1){
            Toast.makeText(OrderActivity.this,"该色号最近6个月无进厂记录",Toast.LENGTH_SHORT).show();
        }else {
            Collections.sort(mLogs);
            adapter.notifyDataSetChanged();
        }

    }

    @OnClick(R.id.search)
    public void onViewClicked() {
        storeHousePtrFrame.autoRefresh();
    }
}
