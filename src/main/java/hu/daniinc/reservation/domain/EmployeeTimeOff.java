package hu.daniinc.reservation.domain;

import hu.daniinc.reservation.domain.enumeration.TimeOffStatus;
import hu.daniinc.reservation.domain.enumeration.TimeOffType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "employee_time_off")
public class EmployeeTimeOff implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "business_employee_id", nullable = false)
    private BusinessEmployee businessEmployee;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Ha null, egész napos szabadság a startDate-en (00:00-tól)
    @Column(name = "start_time")
    private LocalTime startTime;

    // Ha null, egész napos szabadság az endDate-ig (éjfélig)
    @Column(name = "end_time")
    private LocalTime endTime;

    // Kiszámolt, tényleges Instant tartomány (business időzóna alapján) - ezt használja a query-k
    @NotNull
    @Column(name = "start_instant", nullable = false)
    private Instant startInstant;

    @NotNull
    @Column(name = "end_instant", nullable = false)
    private Instant endInstant;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TimeOffType type;

    @Column(name = "note")
    private String note;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TimeOffStatus status = TimeOffStatus.ACTIVE;

    // --- getters / setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BusinessEmployee getBusinessEmployee() {
        return businessEmployee;
    }

    public void setBusinessEmployee(BusinessEmployee businessEmployee) {
        this.businessEmployee = businessEmployee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Instant getStartInstant() {
        return startInstant;
    }

    public void setStartInstant(Instant startInstant) {
        this.startInstant = startInstant;
    }

    public Instant getEndInstant() {
        return endInstant;
    }

    public void setEndInstant(Instant endInstant) {
        this.endInstant = endInstant;
    }

    public TimeOffType getType() {
        return type;
    }

    public void setType(TimeOffType type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public TimeOffStatus getStatus() {
        return status;
    }

    public void setStatus(TimeOffStatus status) {
        this.status = status;
    }
}
