package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.repository.AppointmentRepository;
import hu.daniinc.reservation.repository.OfferingRepository;
import hu.daniinc.reservation.repository.StatisticRepository;
import hu.daniinc.reservation.service.BusinessEmployeeService;
import hu.daniinc.reservation.service.BusinessService;
import hu.daniinc.reservation.service.StatisticService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.*;
import hu.daniinc.reservation.service.mapper.BusinessEmployeeMapperImpl;
import hu.daniinc.reservation.service.mapper.BusinessMapperImpl;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticServiceImpl implements StatisticService {

    private final AppointmentRepository appointmentRepository;
    private final OfferingRepository offeringRepository;
    private final StatisticRepository statisticRepository;
    private final UserService userService;
    private final BusinessService businessService;
    private final BusinessMapperImpl businessMapperImpl;
    private final BusinessEmployeeService businessEmployeeService;
    private final BusinessEmployeeMapperImpl businessEmployeeMapperImpl;

    public StatisticServiceImpl(
        AppointmentRepository appointmentRepository,
        OfferingRepository offeringRepository,
        StatisticRepository statisticRepository,
        BusinessService businessService,
        UserService userService,
        BusinessMapperImpl businessMapperImpl,
        BusinessEmployeeService businessEmployeeService,
        BusinessEmployeeMapperImpl businessEmployeeMapperImpl
    ) {
        this.appointmentRepository = appointmentRepository;
        this.offeringRepository = offeringRepository;
        this.statisticRepository = statisticRepository;
        this.userService = userService;
        this.businessService = businessService;
        this.businessMapperImpl = businessMapperImpl;
        this.businessEmployeeService = businessEmployeeService;
        this.businessEmployeeMapperImpl = businessEmployeeMapperImpl;
    }

    @Override
    public List<IncomeChartDTO> getIncomeChart(Long businessId, Instant from, Instant to, Long employeeId) {
        User loggedInUser = userService.getUserWithAuthorities().orElseThrow(() -> new RuntimeException("User not logged in"));
        Business business = businessMapperImpl.toEntity(businessService.getBusinessByLoggedInUser(businessId));

        BusinessEmployee loggedInBusinessEmployee = businessEmployeeMapperImpl.toEntity(
            businessEmployeeService.findByBusinessIdAndUserId(business.getId(), loggedInUser.getId())
        );

        boolean canViewAll =
            loggedInBusinessEmployee.hasPermission(BusinessPermission.VIEW_ALL_STATISTICS) &&
            loggedInBusinessEmployee.hasPermission(BusinessPermission.VIEW_OWN_STATISTICS);
        boolean canViewOwn = loggedInBusinessEmployee.hasPermission(BusinessPermission.VIEW_OWN_STATISTICS);

        if (canViewAll) {
            return appointmentRepository.getDailyStats(businessId, from, to, employeeId);
        } else if (canViewOwn) {
            return appointmentRepository.getDailyStats(businessId, from, to, loggedInUser.getId());
        } else {
            throw new GeneralException("Don't have permission to view statistics!", "access-denied", HttpStatus.FORBIDDEN);
        }
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
        User loggedInUser = userService.getUserWithAuthorities().orElseThrow(() -> new RuntimeException("User not logged in"));

        Business business = businessMapperImpl.toEntity(businessService.getBusinessByLoggedInUser(businessId));

        BusinessEmployee loggedInBusinessEmployee = businessEmployeeMapperImpl.toEntity(
            businessEmployeeService.findByBusinessIdAndUserId(business.getId(), loggedInUser.getId())
        );

        boolean canViewAll = loggedInBusinessEmployee.hasPermission(BusinessPermission.VIEW_ALL_STATISTICS);
        boolean canViewOwn = loggedInBusinessEmployee.hasPermission(BusinessPermission.VIEW_OWN_STATISTICS);

        if (!canViewAll && !canViewOwn) {
            throw new GeneralException("Don't have permission to view statistics!", "access-denied", HttpStatus.FORBIDDEN);
        }

        Long effectiveEmployeeId = canViewAll ? employeeId : loggedInUser.getId();

        RevenueProjection revData = statisticRepository.getRevenueAndCount(businessId, from, to, effectiveEmployeeId);
        RatingProjection ratData = statisticRepository.getRatingSummary(businessId, from, to);
        Long newClients = statisticRepository.countNewCustomers(businessId, from, to, effectiveEmployeeId);

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
        User loggedInUser = userService.getUserWithAuthorities().orElseThrow(() -> new RuntimeException("User not logged in"));

        Business business = businessMapperImpl.toEntity(businessService.getBusinessByLoggedInUser(businessId));

        BusinessEmployee loggedInBusinessEmployee = businessEmployeeMapperImpl.toEntity(
            businessEmployeeService.findByBusinessIdAndUserId(business.getId(), loggedInUser.getId())
        );

        boolean canViewAll = loggedInBusinessEmployee.hasPermission(BusinessPermission.VIEW_ALL_STATISTICS);
        boolean canViewOwn = loggedInBusinessEmployee.hasPermission(BusinessPermission.VIEW_OWN_STATISTICS);

        if (!canViewAll && !canViewOwn) {
            throw new GeneralException("Don't have permission to view statistics!", "access-denied", HttpStatus.FORBIDDEN);
        }

        Long effectiveEmployeeId = canViewAll ? employeeId : loggedInUser.getId();

        Long totalUnique = statisticRepository.countUniqueCustomers(businessId, effectiveEmployeeId, from, to);

        double returningPer = 0.0;
        double newPer = 0.0;
        String name = "no-data";
        Long bookings = 0L;
        Long newCount = 0L;

        if (totalUnique > 0) {
            Long returningCount = statisticRepository.countReturningCustomers(businessId, effectiveEmployeeId, from, to);
            newCount = totalUnique - returningCount;

            returningPer = (returningCount.doubleValue() / totalUnique) * 100;
            newPer = 100.0 - returningPer;

            List<Object[]> topData = statisticRepository.findTopCustomer(businessId, effectiveEmployeeId, from, to, PageRequest.of(0, 5));

            if (!topData.isEmpty()) {
                Object[] row = topData.get(0);
                name = (String) row[0];
                bookings = (Long) row[1];
            }

            return new CustomerDistributionDTO(returningPer, newPer, name, bookings, returningCount, newCount);
        }

        return new CustomerDistributionDTO(returningPer, newPer, name, bookings);
    }
}
