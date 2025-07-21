package hu.daniinc.reservation.web.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImageControllerView {

    private final String imageDirectory = "uploaded-images";

    @GetMapping("/uploaded-images/{date}/{imageName:.+}")
    public ResponseEntity<?> getPhoto(@PathVariable String date, @PathVariable String imageName) {
        try {
            Path imagePath = Paths.get(imageDirectory).resolve(date).resolve(imageName);
            Resource resource = new InputStreamResource(Files.newInputStream(imagePath));

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageName + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
        } catch (IOException e) {
            // Kezelj hibát, például ha a kép nem található
            return ResponseEntity.notFound().build();
        }
    }
}
