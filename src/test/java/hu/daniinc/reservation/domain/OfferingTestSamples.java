package hu.daniinc.reservation.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OfferingTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Offering getOfferingSample1() {
        return new Offering().id(1L).durationMinutes(1).description("description1").title("title1");
    }

    public static Offering getOfferingSample2() {
        return new Offering().id(2L).durationMinutes(2).description("description2").title("title2");
    }

    public static Offering getOfferingRandomSampleGenerator() {
        return new Offering()
            .id(longCount.incrementAndGet())
            .durationMinutes(intCount.incrementAndGet())
            .description(UUID.randomUUID().toString())
            .title(UUID.randomUUID().toString());
    }
}
