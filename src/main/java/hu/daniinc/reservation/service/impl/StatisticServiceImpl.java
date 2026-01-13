package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.repository.AppointmentRepository;
import hu.daniinc.reservation.repository.OfferingRepository;
import hu.daniinc.reservation.repository.StatisticRepository;
import hu.daniinc.reservation.service.StatisticService;
import hu.daniinc.reservation.service.dto.*;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticServiceImpl implements StatisticService {

    private final AppointmentRepository appointmentRepository;
    private final OfferingRepository offeringRepository;
    private final StatisticRepository statisticRepository;

    public StatisticServiceImpl(
        AppointmentRepository appointmentRepository,
        OfferingRepository offeringRepository,
        StatisticRepository statisticRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.offeringRepository = offeringRepository;
        this.statisticRepository = statisticRepository;
    }

    @Override
    public List<IncomeChartDTO> getIncomeChart(Long businessId, Instant from, Instant to, Long employeeId) {
        return appointmentRepository.getDailyStats(businessId, from, to, employeeId);
    }

    @Override
    public List<TopOfferingStatisticDTO> getTopOfferingsByBusinessId(
        Long businessId,
        Instant from,
        Instant to,
        Long businessEmployeeSearch
    ) {
        return offeringRepository.findTopOfferingsByBusiness(businessId, from, to, businessEmployeeSearch);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessStatisticSummaryDTO getBusinessSummary(Long businessId, Instant from, Instant to, Long employeeId) {
        RevenueProjection revData = statisticRepository.getRevenueAndCount(businessId, from, to, employeeId);

        RatingProjection ratData = statisticRepository.getRatingSummary(businessId, from, to);

        Long newClients = statisticRepository.countNewCustomers(businessId, from, to, employeeId);

        if (revData == null) revData = new RevenueProjection(0L, 0L);
        if (ratData == null) ratData = new RatingProjection(0.0, 0L);

        return new BusinessStatisticSummaryDTO(
            revData.getRevenue(),
            revData.getCount(),
            newClients != null ? newClients : 0L,
            ratData.getAverage(),
            ratData.getCount()
        );
    }

    @Override
    public CustomerDistributionDTO getCustomerDistribution(Long businessId, Instant from, Instant to, Long employeeId) {
        Long totalUnique = statisticRepository.countUniqueCustomers(businessId, employeeId, from, to);

        double returningPer = 0.0;
        double newPer = 0.0;
        String name = "no-data";
        Long bookings = 0L;

        if (totalUnique > 0) {
            Long returningCount = statisticRepository.countReturningCustomers(businessId, employeeId);

            returningPer = (returningCount.doubleValue() / totalUnique) * 100;
            newPer = 100.0 - returningPer;

            List<Object[]> topData = statisticRepository.findTopCustomer(businessId, employeeId, PageRequest.of(0, 1));
            if (!topData.isEmpty()) {
                Object[] row = topData.get(0);
                name = (String) row[0];
                bookings = (Long) row[1];
            }
        }

        return new CustomerDistributionDTO(returningPer, newPer, name, bookings);
    }
}
