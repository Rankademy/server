package maruhxn.rankademy.adapter.integration;

import maruhxn.rankademy.domain.user.service.UserTitleProvider;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Fallback
public class DummyUserTitleProvider implements UserTitleProvider {

    @Override
    public List<String> getTitles(Long userId) {
        return List.of("DUMMY");
    }

}
