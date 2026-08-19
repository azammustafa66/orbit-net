package com.azam.orbitnet.user_service.dto;

import com.azam.orbitnet.user_service.validation.MaxBytes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequestDto {

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email must be a valid address")
    private String email;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    // BCrypt's ceiling is 72 bytes, not characters, so the byte length is what has to be bounded
    @MaxBytes(value = 72, message = "Password must not exceed 72 bytes; accented and emoji characters count as more than one")
    private String password;

    @NotBlank(message = "Full name must not be empty")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;
}
