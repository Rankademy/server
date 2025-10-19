package maruhxn.rankademy.application.match.required;

import maruhxn.rankademy.domain.match.MatchData;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MatchDataRepository extends MongoRepository<MatchData, ObjectId> {

    List<MatchData> findAllByUserId(Long userId);
}
