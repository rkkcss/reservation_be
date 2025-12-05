package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.WorkingHours;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.repository.WorkingHoursRepository;
import hu.daniinc.reservation.service.WorkingHoursService;
import hu.daniinc.reservation.service.dto.WorkingHoursDTO;
import hu.daniinc.reservation.service.mapper.WorkingHoursMapper;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.WorkingHours}.
 */
@Service
@Transactional
public class WorkingHoursServiceImpl implements WorkingHoursService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkingHoursServiceImpl.class);

    private final WorkingHoursRepository workingHoursRepository;

    private final WorkingHoursMapper workingHoursMapper;
    private final BusinessRepository businessRepository;

    public WorkingHoursServiceImpl(
        WorkingHoursRepository workingHoursRepository,
        WorkingHoursMapper workingHoursMapper,
        BusinessRepository businessRepository
    ) {
        this.workingHoursRepository = workingHoursRepository;
        this.workingHoursMapper = workingHoursMapper;
        this.businessRepository = businessRepository;
    }

    @Override
    public WorkingHoursDTO save(WorkingHoursDTO workingHoursDTO) {
        LOG.debug("Request to save WorkingHours : {}", workingHoursDTO);
        WorkingHours workingHours = workingHoursMapper.toEntity(workingHoursDTO);
        workingHours = workingHoursRepository.save(workingHours);
        return workingHoursMapper.toDto(workingHours);
    }

    @Override
    public WorkingHoursDTO update(WorkingHoursDTO workingHoursDTO) {
        LOG.debug("Request to update WorkingHours : {}", workingHoursDTO);
        WorkingHours workingHours = workingHoursMapper.toEntity(workingHoursDTO);
        workingHours = workingHoursRepository.save(workingHours);
        return workingHoursMapper.toDto(workingHours);
    }

    @Override
    public Optional<WorkingHoursDTO> partialUpdate(WorkingHoursDTO workingHoursDTO) {
        LOG.debug("Request to partially update WorkingHours : {}", workingHoursDTO);

        return workingHoursRepository
            .findById(workingHoursDTO.getId())
            .map(existingWorkingHours -> {
                workingHoursMapper.partialUpdate(existingWorkingHours, workingHoursDTO);

                return existingWorkingHours;
            })
            .map(workingHoursRepository::save)
            .map(workingHoursMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkingHoursDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all WorkingHours");
        return workingHoursRepository.findAll(pageable).map(workingHoursMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkingHoursDTO> findOne(Long id) {
        LOG.debug("Request to get WorkingHours : {}", id);
        return workingHoursRepository.findById(id).map(workingHoursMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete WorkingHours : {}", id);
        workingHoursRepository.deleteById(id);
    }

    @Override
    public List<WorkingHoursDTO> getAllByLoggedInUser() {
        return workingHoursRepository.findByBusinessUserLogin().stream().map(workingHoursMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Void updateWorkingHours(List<WorkingHoursDTO> newHours) {
        Business business = businessRepository
            .findBusinessByLoginAndBusinessId(1L)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found"));

        // Get existing working hours
        List<WorkingHours> existingHours = workingHoursRepository.findByBusinessUserLogin();

        // Extract IDs from new DTOs
        Set<Long> newHourIds = newHours.stream().map(WorkingHoursDTO::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        // Delete hours not present in the new list
        existingHours.stream().filter(hour -> !newHourIds.contains(hour.getId())).forEach(workingHoursRepository::delete);

        // Process each DTO
        for (WorkingHoursDTO dto : newHours) {
            if (dto.getId() != null) {
                // Update existing entry
                WorkingHours existing = existingHours
                    .stream()
                    .filter(h -> h.getId().equals(dto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid working hour ID"));

                existing.setDayOfWeek(dto.getDayOfWeek());
                existing.setStartTime(dto.getStartTime());
                existing.setEndTime(dto.getEndTime());
                workingHoursRepository.save(existing);
            } else {
                // Create new entry
                WorkingHours newHour = new WorkingHours();
                newHour.setBusiness(business);
                newHour.setDayOfWeek(dto.getDayOfWeek());
                newHour.setStartTime(dto.getStartTime());
                newHour.setEndTime(dto.getEndTime());
                workingHoursRepository.save(newHour);
            }
        }

        return null;
    }
}
