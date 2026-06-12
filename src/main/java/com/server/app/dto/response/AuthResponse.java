package com.server.app.dto.response;

import com.server.app.entities.User;

public record AuthResponse(String token, User data) {
}
