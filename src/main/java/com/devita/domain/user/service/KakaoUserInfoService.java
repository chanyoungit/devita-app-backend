package com.devita.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class KakaoUserInfoService {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private String kakaoClientId = "3974cecc2e6156a32bfb904ff99de98e";

    private String redirectUri = "http://localhost:3000/callback";

    // 필요시 설정
    // @Value("${kakao.client-secret}")
    // private String kakaoClientSecret;

    /**
     * 인가 코드를 받아서 액세스 토큰을 발급받는 메서드
     */
    public Map<String, Object> getAccessToken(String authorizationCode) {
        log.info("Get access token");
        RestTemplate restTemplate = new RestTemplate();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // HTTP 바디(form-data) 설정
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", authorizationCode);
        // 필요시 client_secret 추가
        // formData.add("client_secret", kakaoClientSecret);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            log.info("토큰 응답: " + responseBody);
            return responseBody;
        } catch (HttpClientErrorException e) {
            log.error("카카오 토큰 발급 에러 응답: " + e.getResponseBodyAsString(), e);
            throw e;
        } catch (ResourceAccessException e) {
            log.error("카카오 토큰 발급 요청 중 네트워크 문제 발생: " + e.getMessage(), e);
            throw e;
        }
    }

    public String getKakaoAccessToken(String authorizationCode){
        Map<String, Object> tokenResponse = getAccessToken(authorizationCode);
        return (String) tokenResponse.get("access_token");
    }


    /**
     * 액세스 토큰을 이용해 사용자 정보를 가져오는 메서드
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        log.info("카카오 사용자 정보 요청 시작");
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secure_resource", "false");
        formData.add("property_keys", "[\"kakao_account.email\",\"kakao_account.profile\",\"kakao_account.name\"]");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            log.info("카카오 사용자 정보 응답: " + response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // 에러 응답 바디 확인
            log.error("카카오 사용자 정보 API 에러 응답: " + e.getResponseBodyAsString(), e);
            throw e;
        } catch (ResourceAccessException e) {
            // 타임아웃 또는 네트워크 이슈 확인 필요
            log.error("카카오 사용자 정보 요청 중 네트워크 문제 발생: " + e.getMessage(), e);
            throw e;
        }
    }
}