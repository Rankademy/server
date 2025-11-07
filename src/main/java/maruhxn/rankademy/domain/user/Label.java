package maruhxn.rankademy.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Label {
    @Column(nullable = false)
    String value;

    public Label(String value) {
        this.value = value;
    }
}
