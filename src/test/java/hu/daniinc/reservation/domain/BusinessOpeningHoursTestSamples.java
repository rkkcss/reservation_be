package hu.daniinc.reservation.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BusinessOpeningHoursTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static BusinessOpeningHours getBusinessOpeningHoursSample1() {
        return new BusinessOpeningHours().id(1L).dayOfWeek(1);
    }

    public static BusinessOpeningHours getBusinessOpeningHoursSample2() {
        return new BusinessOpeningHours().id(2L).dayOfWeek(2);
    }

    public static BusinessOpeningHours getBusinessOpeningHoursRandomSampleGenerator() {
        return new BusinessOpeningHours().id(longCount.incrementAndGet()).dayOfWeek(intCount.incrementAndGet());
    }
}
