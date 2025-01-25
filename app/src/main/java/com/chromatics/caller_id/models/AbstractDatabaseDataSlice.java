package com.chromatics.caller_id.models;

import android.util.Log;

import com.chromatics.caller_id.utils.LittleEndianDataInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class AbstractDatabaseDataSlice<T> {


    protected int dbVersion;

    protected int numberOfItems;

    protected long[] numbers;

    protected long lastAccessTimestamp;

    public int getDbVersion() {
        return this.dbVersion;
    }

    public int getNumberOfItems() {
        return this.numberOfItems;
    }

    public long getLastAccessTimestamp() {
        return this.lastAccessTimestamp;
    }

    protected int indexOf(long number) {
        if (numberOfItems > 0) {
            return Arrays.binarySearch(numbers, number);
        }

        return -1;
    }

    public T getDbItemByNumber(long number) {
//        LOG.debug("getDbItemByNumber({}) started", number);

        lastAccessTimestamp = System.currentTimeMillis();

        int index = indexOf(number);
//        LOG.trace("getDbItemByNumber() index={}", index);
        if (index < 0) return null;

        return getDbItemByNumberInternal(number, index);
    }

    protected abstract T getDbItemByNumberInternal(long number, int index);

    protected void loadFromStreamCheckHeader(String header) throws IOException {}

    protected void loadFromStreamReadPostHeaderData(LittleEndianDataInputStream stream) throws IOException {}

    protected void loadFromStreamReadPostVersionData(LittleEndianDataInputStream stream) throws IOException {}

    protected void loadFromStreamInitFields() {}

    protected void loadFromStreamLoadFields(int index, LittleEndianDataInputStream stream) throws IOException {}

    protected void loadFromStreamLoadExtras(LittleEndianDataInputStream stream) throws IOException {
        int numberOfExtras = stream.readInt();
        if (numberOfExtras != 0) {
            throw new IOException("Number of extras is not 0: " + numberOfExtras);
        }
    }

    public void loadFromStream(InputStream inputStream) throws IOException {
        Log.d("stream","loadFromStream() started");

        long currentTimeMillis = System.currentTimeMillis();

        this.dbVersion = 0;

        LittleEndianDataInputStream stream = new LittleEndianDataInputStream(inputStream);

        System.out.println("loadFromStream() reading header");
        String headerString = stream.readUtf8StringChars(4);
        loadFromStreamCheckHeader(headerString);

        System.out.println("loadFromStream() reading post header data");
        loadFromStreamReadPostHeaderData(stream);

        System.out.println("loadFromStream() reading DB version");
        this.dbVersion = stream.readInt();
        Log.d("loadFromStream() DB version is {}", String.valueOf(dbVersion));

        System.out.println("loadFromStream() reading post version data");
        loadFromStreamReadPostVersionData(stream);

        System.out.println("loadFromStream() reading number of items");
        this.numberOfItems = stream.readInt();
        Log.d("loadFromStream() number of items is {}", String.valueOf(numberOfItems));

        this.numbers = new long[this.numberOfItems];

        loadFromStreamInitFields();

        System.out.println("loadFromStream() reading fields");
        for (int i = 0; i < this.numberOfItems; i++) {
            this.numbers[i] = stream.readLong();
            loadFromStreamLoadFields(i, stream);
        }
//        LOG.trace("loadFromStream() finished reading fields");

//        LOG.trace("loadFromStream() reading CP");
        String dividerString = stream.readUtf8StringChars(2);
        if (!"CP".equalsIgnoreCase(dividerString)) {
            throw new IOException("CP not found. Found instead: " + dividerString);
        }

//        LOG.trace("loadFromStream() reading extras");
        loadFromStreamLoadExtras(stream);

//        LOG.trace("loadFromStream() reading endmark");
        String endmarkString = stream.readUtf8StringChars(6);
        if (!"YABEND".equalsIgnoreCase(endmarkString) && !"MTZEND".equalsIgnoreCase(endmarkString)) {
            throw new IOException("Endmark not found. Found instead: " + endmarkString);
        }

//        LOG.debug("loadFromStream() loaded slice with {} items in {} ms",
//                numberOfItems, System.currentTimeMillis() - currentTimeMillis);
    }

    @Override
    public String toString() {
        return "AbstractDatabaseDataSlice{" +
                "dbVersion=" + dbVersion +
                ", numberOfItems=" + numberOfItems +
                ", lastAccessTimestamp=" + lastAccessTimestamp +
                '}';
    }

}
