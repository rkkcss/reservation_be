package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.service.AppointmentService;
import hu.daniinc.reservation.service.GlobalSearchService;
import hu.daniinc.reservation.service.GuestService;
import hu.daniinc.reservation.service.dto.AppointmentDTO;
import hu.daniinc.reservation.service.dto.GlobalSearchResponse;
import hu.daniinc.reservation.service.dto.GuestDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private final GuestService guestService;
    private final AppointmentService appointmentService;

    public GlobalSearchServiceImpl(GuestService guestService, AppointmentService appointmentService) {
        this.guestService = guestService;
        this.appointmentService = appointmentService;
    }

    @Override
    public GlobalSearchResponse search(Long businessId, String query, int limit) {
        List<GuestDTO> guests = guestService.searchForGlobal(businessId, query, 5);
        List<AppointmentDTO> appointments = appointmentService.searchGlobal(businessId, query, 5);
        long total = guests.size();

        return new GlobalSearchResponse(guests, appointments, total);
    }
}
