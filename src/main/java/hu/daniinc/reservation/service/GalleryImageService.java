package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.GalleryImage;
import hu.daniinc.reservation.service.dto.GalleryImageDTO;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface GalleryImageService {
    List<GalleryImageDTO> uploadMultiple(Long businessEmployeeId, MultipartFile[] file) throws IOException;

    Page<GalleryImageDTO> getByBusinessEmployee(Long businessEmployeeId, Pageable pageable);

    void delete(Long imageId) throws IOException;

    boolean isEmployeeHasImageById(Long employeeId, Long imageId);
}
