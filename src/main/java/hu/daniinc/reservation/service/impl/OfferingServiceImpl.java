package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.*;
import hu.daniinc.reservation.domain.enumeration.BasicEntityStatus;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.repository.OfferingRepository;
import hu.daniinc.reservation.service.OfferingService;
import hu.daniinc.reservation.service.UserService;
import hu.daniinc.reservation.service.dto.OfferingDTO;
import hu.daniinc.reservation.service.mapper.OfferingMapper;
import hu.daniinc.reservation.service.specifications.OfferingSpecification;
import hu.daniinc.reservation.web.rest.errors.GeneralException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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
    public OfferingDTO createOffering(OfferingDTO offeringDTO, Long businessId, Long employeeId) {
        LOG.debug("Request to save Offering : {}", offeringDTO);

        BusinessEmployee employee = businessEmployeeRepository
            .findByBusinessIdAndEmployeeId(businessId, employeeId)
            .orElseThrow(() -> new EntityNotFoundException("You are not part of this business"));

        Offering offering = offeringMapper.toEntity(offeringDTO);
        offering.setBusinessEmployee(employee);
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
    public Optional<OfferingDTO> partialUpdate(OfferingDTO offeringDTO, Long businessId) {
        LOG.debug("Request to partially update Offering : {}", offeringDTO);

        // 2. Ellenőrzés: a user valóban tagja-e a megadott businessnek
        businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("You are not part of this business", "business-access-denied", HttpStatus.FORBIDDEN));

        // 3. Offering betöltése + ellenőrzés, hogy ugyanahhoz a businesshez tartozik
        Offering offering = offeringRepository
            .findById(offeringDTO.getId())
            .orElseThrow(() -> new GeneralException("Offering not found", "offering-not-found", HttpStatus.NOT_FOUND));

        if (!offering.getBusinessEmployee().getBusiness().getId().equals(businessId)) {
            throw new AuthorizationDeniedException("You cannot modify an offering in another business");
        }

        // 4. Részleges frissítés
        offeringMapper.partialUpdate(offering, offeringDTO);

        // 5. Mentés + DTO vissza
        Offering saved = offeringRepository.save(offering);
        return Optional.of(offeringMapper.toDto(saved));
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
    public void logicalDelete(Long offerId, Long businessId) {
        LOG.debug("Request to logical delete Offering : {}, in businessId: {}", offerId, businessId);
        Offering offering = offeringRepository
            .findByOfferingIdAndBusinessId(offerId, businessId)
            .orElseThrow(() -> new EntityNotFoundException("Offering not found for id : " + offerId));
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
    public List<OfferingDTO> getAllOfferingsByLoggedInEmployee(Long businessId) {
        userService.getUserWithAuthorities().orElseThrow(() -> new AuthorizationDeniedException("You are not authorised"));
        // Check if user is part of the business
        businessEmployeeRepository
            .findByUserLoginAndBusinessId(businessId)
            .orElseThrow(() -> new GeneralException("You are not part of this business", "not-part-business", HttpStatus.NOT_FOUND));

        return offeringRepository.getAllByLoggedInEmployee(businessId).stream().map(offeringMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<OfferingDTO> getAllOfferingsByLoggedInBusinessId(Long businessId, Pageable pageable) {
        return offeringRepository.findAllByBusinessId(businessId, pageable).map(offeringMapper::toDto);
    }

    @Override
    public List<OfferingDTO> getAllByBusinessEmployee(Long businessEmployeeId) {
        return offeringRepository
            .getAllByBusinessEmployee(businessEmployeeId)
            .stream()
            .map(offeringMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    public Page<OfferingDTO> findAllPublicOfferingByBusinessId(Long businessId, String search, Pageable pageable) {
        Specification<Offering> spec = OfferingSpecification.publicOfferingsWithEmployeeNameFilter(businessId, search);

        return offeringRepository.findAll(spec, pageable).map(offeringMapper::toDto);
    }
}
