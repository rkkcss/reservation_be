package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.domain.Business;
import hu.daniinc.reservation.domain.Guest;
import hu.daniinc.reservation.repository.BusinessRepository;
import hu.daniinc.reservation.repository.GuestRepository;
import hu.daniinc.reservation.service.GuestService;
import hu.daniinc.reservation.service.dto.GuestDTO;
import hu.daniinc.reservation.service.mapper.GuestMapper;
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
 * Service Implementation for managing {@link hu.daniinc.reservation.domain.Guest}.
 */
@Service
@Transactional
public class GuestServiceImpl implements GuestService {

    private static final Logger LOG = LoggerFactory.getLogger(GuestServiceImpl.class);

    private final GuestRepository guestRepository;

    private final GuestMapper guestMapper;
    private final BusinessRepository businessRepository;
    private final BusinessServiceImpl businessServiceImpl;
    private final BusinessEmployeeServiceImpl businessEmployeeServiceImpl;

    public GuestServiceImpl(
        GuestRepository guestRepository,
        GuestMapper guestMapper,
        BusinessRepository businessRepository,
        BusinessServiceImpl businessServiceImpl,
        BusinessEmployeeServiceImpl businessEmployeeServiceImpl
    ) {
        this.guestRepository = guestRepository;
        this.guestMapper = guestMapper;
        this.businessRepository = businessRepository;
        this.businessServiceImpl = businessServiceImpl;
        this.businessEmployeeServiceImpl = businessEmployeeServiceImpl;
    }

    @Override
    public GuestDTO save(GuestDTO guestDTO) {
        LOG.debug("Request to save Guest : {}", guestDTO);
        Guest guest = guestMapper.toEntity(guestDTO);
        guest = guestRepository.save(guest);
        return guestMapper.toDto(guest);
    }

    @Override
    public GuestDTO update(GuestDTO guestDTO) {
        LOG.debug("Request to update Guest : {}", guestDTO);
        Guest guest = guestMapper.toEntity(guestDTO);
        guest = guestRepository.save(guest);
        return guestMapper.toDto(guest);
    }

    @Override
    public Optional<GuestDTO> partialUpdate(GuestDTO guestDTO) {
        LOG.debug("Request to partially update Guest : {}", guestDTO);

        return guestRepository
            .findById(guestDTO.getId())
            .map(existingGuest -> {
                guestMapper.partialUpdate(existingGuest, guestDTO);

                return existingGuest;
            })
            .map(guestRepository::save)
            .map(guestMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GuestDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Guests");
        return guestRepository.findAllByLoggedInUser(pageable).map(guestMapper::toDto);
    }

    /**
     *  Get all the guests where Appointment is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<GuestDTO> findAllWhereAppointmentIsNull() {
        LOG.debug("Request to get all guests where Appointment is null");
        return StreamSupport.stream(guestRepository.findAll().spliterator(), false)
            .filter(guest -> guest.getAppointments() == null)
            .map(guestMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GuestDTO> findOne(Long id) {
        LOG.debug("Request to get Guest : {}", id);
        return guestRepository.findById(id).map(guestMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Guest : {}", id);
        guestRepository.deleteById(id);
    }

    @Override
    public List<GuestDTO> findAllBySearchString(String searchString) {
        return guestRepository.searchByName(searchString).stream().map(guestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Page<GuestDTO> findAllByLoggedInUser(Pageable pageable) {
        LOG.debug("Request to get all Guests by logged in user");
        return guestRepository.findAllByLoggedInUser(pageable).map(guestMapper::toDto);
    }
}
