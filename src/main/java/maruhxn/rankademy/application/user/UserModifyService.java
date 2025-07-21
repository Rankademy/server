package maruhxn.rankademy.application.user;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.application.user.required.EmailSender;
import maruhxn.rankademy.application.user.required.UnivMailValidator;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.Email;
import maruhxn.rankademy.domain.user.PasswordEncoder;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;
import maruhxn.rankademy.domain.user.dto.UserRegisterRequest;
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
    private final UnivMailValidator univMailValidator;

    @Override
    public User registerOrSetPassword(UserRegisterRequest registerRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(new Email(registerRequest.email()));

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isRegisteredViaOAuthOnly()) {
                // 비밀번호 설정만 진행 (소셜 가입자 → 이메일 로그인 확장)
                user.changePassword(registerRequest.password(), passwordEncoder);
                return userRepository.save(user);
            }
        }

        this.checkDuplicateUsername(registerRequest.username());
        User user = User.register(registerRequest, passwordEncoder);
        this.sendWelcomeEmail(user);

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
            throw new IllegalStateException("이미 사용 중인 유저명입니다: " + username);
        }
    }

    @Override
    public User enrollUnivInfo(Long userId, EnrollUnivRequest enrollUnivRequest) {
        User user = userReader.get(userId);

        if (!univMailValidator.isValid(enrollUnivRequest.univName(), enrollUnivRequest.univMail()))
            throw new IllegalArgumentException("학교 이메일이 올바르지 않습니다.");

        user.enrollUnivInfo(enrollUnivRequest);

        return userRepository.save(user);
    }

    @Override
    public User removeUnivInfo(Long userId) {
        User user = userReader.get(userId);
        user.removeUnivInfo();
        return userRepository.save(user);
    }

    @Override
    public User updateProfile(Long userId, ProfileUpdateRequest updateProfileRequest) {
        User user = userReader.get(userId);

        this.checkDuplicateUsername(updateProfileRequest.username());

        user.updateProfile(updateProfileRequest);

        return userRepository.save(user);
    }

    @Override
    public void withdraw(Long userId) {
        User user = userReader.get(userId);
        userRepository.delete(user);
    }

    @Override
    public User oauth2Register(UserOAuth2CreateRequest userOAuth2CreateRequest) {
        User user = User.oauth2Register(userOAuth2CreateRequest);
        this.sendWelcomeEmail(user);
        return userRepository.save(user);
    }

}

