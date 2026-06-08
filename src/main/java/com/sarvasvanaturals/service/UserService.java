package com.sarvasvanaturals.service;

import com.sarvasvanaturals.model.User;
import com.sarvasvanaturals.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public User register(String firstName, String lastName, String email, String password, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }
        String token = UUID.randomUUID().toString();
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .emailVerificationToken(token)
                .build();
        User saved = userRepository.save(user);
        try { emailService.sendVerificationEmail(saved); } catch (Exception ignored) {}
        return saved;
    }

    public boolean verifyEmail(String token) {
        return userRepository.findByEmailVerificationToken(token).map(user -> {
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            try { emailService.sendPasswordResetEmail(user, token); } catch (Exception ignored) {}
        });
    }

    public boolean resetPassword(String token, String newPassword) {
        return userRepository.findByPasswordResetToken(token).map(user -> {
            if (user.getPasswordResetExpiry() == null ||
                user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) return false;
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetToken(null);
            user.setPasswordResetExpiry(null);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateProfile(User user, String firstName, String lastName, String phone) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    public boolean changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
}
