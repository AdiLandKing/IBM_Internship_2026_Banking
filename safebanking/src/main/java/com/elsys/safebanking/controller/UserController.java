package com.elsys.safebanking.controller;

import com.elsys.safebanking.dto.ChangeEPinRequest;
import com.elsys.safebanking.dto.EPinStatusResponse;
import com.elsys.safebanking.dto.SetEPinRequest;
import com.elsys.safebanking.dto.UpdateProfileRequest;
import com.elsys.safebanking.dto.UserProfileResponse;
import com.elsys.safebanking.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public UserProfileResponse profile(Principal principal) {
        return UserProfileResponse.from(userService.getByEmail(principal.getName()));
    }

    @PutMapping("/profile")
    public UserProfileResponse updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(principal.getName(), request);
    }

    @GetMapping("/e-pin/status")
    public EPinStatusResponse ePinStatus(Principal principal) {
        return userService.getEPinStatus(principal.getName());
    }

    @PostMapping("/e-pin")
    public EPinStatusResponse setEPin(
            Principal principal,
            @Valid @RequestBody SetEPinRequest request
    ) {
        return userService.setEPin(principal.getName(), request);
    }

    @PutMapping("/e-pin")
    public EPinStatusResponse changeEPin(
            Principal principal,
            @Valid @RequestBody ChangeEPinRequest request
    ) {
        return userService.changeEPin(principal.getName(), request);
    }
}
