package com.chromatics.caller_id.common;

public interface ContactsProvider {

    ContactItem get(String number);

    boolean isInLimitedMode();

}
