package hu.daniinc.reservation.web.rest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageKitController {

    @PostMapping("/api/imagekit/auth")
    public ResponseEntity<?> getAuth(@RequestBody Map<String, Object> body) {
        Map<String, Object> uploadPayload = (Map<String, Object>) body.get("uploadPayload");
        String publicKey = (String) body.get("publicKey");
        int expire = (int) body.get("expire");
        String privateKey = "private_AGzXIZnR+xp8VHWtrumseOU2BwI=";

        String token = Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setHeaderParam("kid", publicKey)
            .setClaims(uploadPayload)
            .setExpiration(new Date(System.currentTimeMillis() + (long) expire * 1000))
            .signWith(SignatureAlgorithm.HS256, privateKey.getBytes())
            .compact();

        return ResponseEntity.ok(Map.of("token", token));
    }
}
