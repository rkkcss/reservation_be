package hu.daniinc.reservation.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BusinessRatingTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static BusinessRating getBusinessRatingSample1() {
        return new BusinessRating().id(1L).number(1).description("description1").imageUrl("imageUrl1");
    }

    public static BusinessRating getBusinessRatingSample2() {
        return new BusinessRating().id(2L).number(2).description("description2").imageUrl("imageUrl2");
    }

    public static BusinessRating getBusinessRatingRandomSampleGenerator() {
        return new BusinessRating()
            .id(longCount.incrementAndGet())
            .number(intCount.incrementAndGet())
            .description(UUID.randomUUID().toString())
            .imageUrl(UUID.randomUUID().toString());
    }
}
