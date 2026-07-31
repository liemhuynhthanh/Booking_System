package com.huynhliem.service.impl;

import com.huynhliem.model.User;
import com.huynhliem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Optional<User> user=userRepository.findUserByName(username);
        if (user.isPresent()) {
            var userObj=user.get();
            return org.springframework.security.core.userdetails.User.builder().username(userObj.getName())
                    .password(userObj.getPassword())
                    .roles(userObj.getRole().getRoleName())
                    .build();
        }else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

    }
}
