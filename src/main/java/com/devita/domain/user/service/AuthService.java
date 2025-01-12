package com.devita.domain.user.service;

import com.devita.common.exception.ErrorCode;
import com.devita.common.exception.SecurityTokenException;
import com.devita.common.jwt.JwtTokenProvider;
import com.devita.domain.category.dto.CategoryReqDTO;
import com.devita.domain.category.dto.CategoryResDTO;
import com.devita.domain.category.service.CategoryService;
import com.devita.domain.character.domain.Reward;
import com.devita.domain.character.repository.RewardRepository;
import com.devita.domain.user.domain.AuthProvider;
import com.devita.domain.user.domain.User;
import com.devita.domain.user.dto.UserAuthResponse;
import com.devita.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final RewardRepository rewardRepository;

    @Transactional
    public User loadUser(Map<String, Object> attributes){
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> properties = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) properties.get("nickname");
        String profileImage = (String) properties.get("profile_image_url");

        if (email == null) {
            throw new OAuth2AuthenticationException(ErrorCode.TOKEN_NOT_FOUND.getMessage());
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .provider(AuthProvider.KAKAO)
                            .profileImage(profileImage)
                            .build();

                    User savedUser = userRepository.save(newUser);

                    Reward reward = Reward.builder()
                            .user(savedUser)
                            .experience(0)
                            .nutrition(0)
                            .build();
                    rewardRepository.save(reward);

                    createDefaultCategories(savedUser.getId());

                    return savedUser;
                });
        user.updateNickname(nickname);
        userRepository.save(user);

        log.info("유저 로그인 성공!");

        return user;

    }

    private void createDefaultCategories(Long userId) {
        String[] defaultCategories = {"일반", "일일 미션", "자율 미션"};
        String[] defaultColors = {"#6DC2FF", "#086BFF", "#7DB1FF"};

        for (int i = 0; i < defaultCategories.length; i++) {
            CategoryReqDTO categoryReqDto = CategoryReqDTO.builder()
                    .name(defaultCategories[i])
                    .color(defaultColors[i])
                    .build();

            categoryService.createCategory(userId, categoryReqDto);
        }
    }

    public UserAuthResponse issueAccessAndRefreshTokens(Long userId, String kakaoAccessToken) {
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        return refreshUserAuth(refreshToken, kakaoAccessToken);
    }

    public UserAuthResponse refreshUserAuth(String refreshToken, String kakaoAccessToken) {
        try {
            log.info("액세스 토큰을 생성합니다.");
            Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
            String newAccessToken = jwtTokenProvider.validateRefreshToken(refreshToken, userId);
            log.info("액세스 토큰: " + newAccessToken);
            // 사용자 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new SecurityTokenException(ErrorCode.USER_NOT_FOUND));

            // 카테고리 정보 조회
            List<CategoryResDTO> categories = categoryService.findUserCategories(userId);

            // 응답 데이터 생성
            return UserAuthResponse.builder()
                    .refreshToken(refreshToken)     // 수정 필요
                    .accessToken(newAccessToken)
                    .kakaoAccessToken(kakaoAccessToken)
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .imageUrl(user.getProfileImage())
                    .categories(categories)
                    .build();

        } catch (Exception e) {
            log.error("Failed to refresh user authentication: {}", e.getMessage());
            throw new SecurityTokenException(ErrorCode.INTERNAL_TOKEN_SERVER_ERROR);
        }
    }

    public String reissueToken(String refreshToken) {
        try {
            log.info("액세스 토큰을 생성합니다.");
            Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

            return jwtTokenProvider.validateRefreshToken(refreshToken, userId);

        } catch (Exception e) {
            log.error("Failed to refresh user authentication: {}", e.getMessage());
            throw new SecurityTokenException(ErrorCode.INTERNAL_TOKEN_SERVER_ERROR);
        }
    }
}