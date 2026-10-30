package me.kall.narutooverlay.event;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class GCTracker {
    private static final AtomicReference<Instant> TIME = new AtomicReference<>();
    private static final AtomicLong DURATION = new AtomicLong();
    private static final AtomicReference<String> NAME = new AtomicReference<>();

    public static void setup() {
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (bean instanceof NotificationEmitter emitter) {
                emitter.addNotificationListener((notification, handback) -> {
                    if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                        GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                        GcInfo gcInfo = info.getGcInfo();

                        TIME.set(Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime() + gcInfo.getStartTime()));
                        DURATION.set(gcInfo.getDuration());
                        NAME.set(info.getGcName());
                    }
                }, null, null);
            }
        }
    }

    public static Instant time() {
        return TIME.get();
    }

    public static long duration() {
        return DURATION.get();
    }

    public static String name() {
        return NAME.get();
    }
}
