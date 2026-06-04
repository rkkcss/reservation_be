package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.service.GlobalSearchService;
import hu.daniinc.reservation.service.dto.GlobalSearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/global-search")
public class GlobalSearchController {

    private final GlobalSearchService searchService;

    public GlobalSearchController(GlobalSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/{businessId}/")
    public ResponseEntity<GlobalSearchResponse> search(
        @RequestParam String q,
        @PathVariable(value = "businessId") Long businessId,
        @RequestParam(defaultValue = "5") int limit
    ) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(searchService.search(businessId, q, limit));
    }
}
