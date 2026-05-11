package com.assessment.platform.service;

import com.assessment.platform.dto.UserSyncRequest;
import com.assessment.platform.entity.User;
import com.assessment.platform.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public synchronized User sync(UserSyncRequest request) {
        if (!StringUtils.hasText(request.clerkUserId())) {
            throw new IllegalArgumentException("clerkUserId is required");
        }
        try {
            return userRepository.findByClerkUserId(request.clerkUserId())
                    .map(user -> updateUser(user, request))
                    .orElseGet(() -> createUser(request));
        } catch (DataIntegrityViolationException ex) {
            return userRepository.findByClerkUserId(request.clerkUserId()).orElseThrow(() -> ex);
        }
    }

    private User createUser(UserSyncRequest request) {
        User user = new User();
        user.setClerkUserId(request.clerkUserId());
        return updateUser(user, request);
    }

    private User updateUser(User user, UserSyncRequest request) {
        user.setName(StringUtils.hasText(request.name()) ? request.name() : "Candidate");
        user.setEmail(StringUtils.hasText(request.email()) ? request.email() : request.clerkUserId() + "@local.dev");
        return userRepository.save(user);
    }
}
