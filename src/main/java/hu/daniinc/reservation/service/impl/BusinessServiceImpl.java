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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    public BusinessServiceImpl(BusinessRepository businessRepository, BusinessMapper businessMapper) {
        this.businessRepository = businessRepository;
        this.businessMapper = businessMapper;
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
    public BusinessDTO getBusinessByLoggedInUser(Long businessId) {
        LOG.debug("Request to get Business By LoggedInUser");
        return businessRepository
            .findBusinessByLoginAndBusinessId(businessId)
            .map(businessMapper::toDto)
            .orElseThrow(() -> new RuntimeException("No Business Found"));
    }

    @Override
    @CacheEvict(value = { "businessBySlug", "businessByCustomDomain" }, allEntries = true)
    public void changeBusinessLogo(String newLogo) {
        LOG.debug("Request to change Business Logo");

        Business business = businessRepository
            .findBusinessByLoginAndBusinessId(1L)
            .orElseThrow(() -> new EntityNotFoundException("No Business Found"));
        businessRepository.save(business);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "businessBySlug", "businessByCustomDomain" }, allEntries = true)
    public void changeBusinessThemeById(Long businessId, BusinessTheme theme) {
        LOG.debug("Request to change Business Theme");

        Business business = businessRepository
            .findBusinessByLoginAndBusinessId(businessId)
            .orElseThrow(() -> new EntityNotFoundException("No Business Found"));

        business.setTheme(theme);
        businessRepository.save(business);
    }

    @Override
    @Cacheable(value = "businessBySlug", key = "#slug.toLowerCase()", unless = "#result == null")
    public BusinessDTO findBySlug(String slug) {
        Business result = businessRepository.findBySlugIgnoreCase(slug).orElseThrow(() -> new EntityNotFoundException("No Business Found"));
        return businessMapper.toDto(result);
    }

    @Override
    public BusinessDTO findByCustomDomain(String domain) {
        Business result = businessRepository
            .findByCustomDomainIgnoreCase(domain)
            .orElseThrow(() -> new EntityNotFoundException("No Business Found"));
        return businessMapper.toDto(result);
    }
}
