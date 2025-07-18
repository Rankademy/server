package maruhxn.rankademy.adapter.integration;

import maruhxn.rankademy.domain.member.service.MemberTitleProvider;
import org.springframework.context.annotation.Fallback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Fallback
public class DummyMemberTitleProvider implements MemberTitleProvider {

    @Override
    public List<String> getTitles(Long memberId) {
        return List.of("DUMMY");
    }

}
