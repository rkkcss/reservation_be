package hu.daniinc.reservation.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class GuestTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Guest getGuestSample1() {
        return new Guest().id(1L).name("name1").email("email1").phoneNumber("phoneNumber1");
    }

    public static Guest getGuestSample2() {
        return new Guest().id(2L).name("name2").email("email2").phoneNumber("phoneNumber2");
    }

    public static Guest getGuestRandomSampleGenerator() {
        return new Guest()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString())
            .phoneNumber(UUID.randomUUID().toString());
    }
}
