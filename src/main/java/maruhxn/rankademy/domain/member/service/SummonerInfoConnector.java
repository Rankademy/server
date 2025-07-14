package maruhxn.rankademy.domain.member.service;

import maruhxn.rankademy.domain.member.SummonerInfo;
import maruhxn.rankademy.domain.member.dto.RiotAuthRequest;

/**
 * 도메인 서비스
 * 소환사 이름 및 태그를 받으면 이와 일치하는 소환사 정보를 Riot API를 통해 받아옵니다
 */
public interface SummonerInfoConnector {

    SummonerInfo connect(RiotAuthRequest request);
}
