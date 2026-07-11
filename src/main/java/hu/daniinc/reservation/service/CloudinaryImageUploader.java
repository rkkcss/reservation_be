package hu.daniinc.reservation.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CloudinaryImageUploader {

    private final Cloudinary cloudinary;

    public CloudinaryImageUploader(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public record UploadResult(String url, String publicId) {}

    public UploadResult uploadAndReplace(MultipartFile file, String folder, String oldPublicId) throws IOException {
        validateImageFile(file);

        if (oldPublicId != null && !oldPublicId.isBlank()) {
            cloudinary.uploader().destroy(oldPublicId, ObjectUtils.emptyMap());
        }

        Map<String, Object> options = ObjectUtils.asMap(
            "folder",
            folder,
            "resource_type",
            "image",
            "transformation",
            "w_400,h_400,c_fill,g_face,f_auto,q_auto"
        );
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

        return new UploadResult((String) uploadResult.get("secure_url"), (String) uploadResult.get("public_id"));
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
