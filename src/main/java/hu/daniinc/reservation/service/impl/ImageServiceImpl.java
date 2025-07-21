package hu.daniinc.reservation.service.impl;

import hu.daniinc.reservation.service.ImageService;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${photo.storage.location}")
    private String staticLocations; // Statikus tartalom elérési útvonala

    public String uploadImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Üres fájlt nem lehet feltölteni.");
            }

            // 📂 Alap mappa létrehozása
            Path uploadPath = Path.of(staticLocations);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 📅 Dátum alapú alkönyvtár (pl. 2025-07-16)
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Path datePath = uploadPath.resolve(currentDate);
            if (!Files.exists(datePath)) {
                Files.createDirectories(datePath);
            }

            // 🧠 Kiterjesztés megtartása
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            if (!isSupportedImageFormat(extension)) {
                throw new IllegalArgumentException("Nem támogatott képformátum: " + extension);
            }

            // 🆔 Egyedi fájlnév
            String filename = UUID.randomUUID() + "." + extension;
            Path targetPath = datePath.resolve(filename);

            // 📏 Átméretezés és mentés
            Thumbnails.of(file.getInputStream()).size(800, 600).outputQuality(0.8).toFile(targetPath.toFile());

            // ✅ Útvonal visszaadása (webbarát formában)
            return targetPath.toString().replace("\\", "/");
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    /*
    true if deleted successfully
     */
    public boolean deleteImage(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            System.err.println("Üres vagy null fileUrl.");
            return false;
        }

        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();

            if (path == null || path.trim().isEmpty()) {
                System.err.println("Üres path az URI-ból.");
                return false;
            }

            Path targetPath = Path.of(path.replaceFirst("/", ""));

            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
                return true;
            } else {
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("Nem sikerült törölni a fájlt.", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isSupportedImageFormat(String ext) {
        return List.of("jpg", "jpeg", "png").contains(ext);
    }
}
