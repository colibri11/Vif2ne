package ru.vif2ne.ui.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.vif2ne.R;
import ru.vif2ne.Session;
import ru.vif2ne.backend.domains.Smoking;

/**
 * Created by serg on 05.06.15.
 */
public class SmokeRecyclerAdapter extends RecyclerView.Adapter<SmokeRecyclerViewHolder> {

    protected LinearLayoutManager linearLayoutManager;
    private Session session;
    private Smoking smoking;

    public SmokeRecyclerAdapter(Session session) {
        this.session = session;
        this.smoking = session.getSmoking();
    }

    @Override
    public SmokeRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.smoking_recycler_layout, parent, false);
        return new SmokeRecyclerViewHolder(session, v);
    }

    @Override
    public void onBindViewHolder(SmokeRecyclerViewHolder holder, int position) {
        holder.bind(smoking.getMessage(position));
    }

    @Override
    public int getItemCount() {
        return smoking.sizeMessages();
    }
}
