package com.azam.orbitnet.user_service.dto;

import lombok.Data;

@Data
public class SignUpResponseDto {

    private Long id;
    private String name;
    private String email;
    private boolean accountCreated;
}
