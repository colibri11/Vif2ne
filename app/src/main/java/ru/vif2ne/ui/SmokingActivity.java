package ru.vif2ne.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

import ru.vif2ne.R;
import ru.vif2ne.backend.tasks.LoadSmokingTask;
import ru.vif2ne.ui.adapter.SmokeRecyclerAdapter;

/**
 * Created by serg on 05.06.15.
 */
public class SmokingActivity extends BaseActivity {

    private static final String LOG_TAG = "SmokingActivity";
    Handler handler;
    private SmokeRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private EditText messageEdit;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoking);
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbarTop);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        messageEdit = (EditText) findViewById(R.id.message_edit);
        messageEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        Log.d(LOG_TAG, "key code:" + keyCode + " keyEvent:" + event.toString());
                    }
                    return true;
                }
                return false;
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_smoking_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new SmokeRecyclerAdapter(session, layoutManager);
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
        handler = new Handler();

        //refreshing();
    }


    public void startTimer() {
        timer = new Timer();
        intTimerTask();
        timer.schedule(timerTask, 0, session.getSmokingSettings().getRefresh() * 1000); //
    }

    public void intTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshing();
                    }
                });
            }
        };
    }

    public void refreshing() {
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
    }

    @Override
    protected void bind() {
        startTimer();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            timerTask = null;
        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
