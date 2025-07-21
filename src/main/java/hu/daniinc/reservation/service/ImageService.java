package hu.daniinc.reservation.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String uploadImage(MultipartFile file);

    boolean deleteImage(String filePath);
}
