package maruhxn.rankademy.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class EffectiveStrength {

    /**
     * 플레이어 추정 실력
     * 경기 승/패에 따라 증감
     */
    @Column(name = "mu")
    private Double mu = DEFAULT_MU;

    /**
     * 실력 추정의 불확실성
     * 경기를 거듭하면 감소
     */
    @Column(name = "sigma")
    private Double sigma = DEFAULT_SIGMA;

    public static Double DEFAULT_MU = 0.0;
    public static Double DEFAULT_SIGMA = 6.0;

    public EffectiveStrength(Double mu, Double sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }
}
