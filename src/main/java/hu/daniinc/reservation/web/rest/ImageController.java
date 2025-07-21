package hu.daniinc.reservation.web.rest;

import hu.daniinc.reservation.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/api/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // MIME típus ellenőrzése
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/") || contentType.startsWith("image/svg")) {
            return new ResponseEntity<>("Csak kép típusú fájlok engedélyezettek.", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        try {
            String path = imageService.uploadImage(file);

            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            String imageUrl = baseUrl + "/" + path;
            return new ResponseEntity<>(imageUrl, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete")
    @CrossOrigin
    public ResponseEntity<String> deleteImage(@RequestParam("filePath") String filePath) {
        try {
            boolean deleted = imageService.deleteImage(filePath);
            if (deleted) {
                return new ResponseEntity<>("A fájl sikeresen törölve lett.", HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>("A fájl nem található.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Hiba történt a fájl törlése során.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
