package maruhxn.rankademy.domain.univ;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import maruhxn.rankademy.domain.shared.AbstractEntity;

@Table(name = "univ")
@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Univ extends AbstractEntity {

    @Column(name = "univ_name")
    private String univName;

    @Column(name = "univ_mail_postfix")
    private String univMailPostfix;

}
