package maruhxn.rankademy.application.match.required;

import maruhxn.rankademy.domain.match.MatchData;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchDataRepository extends MongoRepository<MatchData, ObjectId> {

}
