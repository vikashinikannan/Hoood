package Backend.socialprompt.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redirect")
@RequiredArgsConstructor
public class RedirectController {
    @Value("${app.frontend.verify-url}")
    private String frontendVerifyUrl;

    @Value("${app.frontend.reset-url}")
    private String frontendResetUrl;

    // Redirect for email verification
    @GetMapping("/verify")
    public ResponseEntity<?> redirectVerify(@RequestParam("token") String token) {
        String deepLink = frontendVerifyUrl + token;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", deepLink);
        return ResponseEntity.status(302).headers(headers).build();
    }

    // Redirect for password reset
    @GetMapping("/reset-password")
    public ResponseEntity<?> redirectReset(@RequestParam("token") String token) {
        String deepLink = frontendResetUrl + token;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", deepLink);
        return ResponseEntity.status(302).headers(headers).build();
    }

}
