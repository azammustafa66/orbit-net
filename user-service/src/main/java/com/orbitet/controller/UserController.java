package com.orbitet.controller;

import com.orbitet.dto.LoginRequestDto;
import com.orbitet.dto.LoginResponseDto;
import com.orbitet.dto.SignUpRequestDto;
import com.orbitet.dto.SignUpResponseDto;
import com.orbitet.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpReq) {
        SignUpResponseDto signUpResponseDto = userService.signUp(signUpReq);
        return new ResponseEntity<>(signUpResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginReq) {
        LoginResponseDto loginResponseDto = userService.login(loginReq);
        return ResponseEntity.ok(loginResponseDto);
    }
}
