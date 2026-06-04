package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.GlobalSearchResponse;

public interface GlobalSearchService {
    GlobalSearchResponse search(Long businessId, String query, int limit);
}
