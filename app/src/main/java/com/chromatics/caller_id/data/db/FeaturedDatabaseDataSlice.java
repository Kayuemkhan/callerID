package com.chromatics.caller_id.data.db;

import com.chromatics.caller_id.models.AbstractDatabaseDataSlice;
import com.chromatics.caller_id.models.FeaturedDatabaseItem;
import com.chromatics.caller_id.utils.LittleEndianDataInputStream;

import java.io.IOException;

public class FeaturedDatabaseDataSlice extends AbstractDatabaseDataSlice<FeaturedDatabaseItem> {

    private String[] names;

    @Override
    protected FeaturedDatabaseItem getDbItemByNumberInternal(long number, int index) {
        return new FeaturedDatabaseItem(number, names[index]);
    }

    @Override
    protected void loadFromStreamCheckHeader(String header) throws IOException {
        if (!"YABX".equalsIgnoreCase(header) && !"MTZX".equalsIgnoreCase(header)) {
            throw new IOException("Invalid header. Actual value: " + header);
        }
    }

    @Override
    protected void loadFromStreamInitFields() {
        names = new String[numberOfItems];
    }

    @Override
    protected void loadFromStreamLoadFields(int index, LittleEndianDataInputStream stream) throws IOException {
        int nameLength = stream.readInt();
        names[index] = stream.readUtf8StringChars(nameLength);
    }

}