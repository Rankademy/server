package maruhxn.rankademy.adapter.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maruhxn.rankademy.application.member.provided.MemberWriter;
import maruhxn.rankademy.domain.member.Member;
import maruhxn.rankademy.domain.member.dto.MemberRegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApi {

    private final MemberWriter memberWriter;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Long register(
            @RequestBody @Valid MemberRegisterRequest request
    ) {
        Member member = memberWriter.register(request);
        return member.getId();
    }
}
