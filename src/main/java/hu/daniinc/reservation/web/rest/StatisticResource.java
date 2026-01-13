package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.service.StatisticService;
import hu.daniinc.reservation.service.dto.BusinessStatisticSummaryDTO;
import hu.daniinc.reservation.service.dto.CustomerDistributionDTO;
import hu.daniinc.reservation.service.dto.IncomeChartDTO;
import hu.daniinc.reservation.service.dto.TopOfferingStatisticDTO;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistic")
public class StatisticResource {

    private final StatisticService statisticService;

    public StatisticResource(final StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("/business/{businessId}/income")
    public ResponseEntity<List<IncomeChartDTO>> getIncomeByBusinessId(
        @PathVariable Long businessId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam String businessEmployeeSearch
    ) {
        Long employeeId = "all".equalsIgnoreCase(businessEmployeeSearch) ? null : Long.parseLong(businessEmployeeSearch);
        return ResponseEntity.ok(statisticService.getIncomeChart(businessId, from, to, employeeId));
    }

    @GetMapping("/business/{businessId}/top-offerings")
    public ResponseEntity<List<TopOfferingStatisticDTO>> getTopOfferingList(
        @PathVariable Long businessId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam String businessEmployeeSearch
    ) {
        Long employeeId = "all".equalsIgnoreCase(businessEmployeeSearch) ? null : Long.parseLong(businessEmployeeSearch);

        return ResponseEntity.ok(statisticService.getTopOfferingsByBusinessId(businessId, from, to, employeeId));
    }

    @GetMapping("/business/{businessId}/summary")
    public ResponseEntity<BusinessStatisticSummaryDTO> getBusinessSummaryByBusinessId(
        @PathVariable Long businessId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam String businessEmployeeSearch
    ) {
        Long employeeId = "all".equalsIgnoreCase(businessEmployeeSearch) ? null : Long.parseLong(businessEmployeeSearch);
        return ResponseEntity.ok(statisticService.getBusinessSummary(businessId, from, to, employeeId));
    }

    @GetMapping("/business/{businessId}/customer-distribution")
    public ResponseEntity<CustomerDistributionDTO> getCustomerDistributionByBusinessId(
        @PathVariable Long businessId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam String businessEmployeeSearch
    ) {
        Long employeeId = "all".equalsIgnoreCase(businessEmployeeSearch) ? null : Long.parseLong(businessEmployeeSearch);
        return ResponseEntity.ok(statisticService.getCustomerDistribution(businessId, from, to, employeeId));
    }
}
