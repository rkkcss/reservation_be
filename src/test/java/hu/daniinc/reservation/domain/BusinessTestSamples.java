package hu.daniinc.reservation.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BusinessTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Business getBusinessSample1() {
        return new Business()
            .id(1L)
            .name("name1")
            .description("description1")
            .address("address1")
            .phoneNumber("phoneNumber1")
            .breakBetweenAppointmentsMin(1)
            .logo("logo1")
            .bannerUrl("bannerUrl1");
    }

    public static Business getBusinessSample2() {
        return new Business()
            .id(2L)
            .name("name2")
            .description("description2")
            .address("address2")
            .phoneNumber("phoneNumber2")
            .breakBetweenAppointmentsMin(2)
            .logo("logo2")
            .bannerUrl("bannerUrl2");
    }

    public static Business getBusinessRandomSampleGenerator() {
        return new Business()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .address(UUID.randomUUID().toString())
            .phoneNumber(UUID.randomUUID().toString())
            .breakBetweenAppointmentsMin(intCount.incrementAndGet())
            .logo(UUID.randomUUID().toString())
            .bannerUrl(UUID.randomUUID().toString());
    }
}
