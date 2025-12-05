package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Appointment;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.Offering;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.domain.enumeration.BasicEntityStatus;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.repository.OfferingRepository;
import hu.daniinc.reservation.service.OfferingService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import hu.daniinc.reservation.service.mapper.OfferingMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.Offering}.
 */
@Service
@Transactional
public class OfferingServiceImpl implements OfferingService {

    private static final Logger LOG = LoggerFactory.getLogger(OfferingServiceImpl.class);

    private final OfferingRepository offeringRepository;

    private final OfferingMapper offeringMapper;
    private final BusinessRepository businessRepository;
    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final UserService userService;

    public OfferingServiceImpl(
        OfferingRepository offeringRepository,
        OfferingMapper offeringMapper,
        BusinessRepository businessRepository,
        BusinessEmployeeRepository businessEmployeeRepository,
        UserService userService
    ) {
        this.offeringRepository = offeringRepository;
        this.offeringMapper = offeringMapper;
        this.businessRepository = businessRepository;
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public OfferingDTO saveOwnOffering(OfferingDTO offeringDTO, Long businessId) {
        LOG.debug("Request to save Offering : {}", offeringDTO);

        BusinessEmployee currentEmployee = businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() -> new EntityNotFoundException("You are not part of this business"));

        Offering offering = offeringMapper.toEntity(offeringDTO);
        offering.setBusinessEmployee(currentEmployee);
        offering = offeringRepository.save(offering);

        return offeringMapper.toDto(offering);
    }

    @Override
    public OfferingDTO update(OfferingDTO offeringDTO) {
        LOG.debug("Request to update Offering : {}", offeringDTO);
        Offering offering = offeringMapper.toEntity(offeringDTO);
        offering = offeringRepository.save(offering);
        return offeringMapper.toDto(offering);
    }

    @Override
    public Optional<OfferingDTO> partialUpdate(OfferingDTO offeringDTO) {
        LOG.debug("Request to partially update Offering : {}", offeringDTO);
        User user = userService.getUserWithAuthorities().orElseThrow(() -> new AuthorizationDeniedException("You are not authorised"));

        return offeringRepository
            .findById(offeringDTO.getId())
            .map(existingOffering -> {
                offeringMapper.partialUpdate(existingOffering, offeringDTO);

                return existingOffering;
            })
            .map(offeringRepository::save)
            .map(offeringMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfferingDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Offerings");
        return offeringRepository.findAll(pageable).map(offeringMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OfferingDTO> findOne(Long id) {
        LOG.debug("Request to get Offering : {}", id);
        return offeringRepository.findById(id).map(offeringMapper::toDto);
    }

    @Override
    public void logicalDelete(Long id) {
        LOG.debug("Request to delete Offering : {}", id);
        Offering offering = offeringRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Offering not found for id : " + id));
        offering.setStatus(BasicEntityStatus.DELETED);
        offeringRepository.save(offering);
    }

    @Override
    public Page<OfferingDTO> getAllByLoggedInEmployeeAndBusinessId(Long businessId, Pageable pageable) {
        return offeringRepository.getAllByBusinessIdAndLoggedInEmployee(businessId, pageable).map(offeringMapper::toDto);
    }

    @Override
    public Page<OfferingDTO> getAllByBusinessId(Long id, Pageable pageable) {
        return offeringRepository.findAllByBusinessId(id, pageable).map(offeringMapper::toDto);
    }

    @Override
    public List<OfferingDTO> getAllByLoggedInOwnerWithoutPagination() {
        return offeringRepository.getAllByBusinessOwnerWithoutPagination().stream().map(offeringMapper::toDto).collect(Collectors.toList());
    }
}
