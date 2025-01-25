package com.chromatics.caller_id.utils;

import android.content.Context;

import com.chromatics.caller_id.R;
import com.chromatics.caller_id.common.NumberInfo;
import com.chromatics.caller_id.common.SiaNumberCategoryUtils;

public class NumberInfoUtils {

    public static String getShortDescription(Context context, NumberInfo numberInfo) {
        if (numberInfo.communityDatabaseItem != null) {
            NumberCategory category = NumberCategory.getById(
                    numberInfo.communityDatabaseItem.getCategory());

            if (category != null && category != NumberCategory.NONE) {
                return SiaNumberCategoryUtils.getName(context, category);
            }
        }

        if (numberInfo.blacklistItem != null && numberInfo.contactItem == null) {
            return context.getString(R.string.call_block);
        }

        return null;
    }

}
