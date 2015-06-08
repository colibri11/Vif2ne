package ru.vif2ne.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import ru.vif2ne.ui.helper.DividerItemDecoration;

/**
 * Created by serg on 05.06.15.
 */
public class SmokingActivity extends BaseActivity {

    public static final String SCHEME = "vif2ne";
    private static final String LOG_TAG = "SmokingActivity";
    Handler handler;
    private SmokeRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private EditText messageEdit;
    private Timer timer;
    private TimerTask timerTask;
    private int reloadStartTime;
    private String anchor;
    private LinearLayoutManager layoutManager;
    private TextWatcher messageTextWather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smoking);
        reloadStartTime = 0;
        Toolbar toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbarTop);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        messageEdit = (EditText) findViewById(R.id.message_edit);
        messageTextWather = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                session.setSmokingEditMessage(s);
            }
        };
        messageEdit.addTextChangedListener(messageTextWather);
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


        anchor = "";
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            anchor = intent.getDataString().replace(SCHEME+"://","");
            reloadStartTime = session.getSmokingSettings().getRefresh() * 1000;
            Log.d(LOG_TAG, anchor);
        }
        Log.d(LOG_TAG, intent.toString());

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_smoking_messages);
        layoutManager = new LinearLayoutManager(this);
        adapter = new SmokeRecyclerAdapter(session, messageEdit);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing();
            }
        });
        handler = new Handler();
        messageEdit.requestFocus();


        //refreshing();
    }


    public void startTimer() {
        timer = new Timer();
        intTimerTask();
        timer.schedule(timerTask, reloadStartTime, session.getSmokingSettings().getRefresh() * 1000); //
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
        messageEdit.removeTextChangedListener(messageTextWather);
        messageEdit.setText(session.getSmokingEditMessage());
        messageEdit.addTextChangedListener(messageTextWather);
        adapter.notifyDataSetChanged();
        if (!TextUtils.isEmpty(anchor)) {
            int position = session.getSmoking().getMessagePositionByAnchor(anchor);
            layoutManager.scrollToPositionWithOffset(position, 0);
            anchor = "";
        }

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
