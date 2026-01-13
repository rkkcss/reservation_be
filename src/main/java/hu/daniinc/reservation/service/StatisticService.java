package hu.daniinc.reservation.service;

import hu.daniinc.reservation.service.dto.BusinessStatisticSummaryDTO;
import hu.daniinc.reservation.service.dto.CustomerDistributionDTO;
import hu.daniinc.reservation.service.dto.IncomeChartDTO;
import hu.daniinc.reservation.service.dto.TopOfferingStatisticDTO;
import java.time.Instant;
import java.util.List;

public interface StatisticService {
    List<IncomeChartDTO> getIncomeChart(Long businessId, Instant from, Instant to, Long employeeId);

    List<TopOfferingStatisticDTO> getTopOfferingsByBusinessId(Long businessId, Instant from, Instant to, Long businessEmployeeSearch);

    BusinessStatisticSummaryDTO getBusinessSummary(Long businessId, Instant from, Instant to, Long businessEmployeeSearch);

    CustomerDistributionDTO getCustomerDistribution(Long businessId, Instant from, Instant to, Long businessEmployeeSearch);
}
