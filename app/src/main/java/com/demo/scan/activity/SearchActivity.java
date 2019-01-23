package com.demo.scan.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.demo.scan.R;
import com.demo.scan.adapter.TabMainAdapter;
import com.demo.scan.event.Mesg;
import com.demo.scan.fragment.SearchChuKuFragment;
import com.demo.scan.fragment.SearchDiaoBoFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    private TabMainAdapter adapter;  //主界面加载fragment的适配器
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initTool();
        initViewPager();
    }
    /**
     * 初始化 toolbar
     */
    private void initTool() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("记录查询");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    String[] titles;
    private void initViewPager() {
        titles = new String[]{"出库记录", "调拨记录"};
        Fragment[] fragments = new Fragment[]{
                new SearchChuKuFragment().newInstance(), new SearchDiaoBoFragment().newInstance()
        };
        adapter = new TabMainAdapter(getSupportFragmentManager(), titles, fragments);
        viewpager.setAdapter(adapter);
        tab.setupWithViewPager(viewpager);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void FTPEvent(Mesg event) {
        if (event.getMes().equals(Mesg.updateChuKuTabCount)) {
            titles[0] ="出库记录(总数:" + event.getCount() + ")";
        } else if (event.getMes().equals(Mesg.updatediaoBoTabCount))
            titles[1] = "调拨记录(总数:"  + event.getCount() + ")";
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
