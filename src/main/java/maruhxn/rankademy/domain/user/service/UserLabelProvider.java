package maruhxn.rankademy.domain.user.service;

import java.util.List;

/**
 * AI 서비스로부터 멤버의 칭호 리스트를 가져옵니다.
 */
public interface UserLabelProvider {

    List<String> getLabels(String puuid);

}
