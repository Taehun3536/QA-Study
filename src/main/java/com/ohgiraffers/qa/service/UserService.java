package com.ohgiraffers.qa.service;

import com.ohgiraffers.qa.model.User;
import com.ohgiraffers.qa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 로그인 시도
     * @return 성공 시 User, 실패 시, Optional.empty()
     * */
    @Transactional
    public Optional<User> login(String loginId, String password) {
        Optional<User> userOptional = userRepository.findByLoginId(loginId);

        if (userOptional.isEmpty()) {
            return  Optional.empty();
        }

        User user = userOptional.get();
        if(!user.getPassword().equals(password)) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    /**
     * 회원가입
     * */
    @Transactional
    public boolean join(User user) {
        boolean exists = userRepository.findByLoginId(user.getLoginId()).isPresent();
        if (exists) {
            return false;
        }
        userRepository.save(user);
        return true;
    }
}

