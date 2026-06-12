package com.server.app.controllers;

import com.server.app.dto.auth.LoginDto;
import com.server.app.dto.auth.SignUpDto;
import com.server.app.dto.auth.UpdatePasswordDto;
import com.server.app.dto.auth.UpdateProfileDto;
import com.server.app.dto.response.AuthResponse;
import com.server.app.entities.User;
import com.server.app.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDto dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpDto dto) {
        return ResponseEntity.ok(userService.signUp(dto));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> profile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update/profile")
    public ResponseEntity<AuthResponse> updateProfile(@AuthenticationPrincipal User user,
                                                      @Valid @RequestBody UpdateProfileDto dto) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), dto));
    }

    @PutMapping("/update/password")
    public ResponseEntity<User> updatePassword(@AuthenticationPrincipal User user,
                                               @Valid @RequestBody UpdatePasswordDto dto) {
        return ResponseEntity.ok(userService.updatePassword(user.getId(), dto));
    }
}
