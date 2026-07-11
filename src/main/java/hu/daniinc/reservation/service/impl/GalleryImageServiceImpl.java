package hu.daniinc.reservation.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import hu.daniinc.reservation.domain.BusinessEmployee;
import hu.daniinc.reservation.domain.GalleryImage;
import hu.daniinc.reservation.repository.BusinessEmployeeRepository;
import hu.daniinc.reservation.repository.GalleryImageRepository;
import hu.daniinc.reservation.service.GalleryImageService;
import hu.daniinc.reservation.service.dto.GalleryImageDTO;
import hu.daniinc.reservation.service.mapper.GalleryImageMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GalleryImageServiceImpl implements GalleryImageService {

    private final Cloudinary cloudinary;
    private final GalleryImageRepository galleryImageRepository;
    private final BusinessEmployeeRepository businessEmployeeRepository;
    private final GalleryImageMapper galleryImageMapper;

    public GalleryImageServiceImpl(
        Cloudinary cloudinary,
        GalleryImageRepository galleryImageRepository,
        BusinessEmployeeRepository businessEmployeeRepository,
        GalleryImageMapper galleryImageMapper
    ) {
        this.cloudinary = cloudinary;
        this.galleryImageRepository = galleryImageRepository;
        this.businessEmployeeRepository = businessEmployeeRepository;
        this.galleryImageMapper = galleryImageMapper;
    }

    @Override
    @Transactional
    public List<GalleryImageDTO> uploadMultiple(Long businessEmployeeId, MultipartFile[] files) throws IOException {
        BusinessEmployee businessEmployee = businessEmployeeRepository
            .findById(businessEmployeeId)
            .orElseThrow(() -> new EntityNotFoundException("BusinessEmployee not found with id: " + businessEmployeeId));

        List<GalleryImage> savedImages = new ArrayList<>();

        Map<String, Object> options = ObjectUtils.asMap(
            "folder",
            "business-employees/" + businessEmployeeId + "/gallery",
            "resource_type",
            "image",
            "transformation",
            "f_auto,q_auto"
        );

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            validateImageFile(file);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            GalleryImage image = new GalleryImage();
            image.setUrl((String) uploadResult.get("secure_url"));
            image.setPublicId((String) uploadResult.get("public_id"));
            image.setBusinessEmployee(businessEmployee);

            savedImages.add(galleryImageRepository.save(image));
        }

        return galleryImageMapper.toDto(savedImages);
    }

    @Override
    @Transactional
    public Page<GalleryImageDTO> getByBusinessEmployee(Long businessEmployeeId, Pageable pageable) {
        if (!businessEmployeeRepository.existsById(businessEmployeeId)) {
            throw new EntityNotFoundException("BusinessEmployee not found with id: " + businessEmployeeId);
        }
        return galleryImageRepository.findAllByBusinessEmployeeId(businessEmployeeId, pageable).map(galleryImageMapper::toDto);
    }

    @Override
    public void delete(Long imageId) throws IOException {
        GalleryImage image = galleryImageRepository
            .findById(imageId)
            .orElseThrow(() -> new EntityNotFoundException("GalleryImage not found with id: " + imageId));

        cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());
        galleryImageRepository.delete(image);
    }

    @Override
    public boolean isEmployeeHasImageById(Long employeeId, Long imageId) {
        return galleryImageRepository.isEmployeeHasImageById(employeeId, imageId);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }
}
