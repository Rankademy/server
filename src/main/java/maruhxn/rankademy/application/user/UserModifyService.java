package maruhxn.rankademy.application.user;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.application.user.required.EmailSender;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.Email;
import maruhxn.rankademy.domain.user.PasswordEncoder;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;
import maruhxn.rankademy.domain.user.exception.DuplicateUsernameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class UserModifyService implements UserWriter {

    private final UserReader userReader;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;

    @Override
    public User register(UserRegisterRequest registerRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(new Email(registerRequest.email()));

        User user;
        if (optionalUser.isPresent()) {
            // TODO: 같은 이메일로 이메일 회원가입을 중복 진행하는 경우 막기.
            user = optionalUser.get();
            user.changePassword(registerRequest.password(), passwordEncoder);
        } else {
            this.checkDuplicateUsername(registerRequest.username());

            user = User.register(registerRequest, passwordEncoder);

            this.sendWelcomeEmail(user);
        }

        return userRepository.save(user);
    }

    private void sendWelcomeEmail(User user) {
        emailSender.send(
                user.getEmail(),
                "등록을 완료해주세요.",
                String.format("안녕하세요, %s님!%nRankademy에 가입해 주셔서 진심으로 감사합니다.", user.getUsername())
        );
    }

    private void checkDuplicateUsername(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUsernameException(username);
        }
    }

    @Override
    public User enrollUnivInfo(Long userId, EnrollUnivRequest enrollUnivRequest) {
        User user = userReader.find(userId);
        // TODO: 이메일 일치 여부 확인 로직 추가 필요
        user.enrollUnivInfo(enrollUnivRequest);
        return userRepository.save(user);
    }

    @Override
    public User removeUnivInfo(Long userId) {
        User user = userReader.find(userId);
        user.removeUnivInfo();
        return userRepository.save(user);
    }

    @Override
    public User updateProfile(Long userId, ProfileUpdateRequest updateProfileRequest) {
        User user = userReader.find(userId);

        this.checkDuplicateUsername(updateProfileRequest.username());

        user.updateProfile(updateProfileRequest);

        return userRepository.save(user);
    }

    @Override
    public void withdraw(Long userId) {
        User user = userReader.find(userId);
        userRepository.delete(user);
    }

}

