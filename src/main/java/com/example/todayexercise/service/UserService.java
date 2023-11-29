package com.example.todayexercise.service;

import com.example.todayexercise.dto.request.LoginDTO;
import com.example.todayexercise.dto.request.SingUpDTO;
import com.example.todayexercise.entity.User;
import com.example.todayexercise.exception.domain.User.UserErrorCode;
import com.example.todayexercise.exception.domain.User.UserException;
import com.example.todayexercise.repository.User.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Transactional
    public String signup(SingUpDTO signup) {
        User user = User.builder()
                .email(signup.getEmail())
                .password(passwordEncoder.encode(signup.getPassword()))
                .createdAt(LocalDateTime.now())
                .nickName(signup.getNickName())
                .isDeleted(false)
                .build();
        userRepository.save(user);
        return "회원가입 완료";
    }

    @Transactional(readOnly = true)
    public String login(LoginDTO loginDTO, HttpSession session) {
        User user = userRepository.findByEmail(loginDTO.getEmail());
        if(user == null || !user.isPasswordMatch(passwordEncoder, loginDTO.getPassword()))
            throw new UserException(UserErrorCode.FAIL_TO_LOGIN);
        session.setAttribute("user",user);
        return session.getId();
    }

    public String logout(HttpSession session,User user) {
        if(user == null) throw new UserException(UserErrorCode.NOT_EXIST_USER);
        log.info("로그아웃된 사용자 이메일 : {}", user.getEmail());
        session.invalidate();
        return "로그아웃 되었습니다.";
    }


    @Transactional
    public String update(User user, String updateNickName, String updatePassword) {
        Optional.ofNullable(updatePassword)
                .ifPresent(password -> user.setPassword(passwordEncoder.encode(password)));

        Optional.ofNullable(updateNickName)
                .ifPresent(user::setNickName);

        userRepository.save(user);
        return "정보 수정 완료";
    }

    @Transactional
    public String updateImage(User user, MultipartFile imageFIle ) {
        Optional.ofNullable(imageFIle)
                .filter(file -> !file.isEmpty())
                .ifPresent(file -> {
                    if (user.getProfileImage() != null) s3Service.deleteImageFile(user.getProfileImage());
                    user.setProfileImage(s3Service.uploadImageFile(imageFIle));
                });
        return user.getProfileImage();
    }


    public String checkEmail(String email) {
        if(userRepository.existsByEmail(email))
           throw new UserException(UserErrorCode.EXIST_EMAIL) ;
        return "해당 이메일은 사용 가능합니다.";
    }

    public String checkNickName(String nickName) {
        if(userRepository.existsByNickName(nickName))
            throw new UserException(UserErrorCode.EXIST_NICKNAME) ;
        return "해당 nickName은 사용 가능합니다.";
    }

}
