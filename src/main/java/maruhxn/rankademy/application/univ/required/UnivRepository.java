package maruhxn.rankademy.application.univ.required;

import maruhxn.rankademy.domain.univ.Univ;
import org.springframework.data.repository.Repository;

import java.util.Optional;


public interface UnivRepository extends Repository<Univ, Long> {

    Univ findById(Long id);

    Optional<Univ> findByUnivMailPostfix(String univMailPostfix);
}
