package Backend.socialprompt.controllers;

import Backend.socialprompt.model.User;
import Backend.socialprompt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    // -------------------
    // SIGNUP
    // -------------------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        userService.createUserAndSendVerification(email, password, backendBaseUrl);
        return ResponseEntity.ok(Map.of("message", "Signup successful! Verification mail sent."));
    }

    // -------------------
    // VERIFY EMAIL
    // -------------------
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("token") String token) {
        String jwt = userService.verifyAccountAndLogin(token);
        return ResponseEntity.ok(Map.of("token", jwt, "message", "Email verified successfully!"));
    }

    // -------------------
    // LOGIN
    // -------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        try {
            String jwt = userService.login(email, password);
            return ResponseEntity.ok(Map.of("token", jwt, "message", "Login successful"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(403).body(Map.of("message", ex.getMessage()));
        }
    }

    // -------------------
    // FORGOT PASSWORD
    // -------------------
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        userService.sendPasswordReset(email, backendBaseUrl);
        return ResponseEntity.ok(Map.of("message", "Reset link sent if email exists"));
    }

    // -------------------
    // RESET PASSWORD
    // -------------------
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    // -------------------
    // CHANGE PASSWORD
    // -------------------
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(userId, currentPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
