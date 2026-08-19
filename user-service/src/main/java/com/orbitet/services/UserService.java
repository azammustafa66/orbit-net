package com.orbitet.services;

import com.orbitet.dto.*;
import com.orbitet.entities.AppUser;
import com.orbitet.exceptions.BadCredentialsException;
import com.orbitet.exceptions.BadRequestException;
import com.orbitet.repos.UserRepository;
import com.orbitet.utils.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    /**
     * Verified against when no account matches, so that a failed login costs the same
     * whether or not the email exists — otherwise the response time leaks which is which.
     */
    private static final String DUMMY_HASH = BCrypt.hash("dummy-password-for-constant-time-login");

    private static final String INVALID_CREDENTIALS = "Email or password is incorrect";

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signUpReq) {
        log.info("Signing up user with email {}", signUpReq.getEmail());

        if (userRepository.existsByEmail(signUpReq.getEmail())) {
            throw new BadRequestException("User with this email already exists");
        }

        AppUser user = modelMapper.map(signUpReq, AppUser.class);
        // hash only after the cheap existence check, and never store the raw password
        user.setPassword(BCrypt.hash(signUpReq.getPassword()));

        AppUser savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            // two concurrent signups can both clear the check above; the unique
            // constraint is what actually guarantees it, so report it the same way
            log.warn("Concurrent signup lost the race for email {}", signUpReq.getEmail());
            throw new BadRequestException("User with this email already exists");
        }

        SignUpResponseDto response = new SignUpResponseDto();
        response.setId(savedUser.getId());
        response.setName(savedUser.getFullName());
        response.setEmail(savedUser.getEmail());
        response.setAccountCreated(true);
        return response;
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto loginReq) {
        log.info("Login attempt for email {}", loginReq.getEmail());

        Optional<AppUser> user = userRepository.findByEmail(loginReq.getEmail());

        // an unknown email and a wrong password must be indistinguishable to the caller,
        // in both the response and the time taken to produce it
        String storedHash = user.map(AppUser::getPassword).orElse(DUMMY_HASH);
        boolean doesPasswordMatch = BCrypt.verify(loginReq.getPassword(), storedHash);

        if (user.isEmpty() || !doesPasswordMatch) {
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        return new LoginResponseDto(jwtService.issueToken(user.get()));
    }
}
