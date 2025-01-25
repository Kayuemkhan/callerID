package com.chromatics.caller_id.models;

import com.chromatics.caller_id.common.NumberFilter;
import com.chromatics.caller_id.utils.LittleEndianDataInputStream;
import com.chromatics.caller_id.utils.LittleEndianDataOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class CommunityDatabaseDataSlice extends AbstractDatabaseDataSlice<CommunityDatabaseItem> {

    public static class SliceIndexMap {
        private IntList[] shortSliceIdToIndexMap;
        private IntList[] shortSliceIdToIndexToDeleteMap;

        public Index takeIndex(int sliceId) {
            Index index = new Index();
            index.numberIndexList = shortSliceIdToIndexMap[sliceId];
            index.numbersToDeleteIndexList = shortSliceIdToIndexToDeleteMap[sliceId];

            shortSliceIdToIndexMap[sliceId] = null;
            shortSliceIdToIndexToDeleteMap[sliceId] = null;

            return index;
        }
    }

    public static class Index {
        private IntList numberIndexList;
        private IntList numbersToDeleteIndexList;
    }

    private static final Logger LOG = LoggerFactory.getLogger(CommunityDatabaseDataSlice.class);

    private byte[] positiveRatingsCounts;
    private byte[] negativeRatingsCounts;
    private byte[] neutralRatingsCounts;
    private byte[] unknownData;
    private byte[] categories;

    private long[] numbersToDelete;

    public SliceIndexMap generateShortSliceIndexMap(NumberFilter numberFilter) {
        IntList[] shortSliceIdToIndexMap = new IntList[100];
        IntList[] shortSliceIdToIndexToDeleteMap = new IntList[100];

        for (int i = 0; i <= 99; i++) {
            shortSliceIdToIndexMap[i] = new IntList();
            shortSliceIdToIndexToDeleteMap[i] = new IntList();
        }

        for (int i = 0; i < numbers.length; i++) {
            String numberString = String.valueOf(numbers[i]);
            if (numberString.length() > 1 && passesFilter(numberString, numberFilter)) {
                int sliceId = Integer.parseInt(numberString.substring(0, 2));
                shortSliceIdToIndexMap[sliceId].add(i);
            }
        }
        for (int i = 0; i < numbersToDelete.length; i++) {
            String numberString = String.valueOf(numbersToDelete[i]);
            if (numberString.length() > 1 && passesFilter(numberString, numberFilter)) {
                int sliceId = Integer.parseInt(numberString.substring(0, 2));
                shortSliceIdToIndexToDeleteMap[sliceId].add(i);
            }
        }

        SliceIndexMap sliceIndexMap = new SliceIndexMap();
        sliceIndexMap.shortSliceIdToIndexMap = shortSliceIdToIndexMap;
        sliceIndexMap.shortSliceIdToIndexToDeleteMap = shortSliceIdToIndexToDeleteMap;

        return sliceIndexMap;
    }

    public Index generateFilteredIndex(NumberFilter numberFilter) {
        IntList indexMap = new IntList();
        IntList indexToDeleteMap = new IntList();

        for (int i = 0; i < numbers.length; i++) {
            String numberString = String.valueOf(numbers[i]);
            if (passesFilter(numberString, numberFilter)) {
                indexMap.add(i);
            }
        }
        for (int i = 0; i < numbersToDelete.length; i++) {
            String numberString = String.valueOf(numbersToDelete[i]);
            if (passesFilter(numberString, numberFilter)) {
                indexToDeleteMap.add(i);
            }
        }

        Index index = new Index();
        index.numberIndexList = indexMap;
        index.numbersToDeleteIndexList = indexToDeleteMap;
        return index;
    }

    private boolean passesFilter(String number, NumberFilter numberFilter) {
        return numberFilter == null || (numberFilter.isDetailed()
                ? numberFilter.keepNumber(number) : numberFilter.keepPrefix(number));
    }

    public boolean partialClone(CommunityDatabaseDataSlice source, Index index) {
        IntList numberIndexList = index.numberIndexList;
        this.numberOfItems = numberIndexList.size();
        this.dbVersion = source.dbVersion;
        this.numbers = new long[this.numberOfItems];
        this.positiveRatingsCounts = new byte[this.numberOfItems];
        this.negativeRatingsCounts = new byte[this.numberOfItems];
        this.neutralRatingsCounts = new byte[this.numberOfItems];
        this.unknownData = new byte[this.numberOfItems];
        this.categories = new byte[this.numberOfItems];
        for (int i = 0; i < this.numberOfItems; i++) {
            int k = numberIndexList.getInt(i);
            this.numbers[i] = source.numbers[k];
            this.positiveRatingsCounts[i] = source.positiveRatingsCounts[k];
            this.negativeRatingsCounts[i] = source.negativeRatingsCounts[k];
            this.neutralRatingsCounts[i] = source.neutralRatingsCounts[k];
            this.unknownData[i] = source.unknownData[k];
            this.categories[i] = source.categories[k];
        }

        IntList numbersToDeleteIndexList = index.numbersToDeleteIndexList;
        this.numbersToDelete = new long[numbersToDeleteIndexList.size()];
        for (int i = 0; i < numbersToDelete.length; i++) {
            this.numbersToDelete[i] = source.numbersToDelete[numbersToDeleteIndexList.getInt(i)];
        }

        return this.numberOfItems > 0 || numbersToDelete.length > 0;
    }

    @Override
    protected CommunityDatabaseItem getDbItemByNumberInternal(long number, int index) {
        CommunityDatabaseItem communityDatabaseItem = new CommunityDatabaseItem();
        communityDatabaseItem.setNumber(numbers[index]);
        communityDatabaseItem.setPositiveRatingsCount(positiveRatingsCounts[index] & 255);
        communityDatabaseItem.setNegativeRatingsCount(negativeRatingsCounts[index] & 255);
        communityDatabaseItem.setNeutralRatingsCount(neutralRatingsCounts[index] & 255);
        communityDatabaseItem.setUnknownData(unknownData[index] & 255);
        communityDatabaseItem.setCategory(categories[index] & 255);
        return communityDatabaseItem;
    }

    @Override
    protected void loadFromStreamCheckHeader(String header) throws IOException {
        if (!"YABF".equalsIgnoreCase(header) && !"MTZF".equalsIgnoreCase(header)
                && !"MTZD".equalsIgnoreCase(header)) {
            throw new IOException("Invalid header. Actual value: " + header);
        }
    }

    @Override
    protected void loadFromStreamReadPostHeaderData(LittleEndianDataInputStream stream) throws IOException {
        byte b = stream.readByte(); // ignored
        LOG.trace("loadFromStreamReadPostHeaderData() b={}", b);
    }

    @Override
    protected void loadFromStreamReadPostVersionData(LittleEndianDataInputStream stream) throws IOException {
        String s = stream.readUtf8StringChars(2); // ignored
        int i = stream.readInt(); // ignored
        LOG.trace("loadFromStreamReadPostVersionData() s={}, i={}", s, i);
    }

    @Override
    protected void loadFromStreamInitFields() {
        positiveRatingsCounts = new byte[numberOfItems];
        negativeRatingsCounts = new byte[numberOfItems];
        neutralRatingsCounts = new byte[numberOfItems];
        unknownData = new byte[numberOfItems];
        categories = new byte[numberOfItems];
    }

    @Override
    protected void loadFromStreamLoadFields(int index, LittleEndianDataInputStream stream) throws IOException {
        positiveRatingsCounts[index] = stream.readByte();
        negativeRatingsCounts[index] = stream.readByte();
        neutralRatingsCounts[index] = stream.readByte();
        unknownData[index] = stream.readByte();
        categories[index] = stream.readByte();
    }

    @Override
    protected void loadFromStreamLoadExtras(LittleEndianDataInputStream stream) throws IOException {
        int numberOfItemsToDelete = stream.readInt();
        LOG.trace("loadFromStreamLoadExtras() numberOfItemsToDelete={}", numberOfItemsToDelete);

        numbersToDelete = new long[numberOfItemsToDelete];
        for (int i = 0; i < numberOfItemsToDelete; i++) {
            numbersToDelete[i] = stream.readLong();
        }
    }

    public void writeMerged(CommunityDatabaseDataSlice newSlice, BufferedOutputStream outputStream) throws IOException {
        LOG.debug("writeMerged() started with newSlice={}", newSlice);

        int realNumberOfItems = 0;
        int newNumberOfItems = numberOfItems;
        if (newSlice != null) {
            if (numberOfItems > 0) {
                for (long n : newSlice.numbers) {
                    if (indexOf(n) < 0) {
                        newNumberOfItems++;
                    }
                }
                for (long n : newSlice.numbersToDelete) {
                    if (indexOf(n) < 0) {
                        newNumberOfItems++;
                    }
                }
            } else {
                newNumberOfItems = newSlice.numberOfItems + newSlice.numbersToDelete.length;
            }
        }

        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(outputStream);

        writeHeader(stream);
        writePostHeaderData(stream);
        writeVersion(stream, newSlice != null ? newSlice.dbVersion : dbVersion);
        writePostVersionData(stream);

        writeNumberOfItems(stream, newNumberOfItems);

        /*
          newSlice.numbersToDelete[x] goes first.
          newSlice.numbers[x] goes second (*does not* replace equal newSlice.numbersToDelete[x]).
          numbers[x] goes last (*is replaced* by either of the previous two).
         */

        int sourceIndex = 0;
        int newIndex = 0;
        int newDeletedIndex = 0;

        while (true) {
            boolean hasSourceNumber = sourceIndex < numberOfItems;
            long sourceNumber = hasSourceNumber ? numbers[sourceIndex]: 0;

            boolean hasNewNumber = newSlice != null && newIndex < newSlice.numberOfItems;
            long newNumber = hasNewNumber ? newSlice.numbers[newIndex] : 0;

            boolean hasNewDeletedNumber = newSlice != null && newDeletedIndex < newSlice.numbersToDelete.length;
            long newDeletedNumber = hasNewDeletedNumber ? newSlice.numbersToDelete[newDeletedIndex] : 0;

            boolean replacedSourceNumber = false;

            if (hasNewDeletedNumber
                    && (!hasNewNumber || newDeletedNumber <= newNumber)
                    && (!hasSourceNumber || newDeletedNumber <= sourceNumber)) {
                byte zero = (byte) 0;
                writeItem(stream, newDeletedNumber, zero, zero, zero, zero, zero);

                if (hasSourceNumber && newDeletedNumber == sourceNumber) {
                    replacedSourceNumber = true;
                }

                newDeletedIndex++;
            } else if (hasNewNumber && (!hasSourceNumber || newNumber <= sourceNumber)) {
                writeItem(stream, newNumber,
                        newSlice.positiveRatingsCounts[newIndex],
                        newSlice.negativeRatingsCounts[newIndex],
                        newSlice.neutralRatingsCounts[newIndex],
                        newSlice.unknownData[newIndex],
                        newSlice.categories[newIndex]);

                if (hasSourceNumber && newNumber == sourceNumber) {
                    replacedSourceNumber = true;
                }

                newIndex++;
            } else if (hasSourceNumber) {
                writeItem(stream, numbers[sourceIndex],
                        positiveRatingsCounts[sourceIndex],
                        negativeRatingsCounts[sourceIndex],
                        neutralRatingsCounts[sourceIndex],
                        unknownData[sourceIndex],
                        categories[sourceIndex]);

                sourceIndex++;
            } else {
                break;
            }

            realNumberOfItems++;

            if (replacedSourceNumber) {
                sourceIndex++;
            }
        }

        if (realNumberOfItems != newNumberOfItems) {
            LOG.error("writeMerged() realNumberOfItems={}, newNumberOfItems={}, dbVersion={}, newSlice.dbVersion={}",
                    realNumberOfItems, newNumberOfItems, dbVersion, newSlice != null ? newSlice.dbVersion : 0);
            throw new IOException("writeMerged results in an invalid number of items!" +
                    " Expected=" + newNumberOfItems + ", actual=" + realNumberOfItems);
        }

        writeDivider(stream);
        writeExtras(stream);
        writeEndMark(stream);
    }

    private void writeHeader(LittleEndianDataOutputStream stream) throws IOException {
        stream.writeUtf8StringChars("YABF");
    }

    private void writePostHeaderData(LittleEndianDataOutputStream stream) throws IOException {
        stream.writeByte((byte) 1);
    }

    private void writeVersion(LittleEndianDataOutputStream stream, int i) throws IOException {
        stream.writeInt(i);
    }

    private void writePostVersionData(LittleEndianDataOutputStream stream) throws IOException {
        stream.writeUtf8StringChars("ww");
        stream.writeInt(0);
    }

    private void writeNumberOfItems(LittleEndianDataOutputStream stream, int newNumberOfItems) throws IOException {
        stream.writeInt(newNumberOfItems);
    }

    private void writeItem(LittleEndianDataOutputStream stream,
                           long number,
                           byte positiveRatingsCount,
                           byte negativeRatingsCount,
                           byte neutralRatingsCount,
                           byte unknownData,
                           byte category) throws IOException {
        stream.writeLong(number);
        stream.writeByte(positiveRatingsCount);
        stream.writeByte(negativeRatingsCount);
        stream.writeByte(neutralRatingsCount);
        stream.writeByte(unknownData);
        stream.writeByte(category);
    }

    private void writeDivider(LittleEndianDataOutputStream stream) throws IOException {
        stream.writeUtf8StringChars("CP");
    }

    private void writeExtras(LittleEndianDataOutputStream stream) throws IOException {
        stream.writeInt(0);
    }

    private void writeEndMark(LittleEndianDataOutputStream stream) throws IOException {
        stream.writeUtf8StringChars("YABEND");
    }

    // a dumb auto-growing list-like array
    private static class IntList {
        private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

        private static final int[] EMPTY = {};

        private int[] arr;
        private int size;

        public IntList() {
            arr = EMPTY;
        }

        public void add(int value) {
            ensureCapacity(size + 1);
            arr[size++] = value;
        }

        public int getInt(int index) {
            if (index >= size) throw new IndexOutOfBoundsException(index + " >= " + size);
            return arr[index];
        }

        public int size() {
            return size;
        }

        private void ensureCapacity(int capacity) {
            if (capacity <= arr.length) return;

            int[] newArr;
            if (arr.length == 0) {
                newArr = new int[10];
            } else {
                capacity = (int) Math.max(capacity, Math.min((long) arr.length + arr.length / 2, MAX_ARRAY_SIZE));
                newArr = Arrays.copyOf(arr, capacity);
            }
            arr = newArr;
        }
    }

}
