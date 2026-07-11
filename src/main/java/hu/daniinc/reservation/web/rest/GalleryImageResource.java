package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.domain.enumeration.BusinessPermission;
import hu.daniinc.reservation.security.annotation.RequiredBusinessPermission;
import hu.daniinc.reservation.service.GalleryImageService;
import hu.daniinc.reservation.service.dto.GalleryImageDTO;
import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/gallery-image")
public class GalleryImageResource {

    private final GalleryImageService galleryImageService;

    public GalleryImageResource(GalleryImageService galleryImageService) {
        this.galleryImageService = galleryImageService;
    }

    @GetMapping("/business-employee/{id}")
    public ResponseEntity<List<GalleryImageDTO>> getBusinessEmployee(@PathVariable Long id, Pageable pageable) {
        Page<GalleryImageDTO> result = galleryImageService.getByBusinessEmployee(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), result);

        return ResponseEntity.ok().headers(headers).body(result.getContent());
    }

    @PostMapping("/business-employee/{id}")
    @RequiredBusinessPermission(BusinessPermission.MANAGE_BUSINESS_SETTINGS)
    public ResponseEntity<List<GalleryImageDTO>> uploadGallery(@PathVariable Long id, @RequestParam("files") MultipartFile[] files)
        throws IOException {
        List<GalleryImageDTO> images = galleryImageService.uploadMultiple(id, files);

        return ResponseEntity.ok().body(images);
    }

    @DeleteMapping("/business-employee/{employeeId}/image/{imageId}")
    @RequiredBusinessPermission(BusinessPermission.MANAGE_BUSINESS_SETTINGS)
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId, @PathVariable Long employeeId) throws IOException {
        if (!galleryImageService.isEmployeeHasImageById(employeeId, imageId)) {
            return ResponseEntity.badRequest().build();
        }
        galleryImageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }
}
