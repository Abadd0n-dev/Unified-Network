package com.unifiedNetwork.UnifiedNetwork.repository;

import com.unifiedNetwork.UnifiedNetwork.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface IUserService extends UserDetailsService {
    User registerUser(User user);
    User authenticateUser(String username, String password);
    User getUserById(Long id);
    User updateUser(User updatedUser);
}

