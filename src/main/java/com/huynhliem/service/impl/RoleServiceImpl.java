package com.huynhliem.service.impl;

import com.huynhliem.model.Role;
import com.huynhliem.repository.RoleRepository;
import com.huynhliem.service.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @PostConstruct
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}
