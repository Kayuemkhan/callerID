package com.chromatics.caller_id.common;

public interface Settings {

    int getBaseDbVersion();

    void setBaseDbVersion(int version);

    int getSecondaryDbVersion();

    void setSecondaryDbVersion(int version);

}