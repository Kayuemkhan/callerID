package com.chromatics.caller_id.common.interfaces;

public interface NumberFilter {

    boolean keepPrefix(String prefix);

    boolean keepNumber(String number);

    boolean isDetailed();

}
