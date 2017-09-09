package com.example.android.easymail.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.example.android.easymail.CurrentDayMessageClickListener;
import com.example.android.easymail.R;
import com.example.android.easymail.models.Message;


import org.w3c.dom.Text;

/**
 * Created by Harshit Bansal on 6/13/2017.
 */

public class MessageViewHolder extends ChildViewHolder {

    Context context;
    CurrentDayMessageClickListener listener;
    private TextView subject, from, to, time, snippet, expand_collapse;
    private CardView emailCardView;

    public MessageViewHolder(final CurrentDayMessageClickListener listener, @NonNull View itemView, final int row, final int column, Context context) {

        super(itemView);
        this.listener = listener;
        subject = (TextView) itemView.findViewById(R.id.email_subject);
        from = (TextView) itemView.findViewById(R.id.email_from);
        to = (TextView) itemView.findViewById(R.id.email_to);
        time = (TextView) itemView.findViewById(R.id.email_time);
        snippet = (TextView) itemView.findViewById(R.id.email_snippet);
        expand_collapse = (TextView) itemView.findViewById(R.id.expand_collapse_email);
        emailCardView = (CardView) itemView.findViewById(R.id.email_card_view);
        int r = itemView.getHeight();
        this.context = context;
        WindowManager windowmanager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dimension = new DisplayMetrics();
        windowmanager.getDefaultDisplay().getMetrics(dimension);
        final int height = dimension.heightPixels;
        final int[] minHeight = new int[1];

        emailCardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                emailCardView.getViewTreeObserver().removeOnPreDrawListener(this);
                minHeight[0] = emailCardView.getHeight();
                ViewGroup.LayoutParams layoutParams = emailCardView.getLayoutParams();
                layoutParams.height = minHeight[0];
                emailCardView.setLayoutParams(layoutParams);

                return true;
            }
        });
        expand_collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCardView(height, minHeight[0]);
            }
        });
    }

    private void toggleCardView(int height, int minHeight) {

        if (emailCardView.getHeight() == minHeight) {
            // expand
            expandView(height); //'height' is the height of screen which we have measured already.

        } else {
            // collapse
            collapseView(minHeight);

        }
    }

    private void expandView(int height) {

        ValueAnimator anim = ValueAnimator.ofInt(emailCardView.getMeasuredHeightAndState(),
                height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = emailCardView.getLayoutParams();
                layoutParams.height = val;
                emailCardView.setLayoutParams(layoutParams);
            }
        });
        anim.start();
    }

    private void collapseView(int minHeight){

        ValueAnimator anim = ValueAnimator.ofInt(emailCardView.getMeasuredHeightAndState(),
                minHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = emailCardView.getLayoutParams();
                layoutParams.height = val;
                emailCardView.setLayoutParams(layoutParams);

            }
        });
        anim.start();
    }

    public void bind(final Message child) {

        emailCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onCurrentDayMessageClickListener(v, child);
                return true;
            }
        });
        String id = child.getId();
        to.setText("me");
        for (int i = 0; i < child.getPayload().getHeaders().size(); i++) {
            String check = child.getPayload().getHeaders().get(i).getName();
            String value = child.getPayload().getHeaders().get(i).getValue();
            switch (check){
                case "From":
                    from.setText(value);
                    break;
                case "Subject":
                    subject.setText(value);
                    break;
                case "To":
                    to.setText(value);
                    break;
                case "Received":
                    time.setText(value.split(",")[0]);
            }
        }
        snippet.setText(child.getSnippet());
    }
}
