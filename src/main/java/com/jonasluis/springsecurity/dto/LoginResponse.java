package com.jonasluis.springsecurity.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
    
}
