package com.azam.orbitnet.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
// @Builder supplies only an all-args constructor; Jackson needs the no-args one to deserialise
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank
    @Email(message = "provide a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
