package maruhxn.rankademy.application.univ.required;

import maruhxn.rankademy.domain.univ.Univ;
import org.springframework.data.repository.Repository;


public interface UnivRepository extends Repository<Univ, Long> {

    Univ findById(Long id);

    boolean existsByUnivNameAndUnivMailPostfix(String univName, String univMailPostfix);
}
