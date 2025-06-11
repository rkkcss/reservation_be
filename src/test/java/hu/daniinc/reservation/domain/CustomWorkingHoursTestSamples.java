package hu.daniinc.reservation.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class CustomWorkingHoursTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static CustomWorkingHours getCustomWorkingHoursSample1() {
        return new CustomWorkingHours().id(1L);
    }

    public static CustomWorkingHours getCustomWorkingHoursSample2() {
        return new CustomWorkingHours().id(2L);
    }

    public static CustomWorkingHours getCustomWorkingHoursRandomSampleGenerator() {
        return new CustomWorkingHours().id(longCount.incrementAndGet());
    }
}
