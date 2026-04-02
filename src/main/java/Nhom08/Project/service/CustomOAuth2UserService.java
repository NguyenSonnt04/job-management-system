package Nhom08.Project.service;

import Nhom08.Project.entity.Role;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.RoleRepository;
import Nhom08.Project.repository.UserRepository;
import Nhom08.Project.security.CustomOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Xác định provider (google / github)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email;
        String name;
        String providerId;
        String provider;

        if ("github".equals(registrationId)) {
            String login = oauth2User.getAttribute("login");
            email = oauth2User.getAttribute("email");
            // Email private → gọi API /user/emails để lấy email thật
            if (email == null || email.isBlank()) {
                String token = userRequest.getAccessToken().getTokenValue();
                email = fetchGithubPrimaryEmail(token);
            }
            // Vẫn null → fallback noreply
            if (email == null || email.isBlank()) {
                Object idObj = oauth2User.getAttribute("id");
                email = (idObj != null ? idObj.toString() : login) + "+" + login + "@users.noreply.github.com";
            }
            name = oauth2User.getAttribute("name");
            if (name == null || name.isBlank()) name = login;
            providerId = login;
            provider = "GITHUB";
        } else {
            // Google
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
            providerId = oauth2User.getAttribute("sub");
            provider = "GOOGLE";
            if (email == null) {
                throw new OAuth2AuthenticationException("Không lấy được email từ tài khoản Google");
            }
        }

        final String finalEmail    = email;
        final String finalName     = name;
        final String finalProvider = provider;
        final String finalId       = providerId;

        User user = userRepository.findByEmail(finalEmail).map(existingUser -> {
            if (existingUser.getProviderId() == null) {
                existingUser.setProvider(finalProvider);
                existingUser.setProviderId(finalId);
                userRepository.save(existingUser);
            }
            return existingUser;
        }).orElseGet(() -> {
            Role candidateRole = roleRepository.findByName(Role.CANDIDATE)
                    .orElseThrow(() -> new OAuth2AuthenticationException("Không tìm thấy role CANDIDATE"));

            User newUser = new User();
            newUser.setEmail(finalEmail);
            newUser.setFullName(finalName != null ? finalName : finalEmail);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setProvider(finalProvider);
            newUser.setProviderId(finalId);
            newUser.setEnabled(true);
            newUser.setRole(candidateRole);
            return userRepository.save(newUser);
        });

        return new CustomOAuth2User(oauth2User, user);
    }

    /** Gọi GitHub API lấy email chính (kể cả email private) */
    private String fetchGithubPrimaryEmail(String accessToken) {
        try {
            List<Map<String, Object>> emails = RestClient.create()
                    .get()
                    .uri("https://api.github.com/user/emails")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (emails == null) return null;

            // Ưu tiên email primary + verified
            return emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElseGet(() -> emails.stream()
                            .map(e -> (String) e.get("email"))
                            .filter(em -> em != null && !em.isBlank())
                            .findFirst()
                            .orElse(null));
        } catch (Exception e) {
            return null;
        }
    }
}
