package maruhxn.rankademy.adapter.integration.labels;

import maruhxn.rankademy.domain.user.service.UserLabelProvider;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Fallback
public class DummyUserLabelProvider implements UserLabelProvider {

    @Override
    public List<String> getLabels(String puuid) {
        return List.of("DUMMY");
    }

}
