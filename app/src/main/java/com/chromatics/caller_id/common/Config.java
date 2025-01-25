package com.chromatics.caller_id.common;

import static com.chromatics.caller_id.common.SiaConstants.CALLER_ID_PROPERTIES;
import static com.chromatics.caller_id.common.SiaConstants.SIA_PATH_PREFIX;
import static com.chromatics.caller_id.common.SiaConstants.SIA_SECONDARY_PATH_PREFIX;


import android.content.Context;

import com.chromatics.caller_id.Storage;
import com.chromatics.caller_id.data.db.BlacklistDao;
import com.chromatics.caller_id.data.db.DbDownloader;
import com.chromatics.caller_id.data.db.DbManager;
import com.chromatics.caller_id.data.db.DbUpdateRequester;
import com.chromatics.caller_id.data.db.FeaturedDatabase;
import com.chromatics.caller_id.data.db.YacbDaoSessionFactory;
import com.chromatics.caller_id.utils.DbFilteringUtils;
import com.chromatics.caller_id.utils.SettingsImpl;
import com.chromatics.caller_id.utils.SystemUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


import okhttp3.OkHttp;
import okhttp3.OkHttpClient;

public class Config {

    private static class WSParameterProvider extends WebService.DefaultWSParameterProvider {
        final com.chromatics.caller_id.utils.Settings settings;
        final SiaMetadata siaMetadata;
        final CommunityDatabase communityDatabase;

        volatile String appId;
        volatile long appIdTimestamp;

        WSParameterProvider(com.chromatics.caller_id.utils.Settings settings,
                            SiaMetadata siaMetadata, CommunityDatabase communityDatabase) {
            this.settings = settings;
            this.siaMetadata = siaMetadata;
            this.communityDatabase = communityDatabase;
        }

        @Override
        public String getAppId() {
            String appId = this.appId;
            if (appId != null && System.nanoTime() >
                    appIdTimestamp + TimeUnit.MINUTES.toNanos(5)) {
                appId = null;
            }

            if (appId == null) {
                this.appId = appId = String.valueOf(UUID.randomUUID());
                appIdTimestamp = System.nanoTime();
            }

            return appId;
        }

        @Override
        public int getAppVersion() {
            return siaMetadata.getSiaAppVersion();
        }

        @Override
        public String getOkHttpVersion() {
            return siaMetadata.getSiaOkHttpVersion();
        }

        @Override
        public int getDbVersion() {
            return communityDatabase.getEffectiveDbVersion();
        }

        @Override
        public SiaMetadata.Country getCountry() {
            return siaMetadata.getCountry(settings.getCountryCode());
        }
    }

    public static void init(Context context, com.chromatics.caller_id.utils.Settings settings) {
        Storage storage = new AndroidStorage(context);
        Settings siaSettings
                = new SettingsImpl(new AndroidProperties(context, CALLER_ID_PROPERTIES));

//        OkHttp okHttpClientFactory = () -> {
//            DeferredInit.initNetwork();
//            return new OkHttpClient();
//        };
//
//        CommunityDatabase communityDatabase = new CommunityDatabase(
//                storage, AbstractDatabase.Source.ANY, SIA_PATH_PREFIX,
//                SIA_SECONDARY_PATH_PREFIX, siaSettings);
//        YacbHolder.setCommunityDatabase(communityDatabase);
//
//        SiaMetadata siaMetadata = new SiaMetadata(storage, SIA_PATH_PREFIX,
//                communityDatabase::isUsingInternal);
//        YacbHolder.setSiaMetadata(siaMetadata);

//        FeaturedDatabase featuredDatabase = new FeaturedDatabase(
//                storage, AbstractDatabase.Source.ANY, SIA_PATH_PREFIX);
//        YacbHolder.setFeaturedDatabase(featuredDatabase);

//        WSParameterProvider wsParameterProvider = new WSParameterProvider(
//                settings, siaMetadata, communityDatabase);
//
//        WebService webService = new WebService(wsParameterProvider, okHttpClientFactory);
//        YacbHolder.setWebService(webService);
//
//        YacbHolder.setDbManager(new DbManager(storage, SIA_PATH_PREFIX,
//                new DbDownloader(okHttpClientFactory), new DbUpdateRequester(webService),
//                communityDatabase));
//
//        YacbHolder.getDbManager().setNumberFilter(DbFilteringUtils.getNumberFilter(settings));
//
//        YacbHolder.setCommunityReviewsLoader(new CommunityReviewsLoader(webService));

        YacbDaoSessionFactory daoSessionFactory = new YacbDaoSessionFactory(context, "YACB");

        BlacklistDao blacklistDao = new BlacklistDao(daoSessionFactory::getDaoSession);
        YacbHolder.setBlacklistDao(blacklistDao);

        BlacklistService blacklistService = new BlacklistService(
                settings::setBlacklistIsNotEmpty, blacklistDao);
        YacbHolder.setBlacklistService(blacklistService);

        ContactsProvider contactsProvider = new ContactsProvider() {
            @Override
            public ContactItem get(String number) {
                return settings.getUseContacts() ? ContactsHelper.getContact(context, number) : null;
            }

            @Override
            public boolean isInLimitedMode() {
                return !SystemUtils.isUserUnlocked(context);
            }
        };

//        NumberInfoService numberInfoService = new NumberInfoService(
//                settings, NumberUtils::isHiddenNumber, NumberUtils::normalizeNumber,
//                communityDatabase, featuredDatabase, contactsProvider, blacklistService);
//        YacbHolder.setNumberInfoService(numberInfoService);

        NotificationService notificationService = new NotificationService(context);
        YacbHolder.setNotificationService(notificationService);
//
//        YacbHolder.setPhoneStateHandler(
//                new PhoneStateHandler(context, settings, numberInfoService, notificationService));
    }

}
