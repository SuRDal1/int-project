package com.intarea.intarea.service;


import com.intarea.intarea.domain.Users;
import com.intarea.intarea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {

    private final UserRepository userRepository;

    public Users login(String loginId, String password) {
        Optional<Users> findLoginId = userRepository.findByLoginId(loginId);

        if(findLoginId.isPresent()) {
            Users findUsers = findLoginId.get();
            if(findUsers.getPassword().equals(password)) {
                return findUsers;
            }

            else return null; 
        }
        return null;
    }


}
