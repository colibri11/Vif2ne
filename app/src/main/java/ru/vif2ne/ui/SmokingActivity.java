package ru.vif2ne.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import ru.vif2ne.R;
import ru.vif2ne.backend.tasks.LoadSmokingTask;
import ru.vif2ne.ui.adapter.SmokeRecyclerAdapter;

/**
 * Created by serg on 05.06.15.
 */
public class SmokingActivity extends BaseActivity {

    private static final String LOG_TAG = "SmokingActivity";
    private SmokeRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoking);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbarTop);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_smoking_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new SmokeRecyclerAdapter(session);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setAdapter(adapter);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               refreshing();
            }
        });
        refreshing();
    }

    public void refreshing(){
        swipeRefresh.setEnabled(false);
        swipeRefresh.setRefreshing(true);
        new LoadSmokingTask(session) {
            @Override
            public void goSuccess(Object result) {
                adapter.notifyDataSetChanged();
                swipeRefresh.setEnabled(true);
                swipeRefresh.setRefreshing(false);
            }
        }.execute((Void) null);
    };

    @Override
    protected void bind() {
        adapter.notifyDataSetChanged();
    }
}
