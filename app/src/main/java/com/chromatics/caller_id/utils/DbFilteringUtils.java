package com.chromatics.caller_id.utils;

import android.content.Context;
import android.text.TextUtils;

import com.chromatics.caller_id.common.CallLogHelper;
import com.chromatics.caller_id.common.CallLogItem;
import com.chromatics.caller_id.common.NumberFilter;
import com.chromatics.caller_id.common.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DbFilteringUtils {

    public static NumberFilter getNumberFilter(Settings settings) {
        if (!settings.isDbFilteringEnabled()) return null;

        return new NumberFilter(getPrefixesToKeep(settings),
                settings.isDbFilteringThorough(),
                settings.getDbFilteringKeepShortNumbers()
                        ? settings.getDbFilteringKeepShortNumbersMaxLength() : 0);
    }

    public static List<String> getPrefixesToKeep(Settings settings) {
        return parsePrefixes(settings.getDbFilteringPrefixesToKeep());
    }

    public static List<String> parsePrefixes(String prefixesString) {
        if (TextUtils.isEmpty(prefixesString)) return Collections.emptyList();

        List<String> prefixList = new ArrayList<>();

        for (String prefix : prefixesString.split("[,;]")) {
            prefix = prefix.replaceAll("[^0-9]", "");
            if (!prefix.isEmpty() && !prefixList.contains(prefix)) prefixList.add(prefix);
        }

        return prefixList;
    }

    public static String formatPrefixes(Collection<String> prefixes) {
        List<String> formattedPrefixes = new ArrayList<>(prefixes.size());

        for (String prefix : prefixes) {
            formattedPrefixes.add("+" + prefix);
        }

        return TextUtils.join(",", formattedPrefixes);
    }

    public static List<String> detectPrefixes(Context context, String countryCode) {
        Set<String> prefixes = new HashSet<>();

        List<CallLogItem> callLogItems = CallLogHelper.loadCalls(context, null, false, 100);

        for (CallLogItem callLogItem : callLogItems) {
            String number = NumberUtils.normalizeNumber(callLogItem.number, countryCode);

            if (number != null && number.startsWith("+") && number.length() > 1) {
                char firstDigit = number.charAt(1);
                if (firstDigit >= '0' && firstDigit <= '9') {
                    prefixes.add(String.valueOf(firstDigit));
                }
            }
        }

        List<String> prefixList = new ArrayList<>(prefixes);
        Collections.sort(prefixList);

        return prefixList;
    }

}
