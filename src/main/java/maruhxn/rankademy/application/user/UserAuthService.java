package maruhxn.rankademy.application.user;

import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.user.provided.UserAuthorizer;
import maruhxn.rankademy.application.user.provided.UserReader;
import maruhxn.rankademy.application.user.required.UnivMailCertifier;
import maruhxn.rankademy.application.user.required.UserRepository;
import maruhxn.rankademy.domain.user.User;
import maruhxn.rankademy.domain.user.dto.RiotAuthRequest;
import maruhxn.rankademy.domain.user.exception.RequiredUnivInfoException;
import maruhxn.rankademy.domain.user.service.SummonerInfoConnector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UserAuthService implements UserAuthorizer {

    private final UserReader userReader;
    private final UserRepository userRepository;
    private final UnivMailCertifier univMailCertifier;
    private final SummonerInfoConnector summonerInfoConnector;

    @Override
    public void sendUnivCertifyMail(Long userId) {
        User user = userReader.get(userId);

        if (user.getUnivInfo() == null) {
            throw new RequiredUnivInfoException();
        }

        univMailCertifier.sendCertifyMail(
                user.getUnivInfo().univMail().address(),
                user.getUnivInfo().univName(),
                user.getUnivInfo().inCollege()
        );
    }

    @Override
    @Transactional
    public User completeUnivAuthentication(Long userId, int code) {
        User user = userReader.get(userId);

        univMailCertifier.certifyCode(
                user.getUnivInfo().univMail().address(),
                user.getUnivInfo().univName(),
                code
        );

        user.completeUnivAuthentication();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User completeRiotAuthentication(Long userId, RiotAuthRequest riotAuthRequest) {
        User user = userReader.get(userId);
        user.connectSummonerInfo(summonerInfoConnector, riotAuthRequest);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User removeRiotAuthentication(Long userId) {
        User user = userReader.get(userId);
        user.removeRiotAuthentication();
        return userRepository.save(user);
    }
}
