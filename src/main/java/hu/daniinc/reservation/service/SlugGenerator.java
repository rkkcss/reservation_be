package hu.daniinc.reservation.service;

import hu.daniinc.reservation.repository.BusinessRepository;
import org.springframework.stereotype.Component;

@Component
public class SlugGenerator {

    private final BusinessRepository businessRepository;

    public SlugGenerator(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public String generateUnique(String businessName) {
        String base = businessName
            .toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9\\s-]", "") // spec. karakterek ki
            .replaceAll("\\s+", "-") // szóköz → kötőjel
            .replaceAll("-+", "-"); // dupla kötőjel ki

        String slug = base;
        int counter = 2;

        // ha már létezik, számot fűz hozzá: pizzeria-bella-2
        while (businessRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }

        return slug;
    }
}
