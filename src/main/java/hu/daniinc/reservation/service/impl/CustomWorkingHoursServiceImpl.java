package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.CustomWorkingHours;
import hu.daniinc.reservation.repository.CustomWorkingHoursRepository;
import hu.daniinc.reservation.service.CustomWorkingHoursService;
import hu.daniinc.reservation.service.dto.CustomWorkingHoursDTO;
import hu.daniinc.reservation.service.mapper.CustomWorkingHoursMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.CustomWorkingHours}.
 */
@Service
@Transactional
public class CustomWorkingHoursServiceImpl implements CustomWorkingHoursService {

    private static final Logger LOG = LoggerFactory.getLogger(CustomWorkingHoursServiceImpl.class);

    private final CustomWorkingHoursRepository customWorkingHoursRepository;

    private final CustomWorkingHoursMapper customWorkingHoursMapper;

    public CustomWorkingHoursServiceImpl(
        CustomWorkingHoursRepository customWorkingHoursRepository,
        CustomWorkingHoursMapper customWorkingHoursMapper
    ) {
        this.customWorkingHoursRepository = customWorkingHoursRepository;
        this.customWorkingHoursMapper = customWorkingHoursMapper;
    }

    @Override
    public CustomWorkingHoursDTO save(CustomWorkingHoursDTO customWorkingHoursDTO) {
        LOG.debug("Request to save CustomWorkingHours : {}", customWorkingHoursDTO);
        CustomWorkingHours customWorkingHours = customWorkingHoursMapper.toEntity(customWorkingHoursDTO);
        customWorkingHours = customWorkingHoursRepository.save(customWorkingHours);
        return customWorkingHoursMapper.toDto(customWorkingHours);
    }

    @Override
    public CustomWorkingHoursDTO update(CustomWorkingHoursDTO customWorkingHoursDTO) {
        LOG.debug("Request to update CustomWorkingHours : {}", customWorkingHoursDTO);
        CustomWorkingHours customWorkingHours = customWorkingHoursMapper.toEntity(customWorkingHoursDTO);
        customWorkingHours = customWorkingHoursRepository.save(customWorkingHours);
        return customWorkingHoursMapper.toDto(customWorkingHours);
    }

    @Override
    public Optional<CustomWorkingHoursDTO> partialUpdate(CustomWorkingHoursDTO customWorkingHoursDTO) {
        LOG.debug("Request to partially update CustomWorkingHours : {}", customWorkingHoursDTO);

        return customWorkingHoursRepository
            .findById(customWorkingHoursDTO.getId())
            .map(existingCustomWorkingHours -> {
                customWorkingHoursMapper.partialUpdate(existingCustomWorkingHours, customWorkingHoursDTO);

                return existingCustomWorkingHours;
            })
            .map(customWorkingHoursRepository::save)
            .map(customWorkingHoursMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomWorkingHoursDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all CustomWorkingHours");
        return customWorkingHoursRepository.findAll(pageable).map(customWorkingHoursMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomWorkingHoursDTO> findOne(Long id) {
        LOG.debug("Request to get CustomWorkingHours : {}", id);
        return customWorkingHoursRepository.findById(id).map(customWorkingHoursMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete CustomWorkingHours : {}", id);
        customWorkingHoursRepository.deleteById(id);
    }
}
