package ru.vif2ne.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ru.vif2ne.R;
import ru.vif2ne.backend.SmokingLinkMovementMethod;
import ru.vif2ne.backend.domains.SmokingMessage;

/**
 * Created by serg on 05.06.15.
 */
public class SmokeRecyclerViewHolder extends RecyclerView.ViewHolder {
    private static final String LOG_TAG = "SmokeRecyclerViewHolder";
    TextView smokingMessageText, smokingTime, smokingAuthor;
    EditText editText;

    public SmokeRecyclerViewHolder(View itemView, final EditText edit) {
        super(itemView);
        this.editText = edit;
        smokingMessageText = (TextView) itemView.findViewById(R.id.smoking_message);
        smokingMessageText.setMovementMethod(SmokingLinkMovementMethod.getInstance());
        smokingAuthor = (TextView) itemView.findViewById(R.id.smoking_author);
        smokingTime = (TextView) itemView.findViewById(R.id.smoking_time);
        smokingAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cursorPosition = editText.getSelectionStart();
                editText.getText().insert(cursorPosition, " `" + smokingAuthor.getText() + "` ");
                //  editText.setText(editText.getText() + " `" + smokingAuthor.getText() + "` ");
            }
        });
        smokingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmokingMessage smokingMessage = (SmokingMessage) v.getTag();
                int cursorPosition = editText.getSelectionStart();
                editText.getText().insert(cursorPosition, " {{{" + smokingMessage.getAnchor() + "}}} ");
                //    editText.setText(editText.getText() + " {{{" + smokingMessage.getAnchor() + "}}} ");
            }
        });
    }

    public void bind(SmokingMessage smokingMessage) {
        smokingMessageText.setText(Html.fromHtml(smokingMessage.getMessage()));
        smokingTime.setText(smokingMessage.getTime());
        smokingAuthor.setText(smokingMessage.getAuthor());
        smokingTime.setTag(smokingMessage);
    }

}
