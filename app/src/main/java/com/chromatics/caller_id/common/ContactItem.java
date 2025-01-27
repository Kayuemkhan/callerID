package com.chromatics.caller_id.common;

public class ContactItem {

    public long id;
    public String displayName;

    public ContactItem(long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "ContactItem{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
