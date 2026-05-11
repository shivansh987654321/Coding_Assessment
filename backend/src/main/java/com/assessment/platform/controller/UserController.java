package com.assessment.platform.controller;

import com.assessment.platform.dto.UserSyncRequest;
import com.assessment.platform.entity.User;
import com.assessment.platform.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sync")
    public User sync(@RequestBody UserSyncRequest request) {
        return userService.sync(request);
    }
}
