package Backend.socialprompt.service;

import Backend.socialprompt.jwt.JwtProvider;
import Backend.socialprompt.model.PasswordResetToken;
import Backend.socialprompt.model.User;
import Backend.socialprompt.model.VerificationToken;
import Backend.socialprompt.repository.PasswordResetTokenRepository;
import Backend.socialprompt.repository.UserRepository;
import Backend.socialprompt.repository.VerificationTokenRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // -------------------
    // SIGNUP + SEND VERIFICATION
    // -------------------
    public void createUserAndSendVerification(String email, String password, String backendBaseUrl) {
        if (userRepository.findByEmail(email).isPresent())
            throw new RuntimeException("Email already exists");

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .enabled(false)
                .provider("local")
                .roles(Set.of("ROLE_USER"))
                .build();
        userRepository.save(user);

        sendVerificationEmail(email, backendBaseUrl);
    }

    // -------------------
    // LOGIN
    // -------------------
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");

        if (!user.isEnabled())
            throw new RuntimeException("Please verify your email before logging in.");

        return jwtProvider.createToken(user.getId(), user.getEmail());
    }

    // -------------------
    // SEND VERIFICATION EMAIL
    // -------------------
    public void sendVerificationEmail(String email, String backendBaseUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .id(token)
                .userId(user.getId())
                .expiryDate(Instant.now().plusSeconds(300)) // 5 min validity
                .build();
        verificationTokenRepository.save(vt);

        String link = backendBaseUrl + "/redirect/verify?token=" + token;

        String body = "Please verify your account by clicking the link below (valid for 5 minutes):\n\n"
                + link + "\n\nThank you for registering!";
        emailService.sendSimpleMessage(email, "Verify your account", body);
    }

    // -------------------
    // VERIFY ACCOUNT
    // -------------------
    public String verifyAccountAndLogin(String token) {
        VerificationToken vt = verificationTokenRepository.findById(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (vt.getExpiryDate().isBefore(Instant.now())) {
            verificationTokenRepository.deleteById(vt.getId());
            throw new RuntimeException("Verification link expired");
        }

        User user = userRepository.findById(vt.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.deleteById(vt.getId());

        return jwtProvider.createToken(user.getId(), user.getEmail());
    }

    // -------------------
    // PASSWORD RESET
    // -------------------
    public void sendPasswordReset(String email, String backendBaseUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = PasswordResetToken.builder()
                .id(token)
                .userId(user.getId())
                .expiryDate(Instant.now().plusSeconds(300)) // 5 min validity
                .build();
        passwordResetTokenRepository.save(prt);

        String link = backendBaseUrl + "/redirect/reset-password?token=" + token;

        String body = "Click the link below to reset your password (valid for 5 minutes):\n\n"
                + link + "\n\nIf you didn't request this, please ignore it.";
        emailService.sendSimpleMessage(email, "Reset your password", body);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = passwordResetTokenRepository.findById(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = userRepository.findById(prt.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.deleteById(prt.getId());
    }
    // -------------------
// CHANGE PASSWORD
// -------------------
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    // -------------------
    // GOOGLE LOGIN
    // -------------------
    public String processOAuthPostLogin(String email) {
        Optional<User> opt = userRepository.findByEmail(email);
        User user = opt.orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .enabled(true)
                    .provider("google")
                    .roles(Set.of("ROLE_USER"))
                    .build();
            return userRepository.save(newUser);
        });
        return jwtProvider.createToken(user.getId(), user.getEmail());
    }
}

