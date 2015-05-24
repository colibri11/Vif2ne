/*
 * Copyright (C) 2015 by Sergey Omarov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by serg 21.05.15 20:19
 */

package ru.vif2ne.ui.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.vif2ne.R;
import ru.vif2ne.Session;
import ru.vif2ne.backend.domains.EventEntry;

public class EntryRecyclerAdapter extends RecyclerView.Adapter<EntryRecyclerViewHolder> {

    protected ArrayList<EventEntry> eventEntries;
    protected Session session;
    protected LinearLayoutManager linearLayoutManager;

    public EntryRecyclerAdapter(Session session,
                                ArrayList<EventEntry> eventEntries,
                                LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
        this.session = session;
        this.eventEntries = eventEntries;
    }

    @Override
    public EntryRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_recycler_layout, parent, false);
        return new EntryRecyclerViewHolder(session, v, linearLayoutManager);
    }

    @Override
    public void onBindViewHolder(EntryRecyclerViewHolder holder, int position) {
        holder.bind(eventEntries.get(position));
    }

    @Override
    public int getItemCount() {
        return eventEntries.size();
    }
}
