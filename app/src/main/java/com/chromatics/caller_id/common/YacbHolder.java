package com.chromatics.caller_id.common;

import android.annotation.SuppressLint;

import com.chromatics.caller_id.data.db.BlacklistDao;
import com.chromatics.caller_id.data.db.DbManager;
import com.chromatics.caller_id.data.db.FeaturedDatabase;


public class YacbHolder {

    private static WebService webService;
    private static DbManager dbManager;
    private static SiaMetadata siaMetadata;
    public static CommunityDatabase communityDatabase;
    private static FeaturedDatabase featuredDatabase;
    private static CommunityReviewsLoader communityReviewsLoader;

    private static BlacklistDao blacklistDao;
    private static BlacklistService blacklistService;

    private static NumberInfoService numberInfoService;

    @SuppressLint("StaticFieldLeak")
    private static NotificationService notificationService;

    @SuppressLint("StaticFieldLeak")
    private static PhoneStateHandler phoneStateHandler;

    static void setWebService(WebService webService) {
        YacbHolder.webService = webService;
    }

    static void setDbManager(DbManager dbManager) {
        YacbHolder.dbManager = dbManager;
    }

    static void setSiaMetadata(SiaMetadata siaMetadata) {
        YacbHolder.siaMetadata = siaMetadata;
    }

    static void setCommunityDatabase(CommunityDatabase communityDatabase) {
        YacbHolder.communityDatabase = communityDatabase;
    }

    static void setFeaturedDatabase(FeaturedDatabase featuredDatabase) {
        YacbHolder.featuredDatabase = featuredDatabase;
    }

    static void setCommunityReviewsLoader(CommunityReviewsLoader communityReviewsLoader) {
        YacbHolder.communityReviewsLoader = communityReviewsLoader;
    }

    static void setBlacklistDao(BlacklistDao blacklistDao) {
        YacbHolder.blacklistDao = blacklistDao;
    }

    static void setBlacklistService(BlacklistService blacklistService) {
        YacbHolder.blacklistService = blacklistService;
    }

    static void setNumberInfoService(NumberInfoService numberInfoService) {
        YacbHolder.numberInfoService = numberInfoService;
    }

    static void setNotificationService(NotificationService notificationService) {
        YacbHolder.notificationService = notificationService;
    }

    static void setPhoneStateHandler(PhoneStateHandler phoneStateHandler) {
        YacbHolder.phoneStateHandler = phoneStateHandler;
    }

    public static WebService getWebService() {
        return webService;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public static SiaMetadata getSiaMetadata() {
        return siaMetadata;
    }

    public static CommunityDatabase getCommunityDatabase() {
        return communityDatabase;
    }

    public static FeaturedDatabase getFeaturedDatabase() {
        return featuredDatabase;
    }

    public static CommunityReviewsLoader getCommunityReviewsLoader() {
        return communityReviewsLoader;
    }

    public static BlacklistDao getBlacklistDao() {
        return blacklistDao;
    }

    public static BlacklistService getBlacklistService() {
        return blacklistService;
    }

    public static NumberInfoService getNumberInfoService() {
        return numberInfoService;
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }

    public static PhoneStateHandler getPhoneStateHandler() {
        return phoneStateHandler;
    }

    public static NumberInfo getNumberInfo(String number, String countryCode) {
        return numberInfoService.getNumberInfo(number, countryCode, true);
    }

}
