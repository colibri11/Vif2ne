package ru.vif2ne.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import ru.vif2ne.R;
import ru.vif2ne.Session;
import ru.vif2ne.backend.domains.SmokingMessage;

/**
 * Created by serg on 05.06.15.
 */
public class SmokeRecyclerViewHolder extends RecyclerView.ViewHolder {
    TextView smokingMessageText;

    public SmokeRecyclerViewHolder(Session session, View itemView) {
        super(itemView);
        smokingMessageText = (TextView) itemView.findViewById(R.id.smoking_message);

    }

    public void bind (SmokingMessage smokingMessage) {
        smokingMessageText.setText(Html.fromHtml(smokingMessage.getMessage()));
    }

}
