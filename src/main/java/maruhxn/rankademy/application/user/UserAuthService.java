package maruhxn.rankademy.application.user;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.univ_certification_code.provided.UnivCertificationCodeManager;
import maruhxn.rankademy.application.user.provided.UserAuthorizer;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.required.UnivMailCertifier;
import maruhxn.rankademy.application.user.required.UnivValidator;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.shared.DomainEventPublisher;
import maruhxn.rankademy.domain.shared.event.RiotAuthEvent;
import maruhxn.rankademy.domain.univ_certification_code.UnivCertificationCode;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.EnrollUnivRequest;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ThreadLocalRandom;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class UserAuthService implements UserAuthorizer {

    private final UserReader userReader;
    private final UserRepository userRepository;
    private final UnivValidator univValidator;
    private final UnivMailCertifier univMailCertifier;
    private final SummonerInfoConnector summonerInfoConnector;
    private final DomainEventPublisher publisher;
    private final UnivCertificationCodeManager univCertificationCodeManager;

    @Override
    public void sendUnivCertifyMail(Long userId, String univName, String email) {
        User user = userReader.get(userId);
        if (user.getUnivInfo().isAuthorized()) {
            throw new IllegalStateException("이미 대학교 인증이 완료되었습니다.");
        }
        univValidator.validateUnivMail(univName, email);

        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);

        univMailCertifier.sendCertifyMail(email, code);

        univCertificationCodeManager.generateCode(
                userId,
                email,
                univName,
                code
        );
    }

    @Override
    public User completeUnivAuthentication(Long userId, String email, int code) {
        User user = userReader.get(userId);

        UnivCertificationCode certificationCode = univMailCertifier.certifyCode(email, code);

        user.completeUnivAuthentication(new EnrollUnivRequest(certificationCode.univName, email));

        return userRepository.save(user);
    }

    @Override
    public User completeRiotAuthentication(Long userId, RiotAuthRequest riotAuthRequest) {
        User user = userReader.get(userId);
        user.connectSummonerInfo(summonerInfoConnector, riotAuthRequest);
        publisher.publish(new RiotAuthEvent(userId));
        return userRepository.save(user);
    }

    @Override
    public User removeRiotAuthentication(Long userId) {
        User user = userReader.get(userId);
        user.removeRiotAuthentication();
        return userRepository.save(user);
    }
}
