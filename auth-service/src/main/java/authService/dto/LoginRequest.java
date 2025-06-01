package authService.dto;

import lombok.Data;

public @Data
class LoginRequest {
    private String username;
    private String password;
}