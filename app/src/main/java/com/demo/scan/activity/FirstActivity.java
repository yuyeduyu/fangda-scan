package com.demo.scan.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.scan.R;

public class FirstActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv1,tv2,tv3;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        initTool();
        tv1 = (TextView) findViewById(R.id.tv_1);
        tv2 = (TextView) findViewById(R.id.tv_2);
        tv3 = (TextView) findViewById(R.id.tv_3);
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        tv3.setOnClickListener(this);
    }
    /**
     * 初始化 toolbar
     */
    private void initTool() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.tv_1:
//                1D/2D扫描
                intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_2:
//                RFID扫描
                if (!checkAppInstalled(FirstActivity.this,"com.example.uhfsdkdemo")){
                    Toast.makeText(FirstActivity.this,"请安装方大丝绸RFID APP后在点击",Toast.LENGTH_SHORT).show();
                }else {
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    ComponentName cn = new ComponentName("com.example.uhfsdkdemo", "com.example.uhfsdkdemo.activity.RFIDActivity");
                    intent.setComponent(cn);
                    startActivity(intent);
                }

                break;
            case R.id.tv_3:
//                手机扫描
                intent = new Intent(this,PhoneActivity.class);
                startActivity(intent);
                break;
        }
    }
    /**
     * 判断APP是否存在
     * @Author lish
     * @Date 2019-01-23 13:58
     */
    private boolean checkAppInstalled(Context context, String pkgName) {
        if (pkgName== null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if(packageInfo == null) {
            return false;
        } else {
            return true;//true为安装了，false为未安装
        }
    }
}
