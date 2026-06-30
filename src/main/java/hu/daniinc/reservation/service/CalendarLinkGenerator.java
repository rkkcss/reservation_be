package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.Appointment;
import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;

public class CalendarLinkGenerator {

    public static String generateCalendarLink(
        String title,
        LocalDateTime startDateTime,
        int durationMinutes,
        String details,
        String location
    ) {
        try {
            // 1. Dátumok formázása a Google által elvárt formátumra (pl: 20260715T140000)
            LocalDateTime endDateTime = startDateTime.plusMinutes(durationMinutes);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

            String startFormatted = startDateTime.format(formatter);
            String endFormatted = endDateTime.format(formatter);
            String datesParam = startFormatted + "/" + endFormatted;

            // 2. Szöveges adatok URL-kódolása (hogy a szóközökből %20 legyen, stb.)
            String encodedText = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String encodedDetails = URLEncoder.encode(details, StandardCharsets.UTF_8);
            String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);

            // 3. A teljes URL összerakása
            return String.format(
                "https://calendar.google.com/calendar/render?action=TEMPLATE&text=%s&dates=%s&details=%s&location=%s",
                encodedText,
                datesParam,
                encodedDetails,
                encodedLocation
            );
        } catch (Exception e) {
            throw new RuntimeException("Hiba a Google Calendar link generálása közben", e);
        }
    }

    public static ByteArrayResource generateICSalAttachment(Appointment appointment) {
        // Dátumok formázása UTC időzónában az .ics szabvány szerint
        DateTimeFormatter icsFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(java.time.ZoneOffset.UTC);

        String startFormatted = icsFormatter.format(appointment.getStartDate());
        String endFormatted = icsFormatter.format(
            appointment.getStartDate().plusSeconds(appointment.getOffering().getDurationMinutes() * 60L)
        );

        // ICS tartalom összefűzése
        String icsContent =
            "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "PRODID:-//Reserbio//NONSGML//HU\n" +
            "BEGIN:VEVENT\n" +
            "UID:appointment-" +
            appointment.getId() +
            "@reserbio.com\n" +
            "DTSTAMP:" +
            startFormatted +
            "\n" +
            "DTSTART:" +
            startFormatted +
            "\n" +
            "DTEND:" +
            endFormatted +
            "\n" +
            "SUMMARY:" +
            appointment.getOffering().getTitle() +
            "\n" +
            "LOCATION:Kis utca 2.\n" + // Ide jöhet majd dinamikusan a szalon címe is
            "END:VEVENT\n" +
            "END:VCALENDAR";

        return new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8));
    }
}
