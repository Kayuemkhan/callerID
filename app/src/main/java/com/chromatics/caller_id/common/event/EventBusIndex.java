package com.chromatics.caller_id.common.event;

import com.chromatics.caller_id.common.EventUtils;
import com.chromatics.caller_id.ui.MainActivity;

import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbus.meta.SimpleSubscriberInfo;
import org.greenrobot.eventbus.meta.SubscriberInfo;
import org.greenrobot.eventbus.meta.SubscriberInfoIndex;
import org.greenrobot.eventbus.meta.SubscriberMethodInfo;

import java.util.HashMap;
import java.util.Map;

public class EventBusIndex implements SubscriberInfoIndex {
    private static final Map<Class<?>, SubscriberInfo> SUBSCRIBER_INDEX;

    static {
        SUBSCRIBER_INDEX = new HashMap<>();

//        putIndex(new SimpleSubscriberInfo(BlacklistActivity.class, true, new SubscriberMethodInfo[] {
//            new SubscriberMethodInfo("onBlacklistChanged",
//                    BlacklistChangedEvent.class, ThreadMode.MAIN_ORDERED),
//        }));

        putIndex(new SimpleSubscriberInfo(MainActivity.class, true, new SubscriberMethodInfo[] {
            new SubscriberMethodInfo("onCallEvent", CallEndedEvent.class,
                    ThreadMode.MAIN_ORDERED),
            new SubscriberMethodInfo("onMainDbDownloadFinished",
                    MainDbDownloadFinishedEvent.class,
                            ThreadMode.MAIN_ORDERED),
            new SubscriberMethodInfo("onSecondaryDbUpdateFinished",
                    SecondaryDbUpdateFinished.class, ThreadMode.MAIN_ORDERED),
        }));



        putIndex(new SimpleSubscriberInfo(EventUtils.class, true, new SubscriberMethodInfo[] {
            new SubscriberMethodInfo("onSubscriberExceptionEvent",
                    org.greenrobot.eventbus.SubscriberExceptionEvent.class),
        }));



    }

    private static void putIndex(SubscriberInfo info) {
        SUBSCRIBER_INDEX.put(info.getSubscriberClass(), info);
    }

    @Override
    public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {
        SubscriberInfo info = SUBSCRIBER_INDEX.get(subscriberClass);
        if (info != null) {
            return info;
        } else {
            return null;
        }
    }
}
