package com.chromatics.caller_id.common;

public interface Properties {

    int getInt(String key, int defValue);

    void setInt(String key, int value);

}