package com.alquiler.car_rent.service.impl;

import com.alquiler.car_rent.commons.entities.UserEntity;
import com.alquiler.car_rent.commons.enums.Role;
import com.alquiler.car_rent.repositories.UserEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserEntityRepository userEntityRepository;

    @Transactional
    public void promoToAdmin(Long userId){
        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("Usuario No Encontrado"));
        if(user.getRole()!= Role.ADMIN){
            user.setRole(Role.ADMIN);
            userEntityRepository.save(user);
        }
    }
}
