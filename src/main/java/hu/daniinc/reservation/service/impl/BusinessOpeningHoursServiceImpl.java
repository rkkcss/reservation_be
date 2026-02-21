package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.BusinessOpeningHours;
import hu.daniinc.reservation.domain.WorkingHours;
import hu.daniinc.reservation.repository.BusinessOpeningHoursRepository;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.service.BusinessOpeningHoursService;
import hu.daniinc.reservation.service.dto.BusinessOpeningHoursDTO;
import hu.daniinc.reservation.service.dto.WorkingHoursDTO;
import hu.daniinc.reservation.service.mapper.BusinessOpeningHoursMapper;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
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
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.BusinessOpeningHours}.
 */
@Service
@Transactional
public class BusinessOpeningHoursServiceImpl implements BusinessOpeningHoursService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessOpeningHoursServiceImpl.class);

    private final BusinessOpeningHoursRepository businessOpeningHoursRepository;

    private final BusinessOpeningHoursMapper businessOpeningHoursMapper;
    private final BusinessRepository businessRepository;

    public BusinessOpeningHoursServiceImpl(
        BusinessOpeningHoursRepository businessOpeningHoursRepository,
        BusinessOpeningHoursMapper businessOpeningHoursMapper,
        BusinessRepository businessRepository
    ) {
        this.businessOpeningHoursRepository = businessOpeningHoursRepository;
        this.businessOpeningHoursMapper = businessOpeningHoursMapper;
        this.businessRepository = businessRepository;
    }

    @Override
    public BusinessOpeningHoursDTO save(BusinessOpeningHoursDTO businessOpeningHoursDTO) {
        LOG.debug("Request to save BusinessOpeningHours : {}", businessOpeningHoursDTO);
        BusinessOpeningHours businessOpeningHours = businessOpeningHoursMapper.toEntity(businessOpeningHoursDTO);
        businessOpeningHours = businessOpeningHoursRepository.save(businessOpeningHours);
        return businessOpeningHoursMapper.toDto(businessOpeningHours);
    }

    @Override
    public BusinessOpeningHoursDTO update(BusinessOpeningHoursDTO businessOpeningHoursDTO) {
        LOG.debug("Request to update BusinessOpeningHours : {}", businessOpeningHoursDTO);
        BusinessOpeningHours businessOpeningHours = businessOpeningHoursMapper.toEntity(businessOpeningHoursDTO);
        businessOpeningHours = businessOpeningHoursRepository.save(businessOpeningHours);
        return businessOpeningHoursMapper.toDto(businessOpeningHours);
    }

    @Override
    public Optional<BusinessOpeningHoursDTO> partialUpdate(BusinessOpeningHoursDTO businessOpeningHoursDTO) {
        LOG.debug("Request to partially update BusinessOpeningHours : {}", businessOpeningHoursDTO);

        return businessOpeningHoursRepository
            .findById(businessOpeningHoursDTO.getId())
            .map(existingBusinessOpeningHours -> {
                businessOpeningHoursMapper.partialUpdate(existingBusinessOpeningHours, businessOpeningHoursDTO);

                return existingBusinessOpeningHours;
            })
            .map(businessOpeningHoursRepository::save)
            .map(businessOpeningHoursMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BusinessOpeningHoursDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all BusinessOpeningHours");
        return businessOpeningHoursRepository.findAll(pageable).map(businessOpeningHoursMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BusinessOpeningHoursDTO> findOne(Long id) {
        LOG.debug("Request to get BusinessOpeningHours : {}", id);
        return businessOpeningHoursRepository.findById(id).map(businessOpeningHoursMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete BusinessOpeningHours : {}", id);
        businessOpeningHoursRepository.deleteById(id);
    }

    @Override
    public List<BusinessOpeningHoursDTO> findAllByBusinessId(Long businessId) {
        return businessOpeningHoursRepository
            .findAllByBusinessId(businessId)
            .stream()
            .map(businessOpeningHoursMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public List<BusinessOpeningHoursDTO> saveOpeningHoursList(Long businessId, List<BusinessOpeningHoursDTO> newHours) {
        List<BusinessOpeningHours> existingHours = businessOpeningHoursRepository.findAllByBusinessId(businessId);

        // Extract IDs from new DTOs
        List<Long> newHourIds = newHours.stream().map(BusinessOpeningHoursDTO::getId).filter(Objects::nonNull).toList();

        // Delete hours not present in the new list
        existingHours.stream().filter(hour -> !newHourIds.contains(hour.getId())).forEach(businessOpeningHoursRepository::delete);

        // Process each DTO
        for (BusinessOpeningHoursDTO dto : newHours) {
            if (dto.getId() != null) {
                // Update existing entry
                BusinessOpeningHours existing = existingHours
                    .stream()
                    .filter(h -> h.getId().equals(dto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid working hour ID"));

                existing.setDayOfWeek(dto.getDayOfWeek());
                existing.setStartTime(dto.getStartTime());
                existing.setEndTime(dto.getEndTime());
                businessOpeningHoursRepository.save(existing);
            } else {
                // Create new entry
                BusinessOpeningHours newHour = new BusinessOpeningHours();
                newHour.setBusiness(
                    businessRepository
                        .findById(businessId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid working hour ID"))
                );
                newHour.setDayOfWeek(dto.getDayOfWeek());
                newHour.setStartTime(dto.getStartTime());
                newHour.setEndTime(dto.getEndTime());
                businessOpeningHoursRepository.save(newHour);
            }
        }
        return List.of();
    }
}
