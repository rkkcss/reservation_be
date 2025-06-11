package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.BusinessOpeningHours;
import hu.daniinc.reservation.repository.BusinessOpeningHoursRepository;
import hu.daniinc.reservation.service.BusinessOpeningHoursService;
import hu.daniinc.reservation.service.dto.BusinessOpeningHoursDTO;
import hu.daniinc.reservation.service.mapper.BusinessOpeningHoursMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.BusinessOpeningHours}.
 */
@Service
@Transactional
public class BusinessOpeningHoursServiceImpl implements BusinessOpeningHoursService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessOpeningHoursServiceImpl.class);

    private final BusinessOpeningHoursRepository businessOpeningHoursRepository;

    private final BusinessOpeningHoursMapper businessOpeningHoursMapper;

    public BusinessOpeningHoursServiceImpl(
        BusinessOpeningHoursRepository businessOpeningHoursRepository,
        BusinessOpeningHoursMapper businessOpeningHoursMapper
    ) {
        this.businessOpeningHoursRepository = businessOpeningHoursRepository;
        this.businessOpeningHoursMapper = businessOpeningHoursMapper;
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
}
