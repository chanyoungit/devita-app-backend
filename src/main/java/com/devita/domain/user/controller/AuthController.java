package com.devita.domain.user.controller;

import com.devita.common.response.ApiResponse;
import com.devita.domain.user.dto.AuthDTO;
import com.devita.domain.user.domain.User;
import com.devita.domain.user.dto.UserAuthResponse;
import com.devita.domain.user.service.AuthService;
import com.devita.domain.user.service.KakaoUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final KakaoUserInfoService kakaoUserInfoService;


    @PostMapping("/user/info")
    public ApiResponse<UserAuthResponse> sendUserInitData(@RequestBody AuthDTO authDTO) {
        log.info("로그인 성공 후 유저 정보를 반환합니다.(액세스 토큰, 닉네임 ...)");

        String kakaoAccessToken = kakaoUserInfoService.getKakaoAccessToken(authDTO.getAuthorizationCode());
        Map<String, Object> userInfo = kakaoUserInfoService.getUserInfo(kakaoAccessToken);
        User user = authService.loadUser(userInfo);
        UserAuthResponse userAuthResponse = authService.issueAccessAndRefreshTokens(user.getId(), kakaoAccessToken);
        log.info("userAuthResponse.refreshToken: " + userAuthResponse.refreshToken());
        log.info("userAuthResponse.accessToken: " + userAuthResponse.accessToken());

        return ApiResponse.success(userAuthResponse);
    }

    @PostMapping("/reissue")
    public ApiResponse<String> refreshAccessToken(@RequestHeader("Refresh") String refreshToken) {

        return ApiResponse.success(authService.reissueToken(refreshToken));
    }
}
