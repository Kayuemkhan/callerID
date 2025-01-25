package com.chromatics.caller_id.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import com.chromatics.caller_id.R;
import com.chromatics.caller_id.common.NumberInfo;
import com.chromatics.caller_id.models.CommunityReview;

public class IconAndColor {

    @DrawableRes
    public final int iconResId;
    @ColorRes
    final int colorResId;
    public final boolean noInfo;
    private androidx.core.widget.ImageViewCompat ImageViewCompat;

    private IconAndColor(int iconResId, int colorResId) {
        this(iconResId, colorResId, false);
    }

    private IconAndColor(int icon, int color, boolean noInfo) {
        this.iconResId = icon;
        this.colorResId = color;
        this.noInfo = noInfo;
    }

    @ColorInt
    public int getColorInt(@NonNull Context context) {
        return UiUtils.getColorInt(context, colorResId);
    }

   public void applyToImageView(AppCompatImageView imageView) {
        imageView.setImageResource(iconResId);
        androidx.core.widget.ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(
                getColorInt(imageView.getContext())));
    }

    static IconAndColor of(@DrawableRes int icon, @ColorRes int color) {
        return new IconAndColor(icon, color);
    }

    static IconAndColor forReviewRating(CommunityReview.Rating rating) {
        switch (rating) {
            case NEUTRAL:
                return of(R.drawable.ic_thumbs_up_down_24dp, R.color.rateNeutral);
            case POSITIVE:
                return of(R.drawable.ic_thumb_up_24dp, R.color.ratePositive);
            case NEGATIVE:
                return of(R.drawable.ic_thumb_down_24dp, R.color.rateNegative);
        }
        return new IconAndColor(R.drawable.ic_thumbs_up_down_24dp, R.color.notFound, true);
    }

    public static IconAndColor forNumberRating(NumberInfo.Rating rating, boolean contact) {
        boolean noInfo = false;
        @DrawableRes int icon;
        @ColorInt int color;

        switch (rating) {
            case NEUTRAL:
                icon = R.drawable.ic_thumbs_up_down_24dp;
                color = R.color.rateNeutral;
                break;

            case POSITIVE:
                icon = R.drawable.ic_thumb_up_24dp;
                color = R.color.ratePositive;
                break;

            case NEGATIVE:
                icon = R.drawable.ic_thumb_down_24dp;
                color = R.color.rateNegative;
                break;

            default:
                noInfo = true;
                icon = R.drawable.ic_call_in_queue;
                color = R.color.notFound;
                break;
        }

        if (contact) {
            noInfo = false;
            icon = R.drawable.ic_done;
        }

        return new IconAndColor(icon, color, noInfo);
    }
}
