package ru.vif2ne.ui.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import ru.vif2ne.R;
import ru.vif2ne.Session;
import ru.vif2ne.backend.SmokingLinkMovementMethod;
import ru.vif2ne.backend.domains.SmokingMessage;

/**
 * Created by serg on 05.06.15.
 */
public class SmokeRecyclerViewHolder extends RecyclerView.ViewHolder {
    TextView smokingMessageText, smokingTime, smokingAuthor, smokingRecipient;
    LinearLayoutManager layoutManager;

    public SmokeRecyclerViewHolder(final Session session, View itemView,
                                   final LinearLayoutManager layoutManager) {
        super(itemView);
        this.layoutManager = layoutManager;
        smokingMessageText = (TextView) itemView.findViewById(R.id.smoking_message);
        smokingMessageText.setMovementMethod(SmokingLinkMovementMethod.getInstance());
        smokingAuthor = (TextView) itemView.findViewById(R.id.smoking_author);
        smokingTime = (TextView) itemView.findViewById(R.id.smoking_time);
        smokingRecipient = (TextView) itemView.findViewById(R.id.smoking_recipient);
        smokingRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmokingMessage smokingMessage = (SmokingMessage) v.getTag();
                int position = session.getSmoking().getMessagePositionByAnchor(
                        smokingMessage.getRecipient().get(0));
                layoutManager.scrollToPositionWithOffset(position, 0);
            }
        });
        //No Activity found to handle Intent { act=android.intent.action.VIEW dat=#GiantToad, (17:26) (has extras) }

    }

    public void bind (SmokingMessage smokingMessage) {
        smokingMessageText.setText(Html.fromHtml(smokingMessage.getMessage()));
        smokingTime.setText(smokingMessage.getTime());
        smokingRecipient.setText(smokingMessage.getRecipient().toString());
        smokingAuthor.setText(smokingMessage.getAuthor());
        smokingRecipient.setTag(smokingMessage);
    }

}
