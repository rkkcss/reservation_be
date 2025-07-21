package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.enumeration.BusinessTheme;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.service.BusinessService;
import hu.daniinc.reservation.service.dto.BusinessDTO;
import hu.daniinc.reservation.service.mapper.BusinessMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.Business}.
 */
@Service
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessServiceImpl.class);

    private final BusinessRepository businessRepository;

    private final BusinessMapper businessMapper;
    private final ImageServiceImpl imageServiceImpl;

    public BusinessServiceImpl(BusinessRepository businessRepository, BusinessMapper businessMapper, ImageServiceImpl imageServiceImpl) {
        this.businessRepository = businessRepository;
        this.businessMapper = businessMapper;
        this.imageServiceImpl = imageServiceImpl;
    }

    @Override
    public BusinessDTO save(BusinessDTO businessDTO) {
        LOG.debug("Request to save Business : {}", businessDTO);
        Business business = businessMapper.toEntity(businessDTO);
        business = businessRepository.save(business);
        return businessMapper.toDto(business);
    }

    @Override
    public BusinessDTO update(BusinessDTO businessDTO) {
        LOG.debug("Request to update Business : {}", businessDTO);
        Business business = businessMapper.toEntity(businessDTO);
        business = businessRepository.save(business);
        return businessMapper.toDto(business);
    }

    @Override
    public Optional<BusinessDTO> partialUpdate(BusinessDTO businessDTO) {
        LOG.debug("Request to partially update Business : {}", businessDTO);

        return businessRepository
            .findById(businessDTO.getId())
            .map(existingBusiness -> {
                businessMapper.partialUpdate(existingBusiness, businessDTO);

                return existingBusiness;
            })
            .map(businessRepository::save)
            .map(businessMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BusinessDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Businesses");
        return businessRepository.findAll(pageable).map(businessMapper::toDto);
    }

    /**
     *  Get all the businesses where Appointment is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<BusinessDTO> findAllWhereAppointmentIsNull() {
        LOG.debug("Request to get all businesses where Appointment is null");
        return StreamSupport.stream(businessRepository.findAll().spliterator(), false)
            .filter(business -> business.getAppointments() == null)
            .map(businessMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BusinessDTO> findOne(Long id) {
        LOG.debug("Request to get Business : {}", id);
        return businessRepository.findById(id).map(businessMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Business : {}", id);
        businessRepository.deleteById(id);
    }

    @Override
    public BusinessDTO getBusinessByLoggedInUser() {
        LOG.debug("Request to get Business By LoggedInUser");
        return businessRepository.findByLogin().map(businessMapper::toDto).orElseThrow(() -> new RuntimeException("No Business Found"));
    }

    @Override
    public void changeBusinessLogo(String newLogo) {
        LOG.debug("Request to change Business Logo");

        Business business = businessRepository.findByLogin().orElseThrow(() -> new EntityNotFoundException("No Business Found"));

        String oldLogo = business.getLogo();

        if (newLogo == null || newLogo.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid logo name");
        }

        business.setLogo(newLogo);
        businessRepository.save(business);

        if (oldLogo != null && !oldLogo.equals(newLogo)) {
            imageServiceImpl.deleteImage(oldLogo);
        }
    }

    @Override
    @Transactional
    public void changeBusinessTheme(BusinessTheme theme) {
        LOG.debug("Request to change Business Theme");

        Business business = businessRepository.findByLogin().orElseThrow(() -> new EntityNotFoundException("No Business Found"));

        business.setTheme(theme);
        businessRepository.save(business);
    }

    public Business getAuthenticatedBusiness() {
        return businessRepository
            .findByLogin()
            .orElseThrow(() -> new IllegalStateException("Authenticated user has no associated business."));
    }
}
