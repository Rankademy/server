package maruhxn.rankademy.domain.user.service;

import maruhxn.rankademy.domain.user.Rank;
import maruhxn.rankademy.domain.user.Tier;
import maruhxn.rankademy.domain.user.TierInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TierMapperTest {

    @Nested
    @DisplayName("tierToScore 메서드 테스트")
    class TierToScore {

        @Test
        @DisplayName("UNRANKED는 0점으로 변환된다")
        void tierToScore_UNRANKED() {
            int score = TierMapper.tierToScore(Tier.UNRANKED, Rank.EMPTY, 0);
            assertThat(score).isEqualTo(-1);
        }

        @Test
        @DisplayName("PLATINUM II 75LP는 2375점으로 변환된다")
        void tierToScore() {
            // given
            Tier tier = Tier.PLATINUM;
            Rank rank = Rank.II;
            int lp = 75;

            // when
            int score = TierMapper.tierToScore(tier, rank, lp);

            // then
            assertThat(score).isEqualTo(2375);
        }

        @Test
        @DisplayName("IRON IV 0LP는 100점으로 변환된다")
        void tierToScore_whenIronIV0Lp() {
            // given
            Tier tier = Tier.IRON;
            Rank rank = Rank.IV;
            int lp = 0;

            // when
            int score = TierMapper.tierToScore(tier, rank, lp);

            // then
            assertThat(score).isEqualTo(100);
        }

        @Test
        @DisplayName("DIAMOND I 99LP는 3499점으로 변환된다")
        void tierToScore_whenDiamondI100Lp() {
            // given
            Tier tier = Tier.DIAMOND;
            Rank rank = Rank.I;
            int lp = 99;

            // when
            int score = TierMapper.tierToScore(tier, rank, lp);

            // then
            assertThat(score).isEqualTo(3499);
        }
    }

    @Nested
    @DisplayName("scoreToTier 메서드 테스트")
    class ScoreToTier {

        @Test
        @DisplayName("0점은 UNRANKED로 변환된다")
        void scoreToTier_UNRANKED() {
            TierInfo tierInfo = TierMapper.scoreToTier(-1);
            assertThat(tierInfo.getTier()).isEqualTo(Tier.UNRANKED);
            assertThat(tierInfo.getRank()).isEqualTo(Rank.EMPTY);
            assertThat(tierInfo.getLp()).isEqualTo(0);
        }

        @Test
        @DisplayName("2375점은 PLATINUM II 75LP로 변환된다")
        void scoreToTier() {
            // given
            int score = 2375;

            // when
            TierInfo tierInfo = TierMapper.scoreToTier(score);

            // then
            assertThat(tierInfo.getTier()).isEqualTo(Tier.PLATINUM);
            assertThat(tierInfo.getRank()).isEqualTo(Rank.II);
            assertThat(tierInfo.getLp()).isEqualTo(75);
        }

        @Test
        @DisplayName("100점은 IRON IV 0LP로 변환된다")
        void scoreToTier_when0() {
            // given
            int score = 100;

            // when
            TierInfo tierInfo = TierMapper.scoreToTier(score);

            // then
            assertThat(tierInfo.getTier()).isEqualTo(Tier.IRON);
            assertThat(tierInfo.getRank()).isEqualTo(Rank.IV);
            assertThat(tierInfo.getLp()).isEqualTo(0);
        }

        @Test
        @DisplayName("3500점은 MASTER 0LP로 변환된다")
        void scoreToTier_when3500() {
            // given
            int score = 3500;

            // when
            TierInfo tierInfo = TierMapper.scoreToTier(score);

            // then
            assertThat(tierInfo.getTier()).isEqualTo(Tier.MASTER);
            assertThat(tierInfo.getRank()).isNull();
            assertThat(tierInfo.getLp()).isEqualTo(0);
        }

        @Test
        @DisplayName("4000점은 MASTER 500LP로 변환된다")
        void scoreToTier_when4000() {
            // given
            int score = 4000;

            // when
            TierInfo tierInfo = TierMapper.scoreToTier(score);

            // then
            assertThat(tierInfo.getTier()).isEqualTo(Tier.MASTER);
            assertThat(tierInfo.getRank()).isNull();
            assertThat(tierInfo.getLp()).isEqualTo(500);
        }

        @Test
        @DisplayName("1900점은 GOLD I 0LP로 변환된다")
        void scoreToTier_when1900() {
            // given
            int score = 1900;

            // when
            TierInfo tierInfo = TierMapper.scoreToTier(score);

            // then
            assertThat(tierInfo.getTier()).isEqualTo(Tier.GOLD);
            assertThat(tierInfo.getRank()).isEqualTo(Rank.I);
            assertThat(tierInfo.getLp()).isEqualTo(0);
        }

        @Test
        @DisplayName("1899점은 GOLD II 99LP로 변환된다")
        void scoreToTier_when1899() {
            // given
            int score = 1899;

            // when
            TierInfo tierInfo = TierMapper.scoreToTier(score);

            // then
            assertThat(tierInfo.getTier()).isEqualTo(Tier.GOLD);
            assertThat(tierInfo.getRank()).isEqualTo(Rank.II);
            assertThat(tierInfo.getLp()).isEqualTo(99);
        }
    }
}
