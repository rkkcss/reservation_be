package hu.daniinc.reservation.service;

import hu.daniinc.reservation.domain.Authority;
import hu.daniinc.reservation.domain.User;
import hu.daniinc.reservation.repository.AuthorityRepository;
import hu.daniinc.reservation.repository.UserRepository;
import hu.daniinc.reservation.security.AuthoritiesConstants;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email = oAuth2User.getAttribute("email");

        userRepository
            .findOneByEmailIgnoreCase(email)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setLogin(email);
                newUser.setEmail(email);
                newUser.setFirstName(oAuth2User.getAttribute("given_name"));
                newUser.setLastName(oAuth2User.getAttribute("family_name"));
                newUser.setActivated(true);
                newUser.setLangKey("hu");
                newUser.setImageUrl(oAuth2User.getAttribute("picture"));
                newUser.setPassword(passwordEncoder.encode(RandomStringUtils.randomAlphanumeric(60)));

                Authority authority = authorityRepository.findById(AuthoritiesConstants.USER).orElseThrow();
                newUser.setAuthorities(Set.of(authority));

                return userRepository.save(newUser);
            });

        // ← EZ A LÉNYEG: email legyen a getName() értéke
        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
            oAuth2User.getAuthorities(),
            oAuth2User.getAttributes(),
            "email"
        );
    }
}
