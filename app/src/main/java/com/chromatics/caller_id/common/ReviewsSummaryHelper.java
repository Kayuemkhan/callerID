package com.chromatics.caller_id.common;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.chromatics.caller_id.R;
import com.chromatics.caller_id.models.CommunityDatabaseItem;


public class ReviewsSummaryHelper {

    public static void populateSummary(View reviewsSummary, CommunityDatabaseItem item) {
        boolean visible = item != null && item.hasRatings();

        reviewsSummary.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            setValue(reviewsSummary, R.id.summary_text_negative, item.getNegativeRatingsCount());
            setValue(reviewsSummary, R.id.summary_text_neutral, item.getNeutralRatingsCount());
            setValue(reviewsSummary, R.id.summary_text_positive, item.getPositiveRatingsCount());
        }
    }

    private static void setValue(View parentView, @IdRes int id, int value) {
        parentView.<TextView>findViewById(id).setText(String.valueOf(value));
    }

}
