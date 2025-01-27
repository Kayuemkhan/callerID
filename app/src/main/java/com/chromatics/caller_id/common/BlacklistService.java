package com.chromatics.caller_id.common;

import static com.chromatics.caller_id.common.EventUtils.postEvent;

import android.text.TextUtils;

import com.chromatics.caller_id.common.event.BlacklistChangedEvent;
import com.chromatics.caller_id.common.event.BlacklistItemChangedEvent;
import com.chromatics.caller_id.data.db.BlacklistDao;
import com.chromatics.caller_id.data.db.BlacklistItem;

import java.util.Date;
import java.util.Objects;


public class BlacklistService {

    public interface Callback {
        void changed(boolean notEmpty);
    }

    private final Callback callback;
    private final BlacklistDao blacklistDao;

    public BlacklistService(Callback callback, BlacklistDao blacklistDao) {
        this.callback = callback;
        this.blacklistDao = blacklistDao;
    }

    public BlacklistItem getBlacklistItemForNumber(String number) {
        if (TextUtils.isEmpty(number)) return null;

        number = BlacklistUtils.cleanNumber(number);

        return blacklistDao.getFirstMatch(number);
    }

    public void save(BlacklistItem blacklistItem) {
        boolean newItem = blacklistItem.getId() == null;

        sanitize(blacklistItem);
        blacklistDao.save(blacklistItem);

        blacklistChanged(!newItem);
    }

    public void insert(BlacklistItem blacklistItem) {
        sanitize(blacklistItem);
        blacklistDao.insert(blacklistItem);

        blacklistChanged(false);
    }

    public void addCall(BlacklistItem blacklistItem, Date date) {
        sanitize(blacklistItem);

        blacklistItem.setLastCallDate(Objects.requireNonNull(date));
        blacklistItem.setNumberOfCalls(blacklistItem.getNumberOfCalls() + 1);

        blacklistDao.save(blacklistItem);

        postEvent(new BlacklistItemChangedEvent());
    }

    public void delete(Iterable<Long> keys) {
        blacklistDao.delete(keys);

        blacklistChanged(false);
    }

    private void sanitize(BlacklistItem blacklistItem) {
        blacklistItem.setInvalid(!BlacklistUtils.isValidPattern(blacklistItem.getPattern()));
        if (blacklistItem.getCreationDate() == null) blacklistItem.setCreationDate(new Date());
        if (blacklistItem.getNumberOfCalls() < 0) blacklistItem.setNumberOfCalls(0);
    }

    private void blacklistChanged(boolean itemUpdate) {
        callback.changed(blacklistDao.countValid() != 0);

        postEvent(itemUpdate ? new BlacklistItemChangedEvent() : new BlacklistChangedEvent());
    }

}
