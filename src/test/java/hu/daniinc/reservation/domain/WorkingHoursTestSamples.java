package hu.daniinc.reservation.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WorkingHoursTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static WorkingHours getWorkingHoursSample1() {
        return new WorkingHours().id(1L).dayOfWeek(1);
    }

    public static WorkingHours getWorkingHoursSample2() {
        return new WorkingHours().id(2L).dayOfWeek(2);
    }

    public static WorkingHours getWorkingHoursRandomSampleGenerator() {
        return new WorkingHours().id(longCount.incrementAndGet()).dayOfWeek(intCount.incrementAndGet());
    }
}
