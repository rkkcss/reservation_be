package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.BusinessRating;
import hu.daniinc.reservation.repository.BusinessRatingRepository;
import hu.daniinc.reservation.service.BusinessRatingService;
import hu.daniinc.reservation.service.dto.BusinessRatingDTO;
import hu.daniinc.reservation.service.mapper.BusinessRatingMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.BusinessRating}.
 */
@Service
@Transactional
public class BusinessRatingServiceImpl implements BusinessRatingService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessRatingServiceImpl.class);

    private final BusinessRatingRepository businessRatingRepository;

    private final BusinessRatingMapper businessRatingMapper;

    public BusinessRatingServiceImpl(BusinessRatingRepository businessRatingRepository, BusinessRatingMapper businessRatingMapper) {
        this.businessRatingRepository = businessRatingRepository;
        this.businessRatingMapper = businessRatingMapper;
    }

    @Override
    public BusinessRatingDTO save(BusinessRatingDTO businessRatingDTO) {
        LOG.debug("Request to save BusinessRating : {}", businessRatingDTO);
        BusinessRating businessRating = businessRatingMapper.toEntity(businessRatingDTO);
        businessRating = businessRatingRepository.save(businessRating);
        return businessRatingMapper.toDto(businessRating);
    }

    @Override
    public BusinessRatingDTO update(BusinessRatingDTO businessRatingDTO) {
        LOG.debug("Request to update BusinessRating : {}", businessRatingDTO);
        BusinessRating businessRating = businessRatingMapper.toEntity(businessRatingDTO);
        businessRating = businessRatingRepository.save(businessRating);
        return businessRatingMapper.toDto(businessRating);
    }

    @Override
    public Optional<BusinessRatingDTO> partialUpdate(BusinessRatingDTO businessRatingDTO) {
        LOG.debug("Request to partially update BusinessRating : {}", businessRatingDTO);

        return businessRatingRepository
            .findById(businessRatingDTO.getId())
            .map(existingBusinessRating -> {
                businessRatingMapper.partialUpdate(existingBusinessRating, businessRatingDTO);

                return existingBusinessRating;
            })
            .map(businessRatingRepository::save)
            .map(businessRatingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BusinessRatingDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all BusinessRatings");
        return businessRatingRepository.findAll(pageable).map(businessRatingMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BusinessRatingDTO> findOne(Long id) {
        LOG.debug("Request to get BusinessRating : {}", id);
        return businessRatingRepository.findById(id).map(businessRatingMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete BusinessRating : {}", id);
        businessRatingRepository.deleteById(id);
    }
}
