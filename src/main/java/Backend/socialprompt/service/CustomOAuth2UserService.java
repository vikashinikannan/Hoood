package Backend.socialprompt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService  extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest userRequest) {
        OAuth2User user = super.loadUser(userRequest);
        // you can extract attributes here
        return user;
    }
}
