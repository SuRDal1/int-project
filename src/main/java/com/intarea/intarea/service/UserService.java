package com.intarea.intarea.service;

import com.intarea.intarea.domain.Users;
import com.intarea.intarea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // 회원가입
    @Transactional
    public void save(Users users) {
        userRepository.save(users);
    }

    // 중복 회원 검증
    public Users vailduplicateMember(String loginId) {
        Users users = userRepository.findByLoginId(loginId).orElse(null);
        if(users == null){
            return null;
        }
        return users;
    }

    // 전체 회원 조회
    public List<Users> findAllUser() {
        return userRepository.findAll();
    }

    // 회원 한 명 조회
    public Optional<Users> findOne(Long loginId) {
        return userRepository.findById(loginId);
    }


    // 회원 삭제
    @Transactional
    public void delete(Long loginId) {
        userRepository.deleteById(loginId);
    }

}
