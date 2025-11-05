package maruhxn.rankademy.application.user;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.provided.UserWriter;
import maruhxn.rankademy.application.user.required.EmailSender;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.ProfileUpdateRequest;
import maruhxn.rankademy.domain.user.dto.UserOAuth2CreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
public class UserModifyService implements UserWriter {

    private final UserReader userReader;
    private final UserRepository userRepository;
    private final EmailSender emailSender;

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

//    @Override
//    public User enrollUnivInfo(Long userId, EnrollUnivRequest enrollUnivRequest) {
//        User user = userReader.get(userId);
//
//        if (!univExtractor.extractUnivNameFromMail(enrollUnivRequest.univName(), enrollUnivRequest.univMail()))
//            throw new IllegalArgumentException("학교 이메일이 올바르지 않습니다.");
//
//        user.enrollUnivInfo(enrollUnivRequest);
//
//        return userRepository.save(user);
//    }

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
