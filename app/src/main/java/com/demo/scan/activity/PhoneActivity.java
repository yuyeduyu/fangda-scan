package com.demo.scan.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.scan.R;
import com.demo.scan.Server;
import com.demo.scan.bean.Barcode;
import com.demo.scan.bean.InfoData;
import com.demo.scan.bean.RespBean;
import com.demo.scan.utils.AppUtils;
import com.demo.scan.utils.PopupMenuUtil;
import com.demo.scan.utils.WaitDialog;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.github.xudaojie.qrcodelib.CaptureActivity;

public class PhoneActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * UI
     **/
    Toolbar mToolbar;
    private EditText eidtBarCount;
    private Button buttonClear;
    private ListView listViewData;
    private CheckBox checkBoxPer;
    private Button buttonScan;
    private Button buttonExit;
    private Button buttonIn;
    private Button buttonOut;
    private Button buttonSave;
    private Button buttonFun;

    private List<Barcode> listBarcode = new ArrayList<Barcode>();
    private List<Map<String, String>> listMap;
    private SimpleAdapter adapter = null;
    private boolean isScaning = false;
    private Timer scanTimer = null;
    //选择的设备名称
    private String equnitorName;
    private String[] languages;//所有设备名称
    private Button button_give;
    private Button button_lock;

    private void addListView() {
        listMap = new ArrayList<Map<String, String>>();
        int id = 1;
        for (Barcode barcode : listBarcode) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("id", id + "");
            map.put("barcode", barcode.getBarcode());
            map.put("count", barcode.getCount() + "");
            id++;
            listMap.add(map);
        }
        adapter = new SimpleAdapter(this, listMap, R.layout.listview_item,
                new String[]{"id", "barcode", "count", "name", "length", "color", "lock"}, new int[]{
                R.id.textView_list_item_id,
                R.id.textView_list_item_barcode,
                R.id.textView_list_item_count,
                R.id.name,
                R.id.mNum,
                R.id.color,
                R.id.lock,
        });
        listViewData.setAdapter(adapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set HOME key scan
        getWindow().addFlags(0x80000000);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    /**
     * 测试数据
     *
     * @Author lish
     * @Date 2018-10-31 10:59
     */
    private void testData() {
        listMap = new ArrayList<Map<String, String>>();
        int id = 1;
        for (int i = 1; i < 3; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("id", id + "");
            map.put("barcode", "17080601700" + i);
            id++;
            listMap.add(map);
        }
       /* Collections.sort(listMap, new Comparator<Map<String, String>>() {
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                Integer name1 = Integer.valueOf(o1.get("length").substring(0,o1.get("length").length()-1).toString()) ;//name1是从你list里面拿出来的一个
                Integer name2 = Integer.valueOf(o2.get("length").substring(0,o2.get("length").length()-1).toString()) ; //name1是从你list里面拿出来的第二个name
                return name1.compareTo(name2);
            }
        });*/
        adapter = new SimpleAdapter(this, listMap, R.layout.listview_item,
                new String[]{"id", "barcode", "count", "name", "length", "color", "lock"}, new int[]{
                R.id.textView_list_item_id,
                R.id.textView_list_item_barcode,
                R.id.textView_list_item_count,
                R.id.name,
                R.id.mNum,
                R.id.color,
                R.id.lock,
        });
        listViewData.setAdapter(adapter);

    }

    private List<Barcode> sortAndadd(List<Barcode> list, String barcode) {
        Barcode goods = new Barcode();
        goods.setBarcode(barcode);
        int temp = 1;
        if (list == null || list.size() == 0) {
            goods.setCount(temp);
            list.add(goods);
            return list;
        }

        for (int i = 0; i < list.size(); i++) {
            if (barcode.equals(list.get(i).getBarcode())) {
                temp = list.get(i).getCount() + temp;
                goods.setCount(temp);
                for (int j = i; j > 0; j--) {
                    list.set(j, list.get(j - 1));
                }
                list.set(0, goods);
                return list;
            }
        }
        //
        Barcode lastgoods = list.get(list.size() - 1);
        for (int j = list.size() - 1; j >= 0; j--) {
            if (j == 0) {
                goods.setCount(temp);
                list.set(j, goods);
            } else {
                list.set(j, list.get(j - 1));
            }

        }
        list.add(lastgoods);
        return list;
    }

    long exitSytemTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("Sas", keyCode + "");
        if (keyCode == KeyEvent.KEYCODE_HOME) {
//            scanThread.scan();
            scan(REQUEST_QR_CODE_DEV_NUM);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (scanTimer != null) {
            scanTimer.cancel();
        }

        super.onDestroy();
    }

    private void initView() {
        initTool();
        EventBus.getDefault().register(this);
        eidtBarCount = (EditText) findViewById(R.id.editText_barcode_count);
        buttonClear = (Button) findViewById(R.id.button_clear);
        listViewData = (ListView) findViewById(R.id.listView_data_list);

        checkBoxPer = (CheckBox) findViewById(R.id.checkbox_per_100ms);
        buttonScan = (Button) findViewById(R.id.button_scan);
        buttonFun = (Button) findViewById(R.id.button_fun);
        buttonFun.setOnClickListener(this);
        buttonExit = (Button) findViewById(R.id.button_exit);
        buttonIn = (Button) findViewById(R.id.button_in);
        buttonOut = (Button) findViewById(R.id.button_out);
        buttonSave = (Button) findViewById(R.id.button_save);
        listViewData.setOnItemClickListener(this);
        buttonIn.setOnClickListener(this);
        buttonOut.setOnClickListener(this);
        buttonSave.setOnClickListener(this);

        button_give = (Button) findViewById(R.id.button_give);
        button_lock = (Button) findViewById(R.id.button_lock);
        button_give.setOnClickListener(this);
        button_lock.setOnClickListener(this);

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan(REQUEST_QR_CODE_DEV_NUM);
            }
        });
        checkBoxPer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isScaning = true;
                    buttonScan.setClickable(false);
                    buttonScan.setTextColor(Color.GRAY);
                    scanTimer = new Timer();
                    scanTimer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                           /* if (scanThread != null) {
                                scanThread.scan();
                            }*/

                        }
                    }, 100, 200);
                } else {
                    isScaning = false;
                    buttonScan.setClickable(true);
                    buttonScan.setTextColor(Color.BLACK);
                    if (scanTimer != null) {
                        scanTimer.cancel();
                    }
                }

            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clearData();
            }
        });

        buttonExit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                finish();
            }
        });
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        languages = getResources().getStringArray(R.array.arr_equnitor);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                SharedPreferences pref = PhoneActivity.this.getSharedPreferences("equnitor", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("equnitor", pos);
                editor.commit();
                equnitorName = languages[pos];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        SharedPreferences pref = getSharedPreferences("equnitor", MODE_PRIVATE);
        int pos = pref.getInt("equnitor", -1);//第二个参数为默认值
        equnitorName = languages[0];
        if (pos != -1) {
            equnitorName = languages[pos];
            spinner.setSelection(pos, true);
        }
    }

    private void clearData(ArrayList<RespBean> respBeans) {
        if (respBeans.size() == (listMap == null ? 0 : listMap.size())) {
            eidtBarCount.setText("");
            listBarcode.removeAll(listBarcode);
            if (listMap != null) {
                listMap.clear();
            }
            listViewData.setAdapter(null);
        } else {
            eidtBarCount.setText((listMap == null ? 0 : listMap.size()) - respBeans.size() + "");
            listBarcode.removeAll(listBarcode);
            if (listMap != null) {
                for (int i = 0; i < listMap.size(); i++) {
                    for (int j = 0; j < respBeans.size(); j++) {
                        if (respBeans.get(j).equals(listMap.get(i).get("barcode").toString())) {
                            listMap.remove(i);
                            i--;
                            break;
                        }
                    }
                }
                adapter = new SimpleAdapter(this, listMap, R.layout.listview_item,
                        new String[]{"id", "barcode", "count", "name", "length", "color", "lock"}, new int[]{
                        R.id.textView_list_item_id,
                        R.id.textView_list_item_barcode,
                        R.id.textView_list_item_count,
                        R.id.name,
                        R.id.mNum,
                        R.id.color,
                        R.id.lock,
                });
                listViewData.setAdapter(adapter);
            }
        }

    }

    private void clearData() {
        eidtBarCount.setText("");
        listBarcode.removeAll(listBarcode);
        if (listMap != null) {
            listMap.clear();
        }
        listViewData.setAdapter(null);

    }

    @Override
    public void onBackPressed() {
        // 当popupWindow 正在展示的时候 按下返回键 关闭popupWindow 否则关闭activity
        if (PopupMenuUtil.getInstance()._isShowing()) {
            PopupMenuUtil.getInstance()._rlClickAction();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        if (isScaning) {
//            Toast.makeText(PhoneActivity.this, "请先停止扫描,在操作", Toast.LENGTH_SHORT).show();
//            return;
            isScaning = false;
            buttonScan.setClickable(true);
            buttonScan.setTextColor(Color.BLACK);
            if (scanTimer != null) {
                scanTimer.cancel();
            }
        }
        switch (v.getId()) {
            case R.id.button_fun:
                //弹出操作界面
                PopupMenuUtil.getInstance()._show(PhoneActivity.this, mToolbar);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void FTPEvent(String event) {
        List<String> list = new ArrayList<String>();
        switch (event) {
            case "2":
                //调拨
                if (listMap == null || listMap.size() < 1) {
                    Toast.makeText(PhoneActivity.this, "数据为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < listMap.size(); i++) {
                    list.add(listMap.get(i).get("barcode").toString());
                }
//                                list.add("170726039004");
//                                list.add("170630019001");
                GiveData(list);
                break;
            case "1":
                //冻结
                if (listMap == null || listMap.size() < 1) {
                    Toast.makeText(PhoneActivity.this, "数据为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < listMap.size(); i++) {
                    list.add(listMap.get(i).get("barcode").toString());
                }
                LockData(list);

                break;
            case "3":
                //入库
                if (listMap == null || listMap.size() < 1) {
                    Toast.makeText(PhoneActivity.this, "数据为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < listMap.size(); i++) {
                    list.add(listMap.get(i).get("barcode").toString());
                }
                inData(list);

                break;
            case "4":
                //出库
                if (listMap == null || listMap.size() < 1) {
                    Toast.makeText(PhoneActivity.this, "数据为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < listMap.size(); i++) {
                    list.add(listMap.get(i).get("barcode").toString());
                }
                outData(list);

                break;
            case "5":
                //盘存
                if (listMap == null || listMap.size() < 1) {
                    Toast.makeText(PhoneActivity.this, "数据为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < listMap.size(); i++) {
                    list.add(listMap.get(i).get("barcode").toString());
                }
                if (list != null) {
                    saveData(list);
                }

                break;
            case "6":
                //出库查询
                /*if (!AppUtils.checkAppInstalled(PhoneActivity.this,"com.example.uhfsdkdemo")){
                    Toast.makeText(PhoneActivity.this,"请安装方大丝绸RFID APP后在点击",Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent();
                    ComponentName cn = new ComponentName("com.example.uhfsdkdemo", "com.example.uhfsdkdemo.activity.SearchActivity");
                    intent.setComponent(cn);
                    startActivity(intent);
                }*/
                //出库查询
                startActivity(new Intent(PhoneActivity.this, SearchActivity.class));
                break;
            case "7":
                //一键翻译
                if (listMap == null || listMap.size() < 1) {
                    Toast.makeText(PhoneActivity.this, "数据为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                searchDataInfo(listMap);
                break;
            case "8":
                //订单查询
                startActivity(new Intent(PhoneActivity.this, OrderActivity.class));
                break;
            case "9":
                //结算查询
                startActivity(new Intent(PhoneActivity.this, SettlementActivity.class));
                break;
        }
    }

    /**
     * 一键翻译
     *
     * @param list
     * @Author lish
     * @Date 2018-10-30 13:55
     */
    private void searchDataInfo(List<Map<String, String>> list) {
        for (int i = 0; i < list.size(); i++) {
            selectData(list.get(i), i);
            selectDJ(list.get(i), i);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void FTPEvent(InfoData event) {
        if (event.getName() != null)
            listMap.get(event.getId()).put("name", event.getName());
        if (event.getColor() != null)
            listMap.get(event.getId()).put("color", event.getColor());
        if (event.getLength() != null)
            listMap.get(event.getId()).put("length", event.getLength()+"米");
        if (event.getLock() != null) {
            if (event.getLock().equals(InfoData.isLocked)) {
                listMap.get(event.getId()).put("lock", String.valueOf(R.mipmap.lock));
            } else
                listMap.get(event.getId()).put("lock", String.valueOf(0));
        }
        Collections.sort(listMap, new Comparator<Map<String, String>>() {
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                Double name1 = Double.valueOf(o1.get("length").substring(0,o1.get("length").length()-1).toString()) ;//name1是从你list里面拿出来的一个
                Double name2 = Double.valueOf(o2.get("length").substring(0,o2.get("length").length()-1).toString()) ; //name1是从你list里面拿出来的第二个name
                return name1.compareTo(name2);
            }
        });
        adapter = new SimpleAdapter(this, listMap, R.layout.listview_item,
                new String[]{"id", "barcode", "count", "name", "length", "color", "lock"}, new int[]{
                R.id.textView_list_item_id,
                R.id.textView_list_item_barcode,
                R.id.textView_list_item_count,
                R.id.name,
                R.id.mNum,
                R.id.color,
                R.id.lock,
        });
        listViewData.setAdapter(adapter);

    }
    //一键翻译 查询订单状态
    private void selectDJ(final Map<String, String> data, final int i) {
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        String remote_ip = mSharedPrefs.getString("remote_admin_ip", Server.admin_server);
        String remote_port = mSharedPrefs.getString("remote_admin_port", Server.admin_port);

        String url = "http://" +remote_ip + ":" +remote_port+ Server.serveradress+"/findDJByCode";
//		String accountName = mSharedPrefs.getString("phone", null);

        RequestParams params = new RequestParams();
        params.put("code", data.get("barcode").toString().trim());
//        params.put("code", "170726039002");
        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {

                String successStr = new String(responseByte);
                successStr = successStr.substring(1,successStr.length()-1);
                successStr=successStr.replace("\\", "");
                Log.e("success",successStr);
                try {
                    JSONObject jsonObject=new JSONObject(successStr);
                    if(jsonObject.has("dongJ")){
                        String dongJ = jsonObject.get("dongJ").toString();//RFIDF
                        InfoData infoData = new InfoData();
                        infoData.setId(i);
                        infoData.setLock(dongJ);
                        EventBus.getDefault().post(infoData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("e",e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
            }
        });
    }
    //一键翻译
    private void selectData(final Map<String, String> data, final int i) {
//        final WaitDialog waitDialog = new WaitDialog(PhoneActivity.this);
//        waitDialog.setContent("正在查询...");
//        waitDialog.show();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        String remote_ip = mSharedPrefs.getString("remote_admin_ip", Server.admin_server);
        String remote_port = mSharedPrefs.getString("remote_admin_port", Server.admin_port);

        String url = "http://" + remote_ip + ":" + remote_port + Server.serveradress + "/findByCode";
//        String url = "http://q4sx2p.natappfree.cc" + Server.serveradress + "/findByCode";
//		String accountName = mSharedPrefs.getString("phone", null);

        RequestParams params = new RequestParams();
        params.put("code", data.get("barcode").toString().trim());
//        params.put("code", "170726039002");

        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {
                String successStr = new String(responseByte);
                successStr = successStr.substring(1, successStr.length() - 1);
                successStr = successStr.replace("\\", "");
                Log.e("sas", successStr);
//                [{\"rfidf\":\"170505008001\",\"productName\":\"114/素绉缎16\",\"color\":\"3#\"," +
//                "length":41.000,"
//                \"outTime\":\"May 9, 2017 12:00:00 AM\",\"dyeShopNumber\":\"GS170505008\"," +
//                \"crockCountNumber\":1,\"assist\":\"170505008\",\"excelServerRCID\":\"rc20170510000108\"," +
//                \"excelServerRN\":1,\"excelServerCN\":0,\"excelServerRC1\":\"\",\"excelServerWIID\":\"\"," +
//                \"excelServerRTID\":\"1022.1\",\"excelServerCHG\":0,\"status\":\"入库\"," +
//                \"storeroom\":\"色坯库\",\"barCode\":\"\",\"barCodeAssist\":\"R170505008001\"," +
//                \"rfid\":\"170505008001\"}]"

                if (successStr.equals("[]")) {
//                    Toast.makeText(PhoneActivity.this, "未查到数据！！！", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        JSONArray jsonArray = new JSONArray(successStr);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        //解析数据
                        String rfidf = "";//RFIDF
                        String productName = "";//品名
                        String color = "";//颜色
                        String length = "";//米数

                        InfoData infoData = new InfoData();
//                        infoData.setColor("颜色"+i);
                        infoData.setId(i);
//                        infoData.setLength("米数"+i);
//                        infoData.setName("名称"+i);
//                        infoData.setLock(i%2==0);

                        if (jsonObject.has("productName")) {
                            productName = jsonObject.getString("productName");
                            infoData.setName(productName);
                        }
                        if (jsonObject.has("color")) {
                            color = jsonObject.getString("color");//RFIDF
                            infoData.setColor(color);
                        }
                        if (jsonObject.has("length")) {
                            length = jsonObject.getString("length");//RFIDF
                            infoData.setLength(length);
                        }
                        EventBus.getDefault().post(infoData);
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        waitDialog.dismiss();
                        Toast.makeText(PhoneActivity.this, "解析失败  ", Toast.LENGTH_LONG).show();
                    }
                }

//                waitDialog.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(PhoneActivity.this, "连接服务器失败  ", Toast.LENGTH_LONG).show();
//                if (waitDialog != null) {
//                    waitDialog.dismiss();
//                }
            }
        });
    }

    //调拨
    private void GiveData(final List<String> datalist) {
        final WaitDialog waitDialog = new WaitDialog(PhoneActivity.this);
        waitDialog.setContent("正在修改...");
        waitDialog.show();
        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String remote_ip = mSharedPrefs.getString("remote_admin_ip", Server.admin_server);
        String remote_port = mSharedPrefs.getString("remote_admin_port", Server.admin_port);

        String url = "http://" + remote_ip + ":" + remote_port + Server.serveradress + "/allocationByCode";

//		String url = "http://192.168.1.5:8080/fdsc/inComing";
        RequestParams params = new RequestParams();
        params.put("codes", new Gson().toJson(datalist));
        params.put("equInfor", equnitorName);
        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {

                String successStr = new String(responseByte);
                Log.e("successStr", successStr);

                analysisResp(successStr, "调拨", waitDialog);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(PhoneActivity.this, "连接服务器失败  ", Toast.LENGTH_LONG).show();
                if (waitDialog != null) {
                    waitDialog.dismiss();
                }

            }
        });
    }

    private void analysisResp(String successStr, String type, WaitDialog waitDialog) {
        ArrayList<RespBean> respBeans = new ArrayList<>();
        successStr = successStr.substring(1, successStr.length() - 1).replace("\\", "");
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
        RespBean product = null;
        Iterator it = jsonArray.iterator();
        while (it.hasNext()) {
            JsonElement e = (JsonElement) it.next();
            //JsonElement转换为JavaBean对象
            product = gson.fromJson(e, RespBean.class);
            respBeans.add(product);
        }
        if (respBeans.size() > 0) {
            //入库成功，清楚数据
            Toast.makeText(PhoneActivity.this, type + "总数:" + (listMap == null ? 0 : listMap.size()) + "  成功" + respBeans.size()
                    + "  失败" + ((listMap == null ? 0 : listMap.size()) - respBeans.size()), Toast.LENGTH_LONG).show();
            clearData(respBeans);
        } else {
            //入库失败
            Toast.makeText(PhoneActivity.this, type + "失败  " + successStr, Toast.LENGTH_LONG).show();
        }

        if (waitDialog != null)
            waitDialog.dismiss();
    }

    //冻结
    private void LockData(final List<String> datalist) {
        final WaitDialog waitDialog = new WaitDialog(PhoneActivity.this);
        waitDialog.setContent("正在修改...");
        waitDialog.show();
        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String remote_ip = mSharedPrefs.getString("remote_admin_ip", Server.admin_server);
        String remote_port = mSharedPrefs.getString("remote_admin_port", Server.admin_port);

        String url = "http://" + remote_ip + ":" + remote_port + Server.serveradress + "/frozenByCode";

//		String url = "http://192.168.1.5:8080/fdsc/inComing";
        RequestParams params = new RequestParams();
        params.put("codes", new Gson().toJson(datalist));
        params.put("equInfor", equnitorName);
        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {

                String successStr = new String(responseByte);
                analysisResp(successStr, "冻结", waitDialog);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(PhoneActivity.this, "连接服务器失败  ", Toast.LENGTH_LONG).show();
                if (waitDialog != null) {
                    waitDialog.dismiss();
                }
            }
        });
    }

    //盘存
    private void saveData(final List<String> datalist) {
        final WaitDialog waitDialog = new WaitDialog(PhoneActivity.this);
        waitDialog.setContent("正在修改...");
        waitDialog.show();
        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String remote_ip = mSharedPrefs.getString("remote_admin_ip", Server.admin_server);
        String remote_port = mSharedPrefs.getString("remote_admin_port", Server.admin_port);

        String url = "http://" + remote_ip + ":" + remote_port + Server.serveradress + "/inventoryCode";

//		String url = "http://192.168.1.5:8080/fdsc/inComing";
//		String accountName = mSharedPrefs.getString("phone", null);

        RequestParams params = new RequestParams();
        params.put("codes", new Gson().toJson(datalist));

        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {

                String successStr = new String(responseByte);
                analysisResp(successStr, "盘存", waitDialog);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(PhoneActivity.this, "连接服务器失败  ", Toast.LENGTH_LONG).show();
                if (waitDialog != null) {
                    waitDialog.dismiss();
                }

            }
        });
    }

    //入库
    private void inData(final List<String> datalist) {
        final WaitDialog waitDialog = new WaitDialog(PhoneActivity.this);
        waitDialog.setContent("正在修改...");
        waitDialog.show();
        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String remote_ip = mSharedPrefs.getString("remote_admin_ip", Server.admin_server);
        String remote_port = mSharedPrefs.getString("remote_admin_port", Server.admin_port);

        String url = "http://" + remote_ip + ":" + remote_port + Server.serveradress + "/inComingCode";

//		String url = "http://192.168.1.5:8080/fdsc/inComing";
        RequestParams params = new RequestParams();
        params.put("codes", new Gson().toJson(datalist));
        params.put("equInfor", "门店");
        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {

                String successStr = new String(responseByte);
                analysisResp(successStr, "入库", waitDialog);


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(PhoneActivity.this, "连接服务器失败  ", Toast.LENGTH_LONG).show();
                if (waitDialog != null) {
                    waitDialog.dismiss();
                }

            }
        });
    }

    //出库
    private void outData(final List<String> datalist) {
        final WaitDialog waitDialog = new WaitDialog(PhoneActivity.this);
        waitDialog.setContent("正在修改...");
        waitDialog.show();
        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String remote_ip = mSharedPrefs.getString("remote_admin_ip", Server.admin_server);
        String remote_port = mSharedPrefs.getString("remote_admin_port", Server.admin_port);

        String url = "http://" + remote_ip + ":" + remote_port + Server.serveradress + "/outGoingCode";

//        String url = "http://192.168.1.5:8080/fdsc/outGoing";
//		String accountName = mSharedPrefs.getString("phone", null);

        RequestParams params = new RequestParams();
        params.put("codes", new Gson().toJson(datalist));

        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {

                String successStr = new String(responseByte);
                analysisResp(successStr, "出库", waitDialog);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(PhoneActivity.this, "连接服务器失败  ", Toast.LENGTH_LONG).show();
                if (waitDialog != null) {
                    waitDialog.dismiss();
                }

            }
        });
    }

    /**
     * 初始化 toolbar
     */
    private void initTool() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("方大丝绸手机扫描");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent1 = new Intent(this, SetAdminActivity.class);
        startActivity(intent1);
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView epcTextview = (TextView) view.findViewById(R.id.textView_list_item_barcode);
        final String epc = epcTextview.getText().toString().trim();
        Intent intent = new Intent(PhoneActivity.this, InfoActivity.class);
        intent.putExtra("epc", epc);
        startActivity(intent);
    }

    private void scan(int code) {
        Intent i = new Intent(PhoneActivity.this, CaptureActivity.class);
        startActivityForResult(i, code);
    }
    public static int REQUEST_QR_CODE_DEV_NUM = 10001;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_QR_CODE_DEV_NUM) {
                String result = data.getStringExtra("result");
                sortAndadd(listBarcode, result);
                addListView();
                eidtBarCount.setText(listBarcode.size() + "");

            }
        }
    }
}
